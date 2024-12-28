package com.example.food.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "menu") // 테이블 이름 명시
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    @Column(name = "menu_seq")
    private Long menuSeq; // 메뉴 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_food_id", nullable = false)
    @ToString.Exclude // 순환 참조 방지
    @JsonBackReference // JSON 직렬화 시 순환 참조 방지
    private SaveFood saveFood; // 해당 메뉴가 속한 식단

    @Column(name = "name", nullable = false)
    private String name; // 메뉴 이름

    @Column(name = "weight", nullable = false)
    private Double gram; // 중량 (g)

    @Column(name = "protein", nullable = true)
    private Double protein; // 총 단백질 (g)

    @Column(name = "carbohydrates", nullable = true)
    private Double carbohydrates; // 총 탄수화물 (g)

    @Column(name = "fat", nullable = true)
    private Double fat; // 총 지방 (g)

    @Column(name = "calories", nullable = false)
    private Double calories; // 총 칼로리
}
