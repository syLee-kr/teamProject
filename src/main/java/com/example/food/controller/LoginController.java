package com.example.food.controller;

import com.example.food.CodeGenerator;
import com.example.food.domain.Users;
import com.example.food.service.EmailService;
import com.example.food.service.userservice.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

@Slf4j
@Controller
@RequestMapping
public class LoginController {

    private final UserService us;
    private final EmailService es;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LoginController(UserService us, EmailService es, PasswordEncoder passwordEncoder) {
        this.us = us;
        this.es = es;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        Object loginUser = session.getAttribute("user");

        if (loginUser != null) {
            // 사용자가 이미 로그인되어 있으면 메인 페이지로 리다이렉트
            return "redirect:/main";
        }
        // 사용자가 로그인하지 않았으면 로그인 페이지 표시
        log.info("사용자가 로그인되지 않았습니다.");
        return "login/login";
    }

    @PostMapping("/login")
    public String loginAction(
            @RequestParam String userId,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // 사용자 정보 조회
        Users user = us.getUser(userId);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 비밀번호 일치 시 세션에 사용자 저장
            session.setAttribute("user", user);
            log.info("사용자 로그인 성공: {}", user.getUserId());  // SLF4J 사용
            return "redirect:/main";
        } else {
            // 로그인 실패 메시지 설정
        	log.warn("로그인 실패: 잘못된 아이디 또는 비밀번호 (아이디: {})", userId);  // SLF4J 사용
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "login/login";
        }
    }

    // 회원가입 화면 표시
    @PostMapping("/join")
    public String joinView() {
        return "login/join";
    }

    // 아이디,비밀번호 찾기 화면 표시
    @GetMapping("/find")
    public String findIdView() {
        return "login/find";
    }

    // 아이디 찾기 처리
    @PostMapping("/findId")
    public String findIdAction(Users vo, Model model) {
        Users users = us.getUser(vo.getName());

        if (users != null) { // 아이디 조회 성공
            model.addAttribute("message", 1);
            model.addAttribute("userId", users.getUserId());
        } else {
            model.addAttribute("message", -1);
        }
        return "login/findResult";
    }

    @PostMapping("/findPwd")
    public String findPwdAction(Users vo, Model model, HttpSession session) {
        Users user = us.getUser(vo.getUserId());

        if (user != null && user.getEmail().equals(vo.getEmail())) {
            // 인증 코드 생성
            String verificationCode = CodeGenerator.generateCode(6);

            // 인증 코드를 세션에 저장 (추후 검증을 위해)
            session.setAttribute("verificationCode", verificationCode);
            session.setAttribute("userId", user.getUserId());

            // 인증 코드를 이메일로 전송
            es.sendVerificationCode(user.getEmail(), verificationCode);

            // 성공 메시지와 사용자 ID를 모델에 추가
            model.addAttribute("message", 1);
            model.addAttribute("userId", user.getUserId());
        } else {
            // 사용자 조회 실패 시 메시지 추가
            model.addAttribute("message", -1);
        }
        return "login/findPwdResult";
    }

    @GetMapping("/logout")
    public String logoutSubmit(HttpSession session) {
        session.invalidate(); // 세션 무효화
        log.info("사용자가 로그아웃됨.");  // SLF4J 사용
        return "redirect:/login";
    }
}
