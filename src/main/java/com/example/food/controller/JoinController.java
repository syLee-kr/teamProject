package com.example.food.controller;

import com.example.food.entity.Users;
import com.example.food.service.userservice.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class JoinController {

    private static final Logger logger = LoggerFactory.getLogger(JoinController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public JoinController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        logger.info("JoinController가 초기화되었습니다.");
    }

    @GetMapping("/join")
    public String showJoinPage(Model model) {
        logger.info("회원가입 페이지에 접근했습니다.");
        model.addAttribute("users", new Users()); // 빈 Users 객체 전달
        return "user/login/join"; // 회원가입 페이지로 이동
    }

    @GetMapping("/api/join/check-duplicate-id")
    @ResponseBody
    public Map<String, Boolean> checkDuplicateId(@RequestParam String userId) {
        logger.info("중복 ID 확인 요청: {}", userId);
        boolean exists = userService.findById(userId) != null;
        logger.info("사용자 ID '{}' 존재 여부: {}", userId, exists);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return response;
    }

    @PostMapping("/join")
    public String signUpAction(@ModelAttribute Users user, RedirectAttributes redirectAttributes) {
        logger.info("회원가입 시도: 사용자 ID = {}", user.getUserId());

        if (userService.findById(user.getUserId()) != null) {
            logger.warn("회원가입 실패: 중복된 사용자 ID '{}'", user.getUserId());
            redirectAttributes.addFlashAttribute("loginFail", true);
            redirectAttributes.addFlashAttribute("errorMessage", "중복된 아이디입니다. 다시 시도해 주세요.");
            return "redirect:/login";
        }

        try {
            String pwd = passwordEncoder.encode(user.getPassword());
            user.setPassword(pwd);
            userService.newUser(user);
            logger.info("회원가입 성공: 사용자 ID = {}", user.getUserId());
            redirectAttributes.addFlashAttribute("message", "회원 가입이 완료되었습니다. 다시 로그인 해주세요.");
            return "redirect:/login"; // 회원가입 성공 후 로그인 페이지로 이동
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생: 사용자 ID = {}, 오류 메시지 = {}", user.getUserId(), e.getMessage());
            redirectAttributes.addFlashAttribute("loginFail", true);
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 문제가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/login"; // 회원가입 페이지로 다시 이동
        }
    }
}
