package com.example.food.controller;

import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping
public class ProfileController {

    private final UserRepository userRepo;

    @Autowired
    public ProfileController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/profile")
    public String profileMain(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "login/login";
        }

        if (user.getRegdate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedRegDate = user.getRegdate().toLocalDateTime().format(formatter);
            model.addAttribute("formattedRegDate", formattedRegDate);
        }

        model.addAttribute("user", user);
        return "profile/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfile(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("user");
        model.addAttribute("user", user);
        return "profile/edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfileSubmit(Users vo,HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (vo.getEmail() != null) {
            user.setEmail(vo.getEmail());
        }
        if (vo.getBirthday() != null) {
            user.setBirthday(vo.getBirthday());
        }
        if (vo.getPhone() != null) {
            user.setPhone(vo.getPhone());
        }
        if (vo.getAddress() != null) {
            user.setAddress(vo.getAddress());
        }
        userRepo.save(user);
        session.setAttribute("user", user);
        return "profile/profile";
    }
}
