package com.example.food.controller;

import com.example.food.CodeGenerator;
import com.example.food.entity.Users;
import com.example.food.service.AuthenticationService;
import com.example.food.service.EmailService;
import com.example.food.service.userservice.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authService;

    @Autowired
    public LoginController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder, AuthenticationService authService) {
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        logger.info("LoginController가 초기화되었습니다.");
    }

    private String getErrorMessage(String error) {
        switch (error) {
            case "invalid_credentials":
                return "잘못된 사용자 이름 또는 비밀번호입니다.";
            case "account_disabled":
                return "계정이 비활성화되어 있습니다. 관리자에게 문의하세요.";
            case "oauth_error":
                return "소셜 로그인 중 오류가 발생했습니다.";
            default:
                return "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.";
        }
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (authService.isAuthenticated()) {
            logger.info("인증된 사용자가 감지되어 /main으로 리다이렉트합니다.");
            return "redirect:/main";
        }

        if (logout != null) {
            model.addAttribute("message", "성공적으로 로그아웃되었습니다.");
        }

        if (error != null) {
            logger.warn("로그인 오류가 발생했습니다.");
            String errorMessage = getErrorMessage(error);
            model.addAttribute("loginFail", true);
            model.addAttribute("errorMessage", errorMessage);
        }

        return "user/login/login";
    }

    // 아이디 및 비밀번호 찾기 페이지
    @GetMapping("/find")
    public String findIdView() {
        logger.info("아이디/비밀번호 찾기 페이지에 접근했습니다.");
        return "user/login/find";
    }

    // 아이디 찾기
    @PostMapping("/findId")
    @ResponseBody
    public Map<String, Object> findIdAction(@RequestParam("name") String name, @RequestParam("phone") String phone) {
        logger.info("아이디 찾기 요청: 이름={}, 전화번호={}", name, phone);
        Users user = userService.getUserByNameAndPhone(name, phone);

        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            logger.info("사용자가 발견되었습니다: 이름={}, 사용자 ID={}", name, user.getUserId());
            response.put("message", 1);
            response.put("userId", user.getUserId());
        } else {
            logger.warn("사용자를 찾을 수 없습니다: 이름={}, 전화번호={}", name, phone);
            response.put("message", -1);
        }
        return response;
    }

    // 비밀번호 찾기
    @PostMapping("/findPwd")
    public String findPwdAction(@RequestParam("userId") String userId, @RequestParam("phone") String phone, Model model) {
        logger.info("비밀번호 찾기 요청: 사용자 ID={}, 전화번호={}", userId, phone);
        Users user = userService.getUserByIdAndPhone(userId, phone);

        if (user == null) {
            logger.warn("사용자를 찾을 수 없습니다: 사용자 ID={}, 전화번호={}", userId, phone);
            model.addAttribute("message", "잘못된 사용자 ID 또는 전화번호입니다.");
            return "user/login/login";
        }

        try {
            String temporaryPassword = CodeGenerator.generateCode(6);
            user.setPassword(passwordEncoder.encode(temporaryPassword));
            userService.changeUser(user);
            emailService.sendVerificationCode(user.getUserId(), temporaryPassword);
            logger.info("임시 비밀번호가 사용자에게 전송되었습니다: 사용자 ID={}", userId);
            model.addAttribute("message", "임시 비밀번호가 휴대폰으로 전송되었습니다.");
        } catch (Exception e) {
            logger.error("비밀번호 재설정 처리 중 오류 발생: 사용자 ID={}, 오류={}", userId, e.getMessage());
            model.addAttribute("message", "비밀번호 재설정 중 오류가 발생했습니다. 다시 시도해주세요.");
        }

        return "user/login/login";
    }
}
