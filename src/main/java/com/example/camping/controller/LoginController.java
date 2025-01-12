package com.example.camping.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@AllArgsConstructor
public class LoginController {
	
	
	// 로그인 폼
	@GetMapping("/login")
	public String loginForm() {
		return "users/login/login-form";
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
