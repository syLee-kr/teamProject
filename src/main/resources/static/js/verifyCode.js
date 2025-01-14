document.addEventListener("DOMContentLoaded", function() {

	let countdown;
	let countdownTime = 3 * 60; // 3분(유효시간)
	let resendCodeBtn = document.getElementById("resendCodeBtn");
	let verifyCodeBtn = document.getElementById("verifyCodeBtn");
	
	// 인증코드 발송 버튼 클릭 시
	const sendCodeBtn = document.getElementById("sendCodeBtn");
	if (sendCodeBtn){
		sendCodeBtn.addEventListener("click", function(event){
	    	event.preventDefault();  // 폼 제출을 막고, Ajax로 인증코드를 발송
	
		    // 서버에 인증코드 발송 요청
		    fetch("/sendCode", {
		        method: "POST",
		        headers: {
		            "Content-Type": "application/x-www-form-urlencoded",
		        },
		        body: "username=" + document.getElementById("username").value + "&email=" + document.getElementById("email").value
		    })
		    .then(response => {
				if (!response.ok){ // 응답 실패한 경우
					throw new Error('네트워크 응답 실패');
				}
				
				return response.json()
			})	
		    .then(data => {
		        if (data.success) {
		            alert("인증코드가 발송되었습니다.");
		            enableResetCode();  // 인증코드 입력란 활성화
		        } else {
		            alert("인증코드 발송에 실패했습니다.");
		        }
		    })
		    .catch(error => {
		        console.error('Error:', error);
		    });
		});
	}
	
	// 카운트다운을 시작하는 함수
	function startCountdown() {
	    // 이미 타이머가 실행 중이라면, 기존 타이머를 중지
	    if (countdown) {
	        clearInterval(countdown);
	    }
	
	    countdown = setInterval(function() {
	        const minutes = Math.floor(countdownTime / 60);
	        const seconds = countdownTime % 60;
	        document.getElementById("countdown").textContent = `유효시간: ${minutes}:${seconds < 10 ? '0' + seconds : seconds}`;
	
	        if (countdownTime <= 0) {
	            clearInterval(countdown);
	            document.getElementById("countdown").textContent = "유효시간 만료";
	            document.getElementById("resetCode").disabled = false;
	            resendCodeBtn.style.display = 'inline';  // 재발송 버튼 표시
	        } else {
	            countdownTime--;
	        }
	    }, 1000);
	}
	
	
	// 인증코드를 활성화하고 카운트다운을 시작하는 함수
	function enableResetCode() {
	    document.getElementById("username").disabled = true;
	    document.getElementById("email").disabled = true;
	    document.getElementById("sendCodeBtn").disabled = true;  // 인증코드 발송 버튼 비활성화
	    document.getElementById("resetCode").disabled = false;
	    
		// 카운트 다운 시작
		startCountdown();
		
		// 인증코드 발송 후 재발송 버튼 활성화
		resendCodeBtn.style.display = 'inline';
		
		// 인증코드 확인 버튼 이벤트 리스너 추가
		addVerifyCodeBtnEventListener(); // 동적으로 버튼이 생긴 후에 이벤트 리스너를 추가
	}
	
	
	// 인증코드 확인 버튼 클릭 이벤트 처리
	function addVerifyCodeBtnEventListener(){
		let verifyCodeBtn = document.getElementById("verifyCodeBtn");
		if (verifyCodeBtn){
			verifyCodeBtn.addEventListener("click", function() {
				var resetCode = document.getElementById("resetCode").value;
	
			    // 서버로 인증코드 확인 요청
			    fetch("/verifyCode", {
			        method: "POST",
			        headers: {
			            "Content-Type": "application/x-www-form-urlencoded",
			        },
			        body: "resetCode=" + resetCode
			    })
			    .then(response => response.json())
			    .then(isValid => {
			        if (isValid) {
			            // 인증 코드가 유효한 경우
			            alert("인증코드가 확인되었습니다. 비밀번호를 재설정 할 수 있습니다.");
			            // 인증코드 입력란 비활성화, 비밀번호 재설정 화면 등 추가 작업
			            document.getElementById("resetCode").disabled = true;
			            clearInterval(countdown); // 인증 후 카운트다운 정지
			        } else {
			            // 인증 코드가 유효하지 않은 경우
			            alert("입력한 인증코드가 올바르지 않습니다.");
			        }
			    })
			    .catch(error => {
			        console.error('Error:', error);
			    });
			});
		}
	}
  
	// 인증코드 재발송 함수 (재발송 버튼 클릭 시 호출)
	function resendCode() {
	    // 아이디와 이메일을 가져와서 서버에 요청
	    const username = document.getElementById("username").value;
	    const email = document.getElementById("email").value;
	    
	    // 서버로 재발송 요청
	    fetch("/resend-Code", {
	        method: "POST",
	        headers: {
	            "Content-Type": "application/x-www-form-urlencoded"
	        },
	        body: `username=${username}&email=${email}`
	    })
	    .then(response => response.json())
	    .then(data => {
	        if (data.success) {
	            alert("인증코드가 재발송되었습니다.");
	        } else {
	            alert("인증코드 재발송에 실패했습니다.");
	        }
	    })
	    .catch(error => {
	        console.error("Error:", error);
	        alert("인증코드 재발송 중 오류가 발생했습니다.");
	    });

	    // 카운트다운 시간 초기화 및 타이머 재시작
	    countdownTime = 3 * 60;  // 3분으로 다시 설정
	    clearInterval(countdown);  // 기존 타이머 정지
	    startCountdown();  // 새로 카운트다운 시작
		}
		
		// 재발송 버튼 클릭 시 동작
		if(resendCodeBtn) {
		   resendCodeBtn.addEventListener("click", resendCode);
		}
});