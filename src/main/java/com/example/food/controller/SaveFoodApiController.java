package com.example.food.controller;

import com.example.food.domain.Menu;
import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import com.example.food.repository.SaveFoodRepository;
import com.example.food.repository.UserRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/savefood")
public class SaveFoodApiController {

    private final SaveFoodRepository saveFoodRepository;
    private final UserRepository userRepository;

    public SaveFoodApiController(SaveFoodRepository saveFoodRepository, UserRepository userRepository) {
        this.saveFoodRepository = saveFoodRepository;
        this.userRepository = userRepository;
    }

    // 모든 SaveFood 목록 조회
    @GetMapping
    public ResponseEntity<List<SaveFood>> getAllSaveFoods() {
        return ResponseEntity.ok(saveFoodRepository.findAll());
    }

    // SaveFood 생성
    @PostMapping
    public ResponseEntity<SaveFood> createSaveFood(@RequestBody CreateSaveFoodRequest request) {
        Users byUserId = userRepository.findByUserId(request.getUserId());
        if (byUserId == null) {
            return ResponseEntity.badRequest().build();
        }
        OffsetDateTime saveDate;
        try {
            saveDate = OffsetDateTime.parse(request.getSaveDate()); // ISO-8601 형식의 날짜 문자열
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build(); // 잘못된 날짜 형식일 경우 400 Bad Request
        }

        // SaveFood 엔티티 생성
        SaveFood saveFood = SaveFood.builder()
                .user(byUserId)
                .saveDate(saveDate)
                .mealType(request.getMealType())
                .build();

        // Menu 엔티티 생성 및 SaveFood와 연관 설정
        List<Menu> menus = new ArrayList<>();
        if (request.getMenus() != null) {
            for (CreateMenuRequest menuRequest : request.getMenus()) {
                Menu menu = new Menu();
                menu.setName(menuRequest.getName());
                menu.setWeight(menuRequest.getWeight());
                menu.setProtein(menuRequest.getProtein());
                menu.setCarbohydrates(menuRequest.getCarbohydrates());
                menu.setFat(menuRequest.getFat());
                menu.setSaveFood(saveFood); // SaveFood와의 연관 설정
                menus.add(menu);
            }
        }
        saveFood.setMenus(menus); // SaveFood에 메뉴 설정

        // SaveFood 저장
        SaveFood saved = saveFoodRepository.save(saveFood);

        return ResponseEntity.ok(saved);
    }

    // SaveFood 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSaveFood(@PathVariable Long id) {
        Optional<SaveFood> optional = saveFoodRepository.findById(id);
        if (!optional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        saveFoodRepository.delete(optional.get());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreateSaveFoodRequest {
        private String userId;
        private String saveDate;
        private String mealType;
        private List<CreateMenuRequest> menus;
    }

    @Data
    public static class CreateMenuRequest {
        private String name;
        private double weight;
        private double protein;
        private double carbohydrates;
        private double fat;
    }
}
