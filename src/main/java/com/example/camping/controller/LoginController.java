package com.example.camping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.camping.domain.Users;
import com.example.camping.userService.UserService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class LoginController {
	
	private UserService userService;
	
	// 로그인 폼
	@GetMapping("/login")
	public String loginForm() {
		return "login";
	}
	
	// 로그인 요청 처리
	@PostMapping("/login")
	public String login(@RequestParam("userId") String userId,
						@RequestParam("password") String password,
						Model model) {
		Users user = userService.login(userId, password);
		/*
        if (user != null) {
            // 로그인 성공, 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            return "redirect:/dashboard"; // 로그인 후 대시보드 페이지로 리다이렉트
        } else {
            // 로그인 실패, 에러 메시지 표시
            model.addAttribute("error", "Invalid credentials");
            return "login"; // 로그인 폼을 다시 반환
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/login"; // 로그인 페이지로 리다이렉트
	}*/
	
	return null;
	}
	
}
