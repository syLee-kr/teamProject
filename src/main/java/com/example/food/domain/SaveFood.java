package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
public class SaveFood {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "savefood_seq_generator")
    @SequenceGenerator(
            name = "savefood_seq_generator",   // 시퀀스 생성기의 이름
            sequenceName = "savefood_seq",     // 실제 데이터베이스 시퀀스 이름
            initialValue = 1,                // 시작 값
            allocationSize = 1               // 증가 크기
    )
    private Long sfSeq; // 저장된 음식의 고유 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 유저 정보

    @CreationTimestamp              //  자동으로 시간을 적용
    @Column(updatable = false)
    private LocalDateTime saveDate; // 저장 날짜

    private String mealType; // 식사 유형: 아침, 점심, 저녁

    private String mainFood; // 메인 음식

    @ElementCollection
    @CollectionTable(name = "savefood_side_dishes", joinColumns = @JoinColumn(name = "fseq"))
    @Column(name = "side_dish")
    private List<String> sideDishes; // 사이드 메뉴 리스트

    private String dessert; // 디저트
    private String category; // 음식 카테고리
    private int calories; // 칼로리
    private double protein; // 단백질(g)
    private double carbohydrates; // 탄수화물(g)
    private double fat; // 지방(g)
}
