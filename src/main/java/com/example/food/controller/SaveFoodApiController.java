package com.example.food.controller;

import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import com.example.food.repository.SaveFoodRepository;
import com.example.food.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @GetMapping
    public ResponseEntity<List<SaveFood>> getAllSaveFoods() {
        return ResponseEntity.ok(saveFoodRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<SaveFood> createSaveFood(@RequestBody CreateSaveFoodRequest request) {
        Optional<Users> optionalUser = userRepository.findById(request.getUserId());
        if (!optionalUser.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        SaveFood sf = SaveFood.builder()
                .user(optionalUser.get())
                .saveDate(LocalDateTime.parse(request.getSaveDate()+"T00:00:00")) // 날짜 문자열 -> LocalDateTime 처리 가정
                .mealType(request.getMealType())
                .mainFood(request.getMainFood())
                .sideDishes(request.getSideDishes())
                .dessert(request.getDessert())
                .category(request.getCategory())
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbohydrates(request.getCarbohydrates())
                .fat(request.getFat())
                .build();

        SaveFood saved = saveFoodRepository.save(sf);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSaveFood(@PathVariable Long id) {
        Optional<SaveFood> optional = saveFoodRepository.findById(id);
        if (!optional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        saveFoodRepository.delete(optional.get());
        return ResponseEntity.ok().build();
    }

    // 추천 식단 제공 (샘플 로직)
    @GetMapping("/recommendation")
    public ResponseEntity<Map<String, Object>> getRecommendedMeal(@RequestParam double bmr, @RequestParam double bmi) {
        // 여기서는 단순히 BMR, BMI 값을 바탕으로 임의 추천 식단을 반환하는 예시
        // 실제 로직: BMR, BMI에 따른 DB 쿼리나 알고리즘으로 식단 구성
        Map<String, Object> meal = new HashMap<>();
        meal.put("mealType", "점심");
        meal.put("mainFood", "샐러드");
        meal.put("sideDishes", Arrays.asList("닭가슴살", "아보카도"));
        meal.put("dessert", "과일");
        meal.put("category", "헬시");
        meal.put("calories", 450);
        meal.put("protein", 30.0);
        meal.put("carbohydrates", 40.0);
        meal.put("fat", 15.0);

        return ResponseEntity.ok(meal);
    }

    public static class CreateSaveFoodRequest {
        private String userId;
        private String saveDate;
        private String mealType;
        private String mainFood;
        private List<String> sideDishes;
        private String dessert;
        private String category;
        private int calories;
        private double protein;
        private double carbohydrates;
        private double fat;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getSaveDate() { return saveDate; }
        public void setSaveDate(String saveDate) { this.saveDate = saveDate; }

        public String getMealType() { return mealType; }
        public void setMealType(String mealType) { this.mealType = mealType; }

        public String getMainFood() { return mainFood; }
        public void setMainFood(String mainFood) { this.mainFood = mainFood; }

        public List<String> getSideDishes() { return sideDishes; }
        public void setSideDishes(List<String> sideDishes) { this.sideDishes = sideDishes; }

        public String getDessert() { return dessert; }
        public void setDessert(String dessert) { this.dessert = dessert; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }

        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }

        public double getCarbohydrates() { return carbohydrates; }
        public void setCarbohydrates(double carbohydrates) { this.carbohydrates = carbohydrates; }

        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }
    }
}
