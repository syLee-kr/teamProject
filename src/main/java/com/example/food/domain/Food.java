package com.example.food.domain;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int foodSeq; // 음식의 고유 번호 (기본 키)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser; // 해당 음식을 저장한 유저

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fseq", nullable = false)
    private SaveFood saveFood; // SaveFood 참조
}
