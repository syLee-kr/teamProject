package com.example.food.controller;

import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import com.example.food.service.savefood.SaveFoodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping
public class MainController {

    private final SaveFoodService saveFoodService;

    @Autowired
    public MainController(SaveFoodService saveFoodService) {
        this.saveFoodService = saveFoodService;
    }
    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("loginUser");

        if (user == null) {
            user = Users.builder()
                    .userId("testUser123")
                    .password("password123")
                    .name("홍길동")
                    .email("test@example.com")
                    .phone("010-1234-5678")
                    .address("서울특별시 강남구")
                    .height(175.5)
                    .weight(70.0)
                    .birthday(new Date(90, Calendar.JANUARY, 1)) // 1990-01-01
                    .gender(Users.Gender.MALE)
                    .role(Users.Role.ROLE_USER)
                    .profileImage("images/default.png")
                    .build();

            session.setAttribute("loginUser", user);
        }

        // java.util.Date를 문자열로 포맷
        String formattedBirthday = new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday());
        model.addAttribute("user", user);
        model.addAttribute("formattedBirthday", formattedBirthday); // 포맷된 생일 추가

        return "main/main";
    }
//    @GetMapping("/main")
//    public String mainPage(HttpSession session, Model model) {
//        String user = session.getId();
//
//        if (user == null) {
//            return "login/login";
//        }
//        // SaveFood 데이터를 가져옵니다.
//
//        List<SaveFood> saveFoods = saveFoodService.getUserFood(user);
//
//        // 모델에 추가
//        model.addAttribute("user", user);
//        model.addAttribute("saveFoods", saveFoods);
//
//        return "main/main"; // main.html
//    }
    @GetMapping("/logout")
    public String logoutSubmit(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:login/login";
    }
}
