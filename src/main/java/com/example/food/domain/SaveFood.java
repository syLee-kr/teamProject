package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

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
    private int fseq;           //  추천 음식 저장 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;    //  유저 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date savedate;

    @PrePersist
    protected void onCreate() {
        this.savedate = new Date(); // 저장 시점에 현재 시간 설정
    }
}
