package com.example.food.controller;

import com.example.food.domain.Menu;
import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import com.example.food.service.savefood.SaveFoodService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    // ... 기존 import 문 및 클래스 정의 ...

    @PostMapping("/foodInfo")
    public ResponseEntity<Map<String, Object>> submitBodyAndFoodInfo(@RequestBody @Valid FoodRequest request,
                                                                     @SessionAttribute Users user,
                                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input data"));
        }

        try {
            System.out.println("Request received: " + request);

            // Step 1: 사용자 정보 조회
            Optional<Users> optionalUser = userRepository.findById(user.getUserId());
            if (!optionalUser.isPresent()) {
                System.out.println("User not found with ID: " + user.getUserId());
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            System.out.println("User found: " + optionalUser.get());

            Users users = optionalUser.get();
            users.setHeight(request.getHeight());
            users.setWeight(request.getWeight());
            userRepository.save(users);
            System.out.println("Updated user info: " + users);

            String gender = users.getGender().toString();
            int age = users.getAge();
            System.out.println("Gender: " + gender + ", Age: " + age);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> categoriesData = Map.of(
                    "categories", Map.of(
                            "category1", request.getCategory1(),
                            "category2", request.getCategory2(),
                            "category3", request.getCategory3(),
                            "category4", request.getCategory4()
                    ),
                    "selectedDate", request.getSelectedDate(),
                    "bmr", request.getBmr()
            );
            String categoriesJson = mapper.writeValueAsString(categoriesData);
            System.out.println("Categories JSON: " + categoriesJson);

            // Python 스크립트의 절대 경로 설정
            String scriptPath = Paths.get("src/main/resources/scripts/random_food.py").toAbsolutePath().toString();

            // Python 인터프리터 경로 확인 (환경에 따라 'python3' 또는 'python' 사용)
            String pythonInterpreter = "python";

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonInterpreter, scriptPath
            );

            // 작업 디렉토리 설정 (Python 스크립트가 위치한 디렉토리)
            processBuilder.directory(new File(Paths.get("src/main/resources/scripts").toAbsolutePath().toString()));

            // 환경 변수 설정: PYTHONIOENCODING을 UTF-8로 설정
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");

            // stderr와 stdout을 분리하여 읽기 위해 redirectErrorStream을 false로 설정
            processBuilder.redirectErrorStream(false);

            System.out.println("Starting Python script...");
            Process process = processBuilder.start();
            System.out.println("Python script started.");

            // Step 2: JSON을 Python 스크립트의 stdin으로 전달
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(categoriesJson);
                writer.flush();
                System.out.println("Sent categories JSON to Python script.");
            } catch (IOException e) {
                System.err.println("Failed to send data to Python script.");
                e.printStackTrace();
                process.destroy();
                return ResponseEntity.status(500).body(Map.of("error", "Failed to send data to Python script"));
            }

            // Step 3: Python 스크립트의 stdout과 stderr를 별도로 읽기
            StringBuilder stdoutOutput = new StringBuilder();
            StringBuilder stderrOutput = new StringBuilder();

            // stdout 읽기 스레드
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = stdoutReader.readLine()) != null) {
                        stdoutOutput.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // stderr 읽기 스레드
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = stderrReader.readLine()) != null) {
                        stderrOutput.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            stdoutThread.start();
            stderrThread.start();

            // Step 4: 기다리는 시간 제한 (예: 10초)
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("Python script timed out.");
                process.destroy();
                return ResponseEntity.status(500).body(Map.of("error", "Python script timed out"));
            }

            // Ensure both threads have finished
            stdoutThread.join();
            stderrThread.join();

            int exitCode = process.exitValue();
            System.out.println("Python script exit code: " + exitCode);
            System.out.println("Python script output (stdout): " + stdoutOutput.toString());
            System.out.println("Python script output (stderr): " + stderrOutput.toString());

            if (exitCode != 0) {
                String errorMessage = stderrOutput.toString();
                System.err.println("Python script error: " + errorMessage);
                return ResponseEntity.status(500).body(Map.of("error", "Python script execution failed: " + errorMessage));
            }

            // Step 5: Python 결과 처리
            String result = stdoutOutput.toString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(result);
            System.out.println("Parsed Python result: " + rootNode);

            List<Map<String, Object>> savedFoodsResponse = new ArrayList<>();
            String[] mealTypes = {"breakfast", "lunch", "dinner"};

            // 'data' 노드 아래에서 mealType을 가져옴
            JsonNode dataNode = rootNode.get("data");
            if (dataNode == null || !dataNode.isObject()) {
                System.err.println("Invalid JSON structure: 'data' node is missing or not an object");
                return ResponseEntity.status(500).body(Map.of("error", "Invalid JSON structure from Python script"));
            }

            // Step 6: SaveFood 생성 및 저장
            for (String mealType : mealTypes) {
                JsonNode mealNode = dataNode.get(mealType);
                if (mealNode != null && mealNode.isArray()) {
                    System.out.println("Processing mealType: " + mealType);

                    SaveFood saveFood = new SaveFood();
                    saveFood.setUser(users);
                    saveFood.setSaveDate(OffsetDateTime.now());
                    saveFood.setMealType(mealType);

                    // menus 리스트 초기화
                    if (saveFood.getMenus() == null) {
                        saveFood.setMenus(new ArrayList<>());
                    }

                    for (JsonNode foodNode : mealNode) {
                        Menu menu = new Menu();
                        menu.setName(foodNode.get("식품명").asText());

                        // 'gram' 처리
                        JsonNode gramNode = foodNode.get("식품중량");
                        if (gramNode != null && gramNode.isNumber()) {
                            menu.setGram(gramNode.asDouble());
                        } else {
                            menu.setGram(0.0); // 기본값 설정
                        }

                        // '총 에너지(kcal)' 처리
                        JsonNode energyNode = foodNode.get("총 에너지(kcal)");
                        if (energyNode != null && energyNode.isNumber()) {
                            menu.setCalories(energyNode.asDouble());
                        } else {
                            menu.setCalories(0.0); // 기본값 설정
                        }

                        // '총 탄수화물(g)' 처리
                        JsonNode carbNode = foodNode.get("총 탄수화물(g)");
                        if (carbNode != null && carbNode.isNumber()) {
                            menu.setCarbohydrates(carbNode.asDouble());
                        } else {
                            menu.setCarbohydrates(0.0); // 기본값 설정
                        }

                        // '총 단백질(g)' 처리
                        JsonNode proteinNode = foodNode.get("총 단백질(g)");
                        if (proteinNode != null && proteinNode.isNumber()) {
                            menu.setProtein(proteinNode.asDouble());
                        } else {
                            menu.setProtein(0.0); // 기본값 설정
                        }

                        // '총 지방(g)' 처리
                        JsonNode fatNode = foodNode.get("총 지방(g)");
                        if (fatNode != null && fatNode.isNumber()) {
                            menu.setFat(fatNode.asDouble());
                        } else {
                            menu.setFat(0.0); // 기본값 설정
                        }

                        menu.setSaveFood(saveFood);
                        saveFood.getMenus().add(menu);
                    }

                    // SaveFood 저장
                    SaveFood savedFood = saveFoodService.saveSaveFood(saveFood);
                    System.out.println("Saved food: " + savedFood);

                    // 저장된 음식 정보를 응답에 추가
                    Map<String, Object> saveFoodMap = new HashMap<>();
                    saveFoodMap.put("sfSeq", savedFood.getSfSeq());
                    saveFoodMap.put("saveDate", savedFood.getSaveDate());
                    saveFoodMap.put("mealType", savedFood.getMealType());
                    saveFoodMap.put("menus", savedFood.getMenus());
                    savedFoodsResponse.add(saveFoodMap);
                }
            }

            // Step 7: 최종 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("savedFoods", savedFoodsResponse);
            response.put("gender", gender);
            response.put("age", age);
            response.put("bmr", request.getBmr());
            System.out.println("Final response: " + response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Data
    public static class FoodRequest {
        private double height;
        private double weight;
        private double bmr;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
        private String selectedDate;
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
}

