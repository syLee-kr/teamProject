package com.example.camping.controller;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.camping.entity.Users;
import com.example.camping.security.PasswordEmailService;
import com.example.camping.userService.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@AllArgsConstructor
public class PasswordController {

	private UserService userService;
	private PasswordEmailService emailService;
	
	// 비밀번호 찾기 코드 생성
	private String generateResetCode(String username) {
		String resetCode = String.valueOf((int) (Math.random()*100000));
		log.debug("비밀번호 찾기 코드 생성: {}", resetCode);
		
		// 인증 코드 생성 시간 저장
		Users user = userService.findByUserId(username);
		user.setResetCodeGeneratedTime(LocalDateTime.now());
		userService.save(user);
		
		return resetCode;
	}
	
	// 비밀번호 유효성 검사
	private Boolean isPasswordValid(String password) {
		// 비밀번호 최소 길이 6자 이상(개발단계에는 4자 이상으로)
		if (password.length() < 4) {
			return false;
		}
		/*개발 끝날때 까지 4자 이상 아래 메서드는 비활성화
		// 숫자, 대소문자, 특수문자 포함 여부 확인
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\\$%\\^&\\*])(?=\\S+$).{8,}$";
		return password.matches(regex);
		*/
		return true; // 개발끝나면 해당 부분 지우고 적용
	}
	
	
	// 비밀번호 변경 폼
	@GetMapping("/change-password")
	public String changePasswordForm() {
		return "users/password/change-password";
	}
	
	// 비밀번호 변경 처리
	@PostMapping("/change-password")
	public String changePassword(@RequestParam(name="oldPassword") String oldPassword,
								 @RequestParam(name="newPassword") String newPassword,
								 @AuthenticationPrincipal User principal,
								 Model model,
								 HttpServletRequest request, HttpServletResponse response) {
		
		String userId = principal.getUsername();
		log.info("비밀번호 변경 요청: 사용자 {}", userId);
		
		// 비밀번호 유효성 검사
		if (!isPasswordValid(newPassword)) {
			model.addAttribute("error", "비밀번호는 최소 4자 이상 입력해야 합니다.");
			return "users/password/change-password";
		}
		// 사용자 확인
		Users user = userService.findByUserId(userId);
		if (user == null) {
			log.warn("사용자를 찾을 수 없음: {}", userId);
			model.addAttribute("error", "사용자를 찾을수 없습니다.");
			return "users/password/change-password";
		}

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		// 기존 비밀번호와 새로운 비밀번호가 같을 경우
		if (passwordEncoder.matches(newPassword,user.getPassword())) {
			log.warn("기존 비밀번호와 새로운 비밀번호가 같습니다: 사용자 {}", userId);
			String error = "기존 비밀번호와 같습니다. 새로운 비밀번호를 입력해주세요";
			model.addAttribute("error", error);
			return "users/password/change-password";
		}
		
		// 기존 비밀번호가 일치하면 비밀번호 변경
		if (user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
			// 비밀번호 암호화 후 저장
			user.setPassword(passwordEncoder.encode(newPassword));
			userService.save(user);
			log.info("비밀번호 변경 완료: 사용자 {}", userId);
			
			// 비밀번호 변경 후 로그아웃 처리(세션 무효)
			SecurityContextHolder.clearContext();
			LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
			logoutHandler.logout(request, response, 
					SecurityContextHolder.getContext().getAuthentication());
			log.info("로그아웃 처리 완료: 사용자 {}", userId);
			
			// 메세지 전달
			String message = "비밀번호가 변경되었습니다. 다시 로그인해주세요.";
			model.addAttribute("message", message);
			
			return "users/login/login-form";
		} else {
			log.warn("기존 비밀번호가 맞지 않음: 사용자 {}", userId);
			model.addAttribute("error", "기존 비밀번호가 맞지 않습니다.");
			return "users/password/change-password";
		}
		
	}
	

	// 비밀번호 찾기 폼
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "users/password/forgot-password"; 
    }
    
    // 비밀번호 찾기 인증코드 발송 처리
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam(name="username") String username, 
    							 @RequestParam(name="email") String email,
    							 Model model) {
    	log.info("비밀번호 찾기 요청: 아이디 {}, 이메일 {}", username, email);
        
    	// 아이디로 사용자 정보 조회
    	Users user = userService.findByUserId(username);
        
        if (user !=null) {
        	
        	// 아이디가 존재하면 이메일 확인
        	if (user.getEmail().equals(email)) {
	        	// 인증코드 생성
	        	String resetCode = generateResetCode(username);
	        	
	        	// 이메일 발송
	        	Boolean isSent = emailService.sendResetCodeEmail(user.getEmail(), resetCode);
	        	
	        	if (isSent) {
	        		// 인증코드 저장
	        		userService.saveResetCode(username, resetCode);
	        		log.info("이메일 발송 성공: 아이디 {}", username);
	        		
	        		String message = "비밀번호 재설정 링크를 이메일로 보냈습니다."; 
	        		model.addAttribute("message", message);
	        		model.addAttribute("resetCodeSent", true); 	// 인증코드 입력란 표시
	        		model.addAttribute("username", username);	// 아이디 유지
	        		model.addAttribute("email", email);			// 이메일 유지
	        	} else {
	        		log.error("이메일 발송 실패: 사용자 {}", username);
	        		model.addAttribute("error", "이메일 발송에 실패했습니다.");
	        	}
        	} else {
        		log.warn("이메일 불일치: 아이디: {}, 입력된 이메일 {}", username, email);
        		String error = "입력된 이메일과 아이디가 일치하지않습니다.";
        		model.addAttribute("error", error);
        	}	
	
        } else {
        	log.warn("사용자를 찾을 수 없음: {}", username);
        	model.addAttribute("error", "사용자를 찾을 수 없습니다.");
        }
        
        return "users/password/forgot-password";

    }
    
    // 인증코드 검증 처리
    @PostMapping("/verifyCode")
    @ResponseBody
    public ResponseEntity<Boolean> verifyCode(@RequestParam("resetCode") String resetCode) {
        log.info("인증코드 확인 요청: {}", resetCode);
        
        // 인증코드 검증 로직
        Boolean isValid = userService.verifyResetCode(resetCode);
        
        return ResponseEntity.ok(isValid);  // isValid 값만 보내기
    }

    // 비밀번호 재설정 폼
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(name="resetCode") String resetCode, Model model) {
    	log.info("비밀번호 재설정 폼 요청: 인증코드 {}", resetCode);
    	
    	// 코드 검증
    	Boolean isValid = userService.verifyResetCode(resetCode);
    	
    	if (isValid) {
    		Users user = userService.findByUserId(resetCode);
    		//  인증코드 유효시간
    		if (user != null) {
    			LocalDateTime codeGeneratedTime = user.getResetCodeGeneratedTime();
    			LocalDateTime expirationTime = codeGeneratedTime.plusMinutes(5);
    		
    			// 인증코드 유효시간 표시
    			model.addAttribute("resetCode", resetCode);
    			model.addAttribute("expirationTime", expirationTime);
    			return "users/password/reset-password";
    		}
    	} else {
    		log.warn("유효하지 않은 인증코드: {}", resetCode);
    		model.addAttribute("error", "유효하지 않은 코드 입니다.");
    		return "users/password/forgot-password";
    	}
    	return "users/password/reset-password";
    }
    
    // 비밀번호 재설정 처리
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam(name="resetCode") String resetCode, 
    							@RequestParam(name="newPassword") String newPassword,
    							@RequestParam(name="confirmPassword") String confirmPassword, Model model) {
    	log.info("비밀번호 재설정 요청: 인증코드 {}, 새로운 비밀번호", resetCode);
    	
        // 비밀번호 유효성 검사
        if (!isPasswordValid(newPassword)) {
            //model.addAttribute("error", "비밀번호는 최소 6자 이상이어야 하며, 숫자, 대소문자, 특수문자가 포함되어야 합니다.");
        	String error = "비밀번호는 최소 4자 이상이어야 합니다.";
        	model.addAttribute("error", error);
            return "users/password/reset-password";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            String error = "새로운 비밀번호와 비밀번호 확인이 일치하지 않습니다.";
        	model.addAttribute("error", error);
            return "users/password/reset-password";
        }
        
    	Boolean isValid = userService.verifyResetCode(resetCode);
    	
    	if (isValid) {
    		Users user = userService.findByUserId(resetCode);
    		if (user != null) {
    			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    			user.setPassword(passwordEncoder.encode(newPassword));
    			userService.save(user);
    			log.info("비밀번호 재설정 완료: 사용자 {}", user.getUserId());
    			
    			model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
    			return "redirect:/users/login/login-form";
    		} else {
    			log.warn("사용자를 찾을 수 없음: 인증코드 {}", resetCode);
    			model.addAttribute("error", "사용자를 찾을 수 없습니다.");
    		}
    	} else {
    		log.warn("유효하지 않은 인증코드: {}", resetCode);
    		model.addAttribute("error", "유효하지 않는 코드입니다.");
    	}
    	return "users/password/reset-password";
    }
    
	
}
