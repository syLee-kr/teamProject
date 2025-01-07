package com.example.camping.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.camping.entity.Users;
import com.example.camping.userService.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class LoginController {
	
	private UserService userService;
	
	// 로그인 폼
	@GetMapping("/login")
	public String loginForm() {
		return "users/login/login";
	}
	/* SecurityConfig 에서 로그인 요청처리, 로그아웃 처리 메서드 구현으로 생략되기 때문에
	 * 해당 부분을 비활성화 처리함
	// 로그인 요청 처리
	@PostMapping("/login")
	public String login(@RequestParam("userId") String userId,
						@RequestParam("password") String password,
						Model model) {
		Users user = userService.login(userId, password);
		
        if (user != null) {
            
            return "redirect:/"; // 로그인 후 메인페이지로
        } else {
            // 로그인 실패, 에러 메시지 표시
            model.addAttribute("error", "Invalid credentials");
            return "users/login/login";
        }
    }
    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        
    	new SecurityContextLogoutHandler().logout(request, response, null); // 로그아웃 처리
        
    	return "redirect:/users/login/login"; // 로그인 페이지로 리다이렉트
	}*/

	
	// 비밀번호 변경 폼
	@GetMapping("/change-password")
	public String changePasswordForm() {
		return "users/login/change-password";
	}
	
	// 비밀번호 변경 처리
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String oldPassword,
								 @RequestParam String newPassword,
								 @AuthenticationPrincipal User principal) {
		
		String userId = principal.getUsername();
		
		Users user = userService.findByUserId(userId);
		
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		
		// 기존 비밀번호가 일치하면 비밀번호 변경
		if(user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
			// 비밀번호 암호화 후 저장
			user.setPassword(passwordEncoder.encode(newPassword));
			userService.save(user);
		}else {
			return "users/login/change-password";
		}
		
		return "redirect:/users/profile/profile";
		
	}
	
	/*
	// 비밀번호 재설정 링크 요청 폼
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "users/login/forgot-password"; // 비밀번호 재설정 링크 요청 폼 반환
    }
    
    // 비밀번호 재설정 요청 처리
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String userId, Model model) {
        // 비밀번호 재설정 메일 보내기 처리 (SMTP나 외부 서비스 연동)
        Users user = userService.findByUserId(userId);
        if (user != null) {
            // 실제 비밀번호 재설정 메일 발송 로직을 작성 (SMTP 등)
            model.addAttribute("message", "비밀번호 재설정 링크를 이메일로 보냈습니다.");
        } else {
            model.addAttribute("error", "사용자를 찾을수 없습니다.");
        }
        return "users/login/forgot-password"; // 다시 폼을 반환
    }
   	*/
}
