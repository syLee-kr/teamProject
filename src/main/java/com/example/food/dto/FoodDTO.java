package com.example.food.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Python에서 추천된 음식 데이터를 받기 위한 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDTO {

    private String mainFood;        // 메인 음식
    private List<String> sideDishes; // 사이드 메뉴 리스트
    private String dessert;         // 디저트

    private String mealType;        // 식사 유형: 아침, 점심, 저녁
    private String category;        // 음식 카테고리 (예: 한식, 중식, 양식 등)
    private int calories;           // 칼로리
    private double protein;         // 단백질(g)
    private double carbohydrates;   // 탄수화물(g)
    private double fat;             // 지방(g)
}
