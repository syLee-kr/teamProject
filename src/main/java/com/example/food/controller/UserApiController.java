package com.example.food.controller;

import com.example.food.entity.Menu;
import com.example.food.entity.SaveFood;
import com.example.food.entity.Users;
import com.example.food.repository.UserRepository;
import com.example.food.service.AuthenticationService;
import com.example.food.service.savefood.SaveFoodService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Logger logger = LoggerFactory.getLogger(UserApiController.class);

    private final UserRepository userRepository;
    private final SaveFoodService saveFoodService;
    private final AuthenticationService authService;

    @Autowired
    public UserApiController(UserRepository userRepository, SaveFoodService saveFoodService, AuthenticationService authService) {
        this.userRepository = userRepository;
        this.saveFoodService = saveFoodService;
        this.authService = authService;
    }

    @GetMapping("/foodInfo")
    public ResponseEntity<List<SaveFood>> getUserSaveFoods() {
        logger.info("GET /api/users/foodInfo 요청이 들어왔습니다.");

        Users user = authService.getLoggedInUser();
        if (user == null) {
            logger.warn("인증되지 않은 사용자의 요청입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("사용자 ID: {}의 저장된 음식 정보를 조회합니다.", user.getUserId());
        List<SaveFood> saveFoods = saveFoodService.getUserFood(user.getUserId());
        logger.info("사용자 ID: {}의 저장된 음식 정보 개수: {}", user.getUserId(), saveFoods.size());
        return ResponseEntity.ok(saveFoods);
    }

    @PostMapping("/foodInfo")
    public ResponseEntity<Map<String, Object>> submitBodyAndFoodInfo(
            @RequestBody @Valid FoodRequest request,
            BindingResult bindingResult) {
        logger.info("POST /api/users/foodInfo 요청이 들어왔습니다.");

        if (bindingResult.hasErrors()) {
            logger.warn("유효성 검사 실패: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(Map.of("error", "유효하지 않은 입력 데이터"));
        }

        try {
            Users user = authService.getLoggedInUser();
            if (user == null) {
                logger.warn("인증되지 않은 사용자의 요청입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "사용자가 인증되지 않았습니다."));
            }

            user.setHeight(request.getHeight());
            user.setWeight(request.getWeight());
            userRepository.save(user);
            logger.info("사용자 정보 업데이트 완료: {}", user);

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
            logger.debug("Categories JSON: {}", categoriesJson);

            String scriptResult = executePythonScript(categoriesJson);
            if (scriptResult == null || scriptResult.isEmpty()) {
                throw new RuntimeException("Python 스크립트 실행 결과가 비어 있습니다.");
            }

            JsonNode rootNode = mapper.readTree(scriptResult);
            if (!rootNode.path("status").asText().equals("success")) {
                throw new RuntimeException("Python 스크립트 실행 실패: " + rootNode.path("status").asText());
            }

            JsonNode dataNode = rootNode.path("data");
            List<SaveFood> saveFoods = processAndSaveFoodData(user, dataNode, request.getSelectedDate());
            logger.info("추천 식단 저장 완료. 저장된 식단 수: {}", saveFoods.size());

            return ResponseEntity.ok(Map.of(
                    "savedFoods", saveFoods,
                    "message", "추천 식단 저장이 완료되었습니다."
            ));

        } catch (Exception e) {
            logger.error("POST /api/users/foodInfo 처리 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Python 스크립트를 실행하고 결과를 반환합니다.
     *
     * @param inputJson 스크립트에 전달할 JSON 입력
     * @return 스크립트의 stdout 결과 문자열 또는 null
     */
    private String executePythonScript(String inputJson) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File scriptFile = new File(classLoader.getResource("scripts/random_food.py").getFile());
            String scriptPath = scriptFile.getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    scriptPath
            );
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.redirectErrorStream(false); // stderr와 stdout을 분리하여 읽기

            logger.info("Starting Python script: {}", scriptPath);
            Process process = processBuilder.start();

            // JSON을 Python 스크립트의 stdin으로 전달
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(inputJson);
                writer.flush();
                logger.info("Sent categories JSON to Python script.");
            } catch (IOException e) {
                logger.error("Failed to send data to Python script.", e);
                process.destroy();
                throw new RuntimeException("Failed to send data to Python script");
            }

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            // stdout 읽기 스레드
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading stdout from Python script.", e);
                }
            });

            // stderr 읽기 스레드
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading stderr from Python script.", e);
                }
            });

            stdoutThread.start();
            stderrThread.start();

            // 스크립트 실행 완료 대기 (타임아웃 10초)
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                logger.error("Python script timed out.");
                process.destroy();
                throw new RuntimeException("Python script timed out.");
            }

            // 스레드가 완료될 때까지 대기
            stdoutThread.join();
            stderrThread.join();

            if (process.exitValue() != 0) {
                throw new RuntimeException("Python script error: " + stderr.toString());
            }

            return stdout.toString().trim();

        } catch (Exception e) {
            logger.error("Python 스크립트 실행 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Python 스크립트의 결과를 처리하고 저장합니다.
     *
     * @param user        현재 로그인한 사용자
     * @param dataNode    Python 스크립트에서 반환한 데이터 노드
     * @param selectedDate 선택한 날짜
     * @return 저장된 SaveFood 목록
     */
    private List<SaveFood> processAndSaveFoodData(Users user, JsonNode dataNode, String selectedDate) {
        List<SaveFood> saveFoods = new ArrayList<>();

        LocalDate parsedDate = LocalDate.parse(selectedDate);

        for (String mealType : List.of("breakfast", "lunch", "dinner")) {
            JsonNode mealNode = dataNode.path(mealType);
            if (mealNode.isArray()) {
                SaveFood saveFood = new SaveFood();
                saveFood.setUser(user);
                saveFood.setMealType(mealType);
                saveFood.setSaveDate(parsedDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime());

                List<Menu> menus = new ArrayList<>();
                for (JsonNode item : mealNode) {
                    Menu menu = new Menu();
                    menu.setName(item.path("식품명").asText());
                    menu.setGram(item.path("식품중량").asDouble(0.0));
                    menu.setCalories(item.path("총 에너지(kcal)").asDouble(0.0));
                    menu.setProtein(item.path("총 단백질(g)").asDouble(0.0));
                    menu.setCarbohydrates(item.path("총 탄수화물(g)").asDouble(0.0));
                    menu.setFat(item.path("총 지방(g)").asDouble(0.0));
                    menu.setSaveFood(saveFood);
                    menus.add(menu);
                }

                saveFood.setMenus(menus);
                saveFoodService.saveSaveFood(saveFood);
                saveFoods.add(saveFood);
                logger.info("Saved food for mealType {}: {} items", mealType, menus.size());
            }
        }

        return saveFoods;
    }

    @DeleteMapping("/foodInfo/{sfSeq}")
    @Transactional
    public ResponseEntity<String> deleteFoodInfo(@PathVariable Long sfSeq) {
        logger.info("DELETE /api/users/foodInfo/{} 요청이 들어왔습니다.", sfSeq);
        try {
            saveFoodService.deleteSaveFood(sfSeq);
            logger.info("식단 정보 삭제 성공: sfSeq = {}", sfSeq);
            return ResponseEntity.ok("식단이 삭제되었습니다.");
        } catch (EmptyResultDataAccessException e) {
            logger.warn("식단 정보 삭제 실패: 존재하지 않는 sfSeq = {}", sfSeq);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("식단 정보가 존재하지 않습니다.");
        } catch (Exception e) {
            logger.error("식단 정보 삭제 중 오류 발생: sfSeq = {}, 오류 메시지 = {}", sfSeq, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("식단 삭제 실패: " + e.getMessage());
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
}
