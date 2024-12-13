package com.example.food.controller;

import com.example.food.domain.Menu;
import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import com.example.food.service.savefood.SaveFoodService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserRepository userRepository;
    private final SaveFoodService saveFoodService;

    @Autowired
    public UserApiController(UserRepository userRepository, SaveFoodService saveFoodService) {
        this.userRepository = userRepository;
        this.saveFoodService = saveFoodService;
    }
    @GetMapping("/foodInfo")
    public ResponseEntity<List<SaveFood>> getUserSaveFoods(@SessionAttribute Users user) {
        List<SaveFood> saveFoods = saveFoodService.getUserFood(user.getUserId());
        return ResponseEntity.ok(saveFoods);
    }

    @PostMapping("/foodInfo")
    public ResponseEntity<Map<String, Object>> submitBodyAndFoodInfo(@RequestBody FoodRequest request,
                                                                     @SessionAttribute Users user) {
        try {
            Optional<Users> optionalUser = userRepository.findById(user.getUserId());
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            Users users = optionalUser.get();
            users.setHeight(request.getHeight());
            users.setWeight(request.getWeight());
            userRepository.save(users);

            String gender = users.getGender().toString();
            int age = users.getAge();

            String categoriesJson = String.format(
                    "{\"categories\": {\"category1\": \"%s\", \"category2\": \"%s\", \"category3\": \"%s\", \"category4\": \"%s\"}, " +
                            "\"bmr\": %.2f, \"selectedDate\": \"%s\"}",
                    request.getCategory1(),
                    request.getCategory2(),
                    request.getCategory3(),
                    request.getCategory4(),
                    request.getBmr(),
                    request.getSelectedDate() // 시간 정보 전달
            );

            // Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", "/scripts/random_food.py", categoriesJson
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(500).body(Map.of("error", "Python 스크립트 실행 오류"));
            }

            // Python 결과 처리
            String result = output.toString();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(result);

            List<Map<String, Object>> savedFoodsResponse = new ArrayList<>();

            String[] mealTypes = {"breakfast", "lunch", "dinner"};
            for (String mealType : mealTypes) {
                JsonNode mealNode = rootNode.get(mealType);
                if (mealNode != null && mealNode.isArray()) {
                    // SaveFood 엔티티 생성
                    SaveFood saveFood = new SaveFood();
                    saveFood.setUser(users);
                    saveFood.setSaveDate(OffsetDateTime.now());
                    saveFood.setMealType(mealType); // 식사 유형 설정

                    // 각 메뉴 처리
                    for (JsonNode foodNode : mealNode) {
                        Menu menu = new Menu();
                        menu.setName(foodNode.get("식품명").asText());
                        menu.setWeight(foodNode.get("식품중량").asDouble());
                        menu.setProtein(foodNode.get("총 단백질(g)").asDouble());
                        menu.setCarbohydrates(foodNode.get("총 탄수화물(g)").asDouble());
                        menu.setFat(foodNode.get("총 지방(g)").asDouble());
                        menu.setSaveFood(saveFood); // 관계 설정
                        saveFood.getMenus().add(menu); // SaveFood에 메뉴 추가
                    }

                    SaveFood savedFood = saveFoodService.saveSaveFood(saveFood);

                    Map<String, Object> saveFoodMap = new HashMap<>();
                    saveFoodMap.put("saveDate", savedFood.getSaveDate());
                    saveFoodMap.put("mealType", savedFood.getMealType());
                    saveFoodMap.put("menus", savedFood.getMenus());
                    savedFoodsResponse.add(saveFoodMap);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("savedFoods", savedFoodsResponse);
            response.put("gender", gender);
            response.put("age", age);
            response.put("bmr", request.getBmr());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/foodInfo/{sfSeq}")
    @Transactional
    public ResponseEntity<String> deleteFoodInfo(@PathVariable Long sfSeq) {
        try {
            saveFoodService.deleteSaveFood(sfSeq);
            return ResponseEntity.ok("식단이 삭제되었습니다.");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("식단 삭제 실패: " + e.getMessage());
        }
    }
    @Data
    public static class FoodRequest {
        private double height;
        private double weight;
        private double bmr;
        private List<String> category1;
        private List<String> category2;
        private List<String> category3;
        private List<String> category4;
        private String selectedDate;

    }
}

