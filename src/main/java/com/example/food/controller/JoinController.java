package com.example.food.controller;

import com.example.food.domain.Users;
import com.example.food.service.userservice.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class JoinController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public JoinController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/join")
    public String showJoinPage(Model model) {
        model.addAttribute("users", new Users()); // 빈 Users 객체 전달
        return "login/join"; // 회원가입 페이지로 이동
    }

    @GetMapping("/api/join/check-duplicate-id")
    @ResponseBody
    public Map<String, Boolean> checkDuplicateId(@RequestParam String userId) {
        boolean exists = userService.findById(userId) != null;
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return response;
    }
    @PostMapping("/signup")
    public String signUpAction(@ModelAttribute Users user, Model model) {
        try {
            String pwd = passwordEncoder.encode(user.getPassword());
            user.setPassword(pwd);
            userService.newUser(user);
            return "redirect:/login"; // 회원가입 성공 후 로그인 페이지로 이동
        } catch (Exception e) {
            model.addAttribute("errorMessage", "회원가입 중 문제가 발생했습니다. 다시 시도해주세요.");
            return "login/join"; // 회원가입 페이지로 다시 이동
        }
    }
}
