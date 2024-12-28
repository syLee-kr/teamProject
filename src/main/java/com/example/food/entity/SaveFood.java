package com.example.food.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
@Table(name = "save_food") // 테이블 이름 명시
public class SaveFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    @Column(name = "sf_seq")
    private Long sfSeq; // 식단 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Users user; // 유저 정보

    @Setter
    @Column(name = "save_date", updatable = false, columnDefinition = "DATETIME(6)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime saveDate; // 저장 날짜

    @Column(name = "meal_type", nullable = false)
    private String mealType; // 식사 유형: 아침, 점심, 저녁

    @OneToMany(mappedBy = "saveFood", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<Menu> menus = new ArrayList<>(); // 한 끼에 포함된 메뉴 리스트
}
