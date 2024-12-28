package com.example.food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long fSeq; // 음식의 고유 번호 (기본 키)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 해당 음식을 저장한 유저

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfSeq", nullable = false)
    private SaveFood saveFood; // SaveFood 참조
}
