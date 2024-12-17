package com.example.food.controller;

import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import com.example.food.service.savefood.SaveFoodService;
import com.example.food.service.userservice.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping
public class MainController {

    private final SaveFoodService saveFoodService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MainController(SaveFoodService saveFoodService, UserService userService, PasswordEncoder passwordEncoder) {
        this.saveFoodService = saveFoodService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    private String renderMainPage(Users user, Model model) {
        if (user == null) {
            model.addAttribute("isLoggedIn", false);
            return "main/main";
        }
        List<SaveFood> saveFoods = saveFoodService.getUserFood(user.getUserId());
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("user", user);
        model.addAttribute("saveFoods", saveFoods);

        return "main/main";
    }

    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("user");
        return renderMainPage(user, model);
    }

    @PostMapping("/main")
    public String mainPages(@RequestParam String userId, @RequestParam String password, HttpSession session, Model model) {
        Users user = userService.getUser(userId);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("user", user);
        }
        return renderMainPage(user, model);
    }

}
