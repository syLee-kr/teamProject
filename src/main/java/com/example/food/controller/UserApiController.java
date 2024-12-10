package com.example.food.controller;

import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/submit-body-and-food-info")
    public ResponseEntity<Map<String, Object>> submitBodyAndFoodInfo(@RequestBody BodyAndFoodRequest request,
                                                                     @SessionAttribute("loginUser") Users loginUser) {
        try {
            // 신체 정보 업데이트
            Optional<Users> optionalUser = userRepository.findById(loginUser.getUserId());
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            Users user = optionalUser.get();
            user.setHeight(request.getHeight());
            user.setWeight(request.getWeight());
            userRepository.save(user);
            System.out.println("user = " + user);

            // 유저 정보에서 성별, 나이 추출
            String gender = loginUser.getGender().toString();
            System.out.println("gender = " + gender);
            int age = loginUser.getAge();
            System.out.println("age = " + age);
            // 전달 받은 식단 선호 정보
            String categoriesJson = String.format(
                    "{\"categories\": {\"category1\": %s, \"category2\": %s, \"category3\": %s, \"category4\": %s}}",
                    toJsonArray(request.getCategory1()),
                    toJsonArray(request.getCategory2()),
                    toJsonArray(request.getCategory3()),
                    toJsonArray(request.getCategory4())
            );

            // Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", "/scripts/random_food.py", categoriesJson
            );
            processBuilder.redirectErrorStream(true);
            System.out.println("processBuilder = " + processBuilder);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            System.out.println("output = " + output);
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(500).body(Map.of("error", "Python 스크립트 실행 오류"));
            }

            // 결과에 신체 정보와 나이도 함께 담아서 반환
            return ResponseEntity.ok(Map.of(
                    "result", output.toString(),
                    "gender", gender,
                    "age", age,
                    "bmr", request.getBmr(),
                    "bmi", request.getBmi()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // JSON 배열 변환 유틸리티
    private String toJsonArray(List<String> list) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(list);
    }

    public static class BodyAndFoodRequest {
        private double height;
        private double weight;
        private double bmr;
        private double bmi;
        private List<String> category1;
        private List<String> category2;
        private List<String> category3;
        private List<String> category4;
        private String selectedDate;

        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }

        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }

        public double getBmr() { return bmr; }
        public void setBmr(double bmr) { this.bmr = bmr; }

        public double getBmi() { return bmi; }
        public void setBmi(double bmi) { this.bmi = bmi; }

        public List<String> getCategory1() { return category1; }
        public void setCategory1(List<String> category1) { this.category1 = category1; }

        public List<String> getCategory2() { return category2; }
        public void setCategory2(List<String> category2) { this.category2 = category2; }

        public List<String> getCategory3() { return category3; }
        public void setCategory3(List<String> category3) { this.category3 = category3; }

        public List<String> getCategory4() { return category4; }
        public void setCategory4(List<String> category4) { this.category4 = category4; }

        public String getSelectedDate() { return selectedDate; }
        public void setSelectedDate(String selectedDate) { this.selectedDate = selectedDate; }
    }
}
