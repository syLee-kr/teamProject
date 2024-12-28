package com.example.food.controller;

import com.example.food.service.MainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/main")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final MainService mainService;

    @Autowired
    public MainController(MainService mainService) {
        this.mainService = mainService;
        logger.info("MainController가 초기화되었습니다.");
    }

    @GetMapping
    public String mainPage(Model model) {
        logger.info("GET /main 요청이 들어왔습니다.");
        mainService.handleGetMain(model);
        return "user/main/main";
    }
}
