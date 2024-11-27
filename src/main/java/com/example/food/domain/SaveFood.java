package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fseq; // 저장된 음식의 고유 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser; // 유저 정보

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date saveDate; // 저장 날짜

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

    @PrePersist
    protected void onCreate() {
        this.saveDate = new Date(); // 저장 시 현재 시간 설정
    }
}
