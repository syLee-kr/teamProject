package com.example.food.service;

import com.example.food.entity.SaveFood;
import com.example.food.entity.Users;
import com.example.food.service.savefood.SaveFoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
public class MainService {

    private static final Logger logger = LoggerFactory.getLogger(MainService.class);

    private final SaveFoodService saveFoodService;
    private final AuthenticationService authService;

    public MainService(SaveFoodService saveFoodService, AuthenticationService authService) {
        this.saveFoodService = saveFoodService;
        this.authService = authService;
    }

    public void handleGetMain(Model model) {
        Users user = authService.getLoggedInUser();
        if (user == null) {
            logger.info("사용자가 로그인하지 않았습니다. 메인 페이지에 로그인 상태 없음 표시.");
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("user", null);
            model.addAttribute("saveFoods", null);
            return;
        }

        logger.info("사용자 ID: {}로 사용자 정보를 성공적으로 조회했습니다.", user.getUserId());
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("user", user);
        populateUserData(user, model);
    }

    private void populateUserData(Users user, Model model) {
        try {
            // 저장된 음식 조회
            List<SaveFood> saveFoods = saveFoodService.getUserFood(user.getUserId());
            saveFoods.forEach(saveFood -> {
                saveFood.getMenus().forEach(menu -> {
                    logger.debug("메뉴 정보 - Name: {}, Calories: {}, Protein: {}", menu.getName(), menu.getCalories(), menu.getProtein());
                });
            });

            model.addAttribute("saveFoods", saveFoods); // 저장된 식단 전체 전달
            logger.info("사용자 ID: {}의 저장된 음식 목록을 성공적으로 조회했습니다. 음식 개수: {}", user.getUserId(), saveFoods.size());
        } catch (Exception e) {
            logger.error("사용자 ID: {}의 저장된 음식 목록 조회 중 오류 발생: {}", user.getUserId(), e.getMessage());
            model.addAttribute("saveFoods", null);
        }
    }

}
