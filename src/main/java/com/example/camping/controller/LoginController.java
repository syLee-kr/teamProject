package com.example.camping.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.camping.entity.Users;
import com.example.camping.userService.UserService;

import com.example.camping.config.PasswordEmailService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@AllArgsConstructor
public class LoginController {
	
	private UserService userService;
	private PasswordEmailService emailService;
	
	// 비밀번호 찾기 코드 생성
	private String generateResetCode() {
		String resetCode = String.valueOf((int) (Math.random()*100000));
		log.debug("비밀번호 찾기 코드 생성: {}", resetCode);
		return resetCode;
	}
	
	// 로그인 폼
	@GetMapping("/login")
	public String loginForm() {
		return "users/login/login-form";
	}


	// 비밀번호 찾기 폼
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "users/login/forgot-password"; 
    }
    
    // 비밀번호 찾기 요청 처리
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam(name="username") String username, Model model) {
    	log.info("비밀번호 찾기 요청: 사용자 {}", username);
        Users user = userService.findByUserId(username);
        
        if (user !=null) {
        	// 인증코드 생성
        	String resetCode = generateResetCode();
        	
        	// 이메일 발송
        	Boolean isSent = emailService.sendResetCodeEmail(user.getEmail(), resetCode);
        	
        	if(isSent) {
        		// 인증코드 저장
        		userService.saveResetCode(username, resetCode);
        		log.info("이메일 발송 성공: 사용자 {}", username);
        		model.addAttribute("message", "비밀번호 재설정 링크를 이메일로 보냈습니다.");
        	} else {
        		log.error("이메일 발송 실패: 사용자 {}", username);
        		model.addAttribute("error", "이메일 발송에 실패했습니다.");
        	}

        } else {
        	log.warn("사용자를 찾을 수 없음: {}", username);
        	model.addAttribute("error", "사용자를 찾을 수 없습니다.");
        }
        
        return "users/login/forgot-password";
    }
    
    // 비밀번호 재설정 폼
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(name="resetCode") String resetCode, Model model) {
    	log.info("비밀번호 재설정 폼 요청: 인증코드 {}", resetCode);
    	
    	// 코드 검증
    	Boolean isValid = userService.verifyResetCode(resetCode);
    	
    	if(isValid) {
    		log.info("유효한 인증코드: {}", resetCode);
    		model.addAttribute("resetCode", resetCode);
    		return "users/login/reset-password";
    	}else {
    		log.warn("유효하지 않은 인증코드: {}", resetCode);
    		model.addAttribute("error", "유효하지 않은 코드 입니다.");
    		return "users/login/forgot-password";
    	}
    }
    
    // 비밀번호 재설정 처리
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam(name="resetCode") String resetCode, 
    							@RequestParam(name="newPassword") String newPassword, Model model) {
    	log.info("비밀번호 재설정 요청: 인증코드 {}, 새로운 비밀번호", resetCode);
    	
    	Boolean isValid = userService.verifyResetCode(resetCode);
    	
    	if (isValid) {
    		Users user = userService.findByUserId(resetCode);
    		if(user != null) {
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
    	}else {
    		log.warn("유효하지 않은 인증코드: {}", resetCode);
    		model.addAttribute("error", "유효하지 않는 코드입니다.");
    	}
    	return "users/login/reset-password";
    }
   	
	
	// 현재 로그인된 사용자 정보 로그
	@GetMapping("/loginSuccess")
	public String loginSuccess(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null) {
	    	String currentUser = authentication.getName();
	    	
	    	String authorities = authentication.getAuthorities().stream()
	    									   .map(authority -> authority.getAuthority())
	    									   .reduce((a, b) -> a + ", " + b)
	    									   .orElse("권한없음");
	    	
	    	log.info("현재 로그인된 사용자: {}, 권한: {}", currentUser, authorities);
	    	
	    	model.addAttribute("currentUser", currentUser);
	    	model.addAttribute("authorities", authorities);
	    
	    } else {
	    	log.warn("로그인된 사용자가 없습니다.");
	    }
	     
	     return "users/profile/profile-form";
	        
	}
	
	@GetMapping("/loginFail")
	public String loginFail(Model model) {
		String error = "로그인 실패, 아이디와 비밀번호를 확인해주세요!!";
		model.addAttribute("error" , error);
		log.warn("로그인 실패");
		return "users/login/login-form";
	}

}
