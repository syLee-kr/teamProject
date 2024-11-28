package com.example.food.controller;

import com.example.food.service.userservice.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UserService us;

    @Autowired
    public LoginController(UserService us) {
        this.us = us;
    }

    @GetMapping
    public String login() {
        return "login/login";
    }

    @PostMapping("/main")
    public String loginSummit(@RequestParam() String userId, @RequestParam String password,HttpSession session) {
        if (us.getUser(userId).getPassword().equals(password)) {
            session.setAttribute("user", us.getUser(userId));
            return "main/main";
        } else {
            return "login/login";
        }
    }
}
