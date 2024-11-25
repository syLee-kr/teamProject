package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food") // 테이블 이름 설정
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 음식 고유 ID

    private String mainfood;    // 메인음식
    @ElementCollection // 사이드 메뉴 리스트
    @CollectionTable(name = "food_side_dishes", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "side_dish")
    private List<String> sideDishes; // 사이드 메뉴 리스트
    private String dessert;     // 디저트

    private String meal;        // 아침, 점심, 저녁 카테고리
    private String category;    // 음식 카테고리 (예: 한식, 중식, 양식 등)
    private int calories;       // 칼로리
    private double protein;     // 단백질(g)
    private double carbohydrates; // 탄수화물(g)
    private double fat;         // 지방(g)
}
