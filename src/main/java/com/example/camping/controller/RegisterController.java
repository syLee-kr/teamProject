package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.userService.UserService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class RegisterController {

    private final UserService userService;

    // 약관 페이지
    @GetMapping("/register/term")
    public String TermPage() {
    	return "users/login/term";
    }
    
    // 약관 동의 후 회원가입 페이지 이동
    @PostMapping("/register/term")
    public String TermsAgreement(@ModelAttribute("agree") Boolean agree) {
    	if (agree) {
    		// 약관에 동의한 경우, 
    		return "redirect:/users/login/register";
    	}else {
    		// 약관에 동의 하지 않은 경우, 
    		return "redirect:/";
    	}
    }
    
    // 회원가입 페이지를 보여주는 메서드
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new Users()); // 빈 User 객체를 모델에 추가
        return "users/login/register";  // 회원가입 양식 페이지 경로
    }

    // 회원가입 처리를 수행하는 메서드
    @PostMapping("/register")
    public String registerUser(@ModelAttribute Users user) {
        // 사용자 이름 중복 체크
        if (userService.usernameExists(user.getUserId())) {
            // 중복된 사용자 이름이 있으면 회원가입 페이지로 다시 리다이렉트
            return "redirect:/users/login/register?error";
        }

        // 사용자 등록
        userService.registerUser(user);
        return "redirect:/users/login/login";  // 회원가입 후 로그인 페이지로 리다이렉트
    }
}
