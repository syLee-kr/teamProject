package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_seq_generator")
    @SequenceGenerator(
            name = "menu_seq_generator",
            sequenceName = "menu_seq",
            initialValue = 1,
            allocationSize = 1
    )
    private Long menuSeq; // 메뉴 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_food_id", nullable = false)
    private SaveFood saveFood; // 해당 메뉴가 속한 식단

    private String name; // 메뉴 이름

    private double weight; // 중량 (g)
    private double protein; // 단백질 (g)
    private double carbohydrates; // 탄수화물 (g)
    private double fat; // 지방 (g)
}
