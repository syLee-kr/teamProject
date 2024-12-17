package com.example.food.domain;

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
public class SaveFood {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "savefood_seq_generator")
    @SequenceGenerator(
            name = "savefood_seq_generator",
            sequenceName = "savefood_seq",
            initialValue = 1,
            allocationSize = 1
    )
    private Long sfSeq; // 식단 고유 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // 순환 참조 방지
    @JsonBackReference // JSON 직렬화 시 순환 참조 방지
    private Users user; // 유저 정보

    @Setter
    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime saveDate; // 저장 날짜

    @Column(nullable = false)
    private String mealType; // 식사 유형: 아침, 점심, 저녁

    @OneToMany(mappedBy = "saveFood", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // 순환 참조 방지
    @JsonManagedReference // JSON 직렬화 시 순환 참조 방지
    private List<Menu> menus = new ArrayList<>(); // 한 끼에 포함된 메뉴 리스트
}
