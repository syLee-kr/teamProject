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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "food_seq_generator")
    @SequenceGenerator(
            name = "food_seq_generator",   // 시퀀스 생성기의 이름
            sequenceName = "food_seq",     // 실제 데이터베이스 시퀀스 이름
            initialValue = 1,                // 시작 값
            allocationSize = 1               // 증가 크기
    )
    private Long fSeq; // 음식의 고유 번호 (기본 키)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 해당 음식을 저장한 유저

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfSeq", nullable = false)
    private SaveFood saveFood; // SaveFood 참조
}
