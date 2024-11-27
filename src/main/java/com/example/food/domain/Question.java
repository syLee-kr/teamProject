package com.example.food.domain;

import jakarta.persistence.*;
import lombok.*;
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
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qSeq; // 질문 시퀀스 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser; // 질문을 한 유저

    private String title;   // 질문 제목
    private String content; // 질문 내용

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date regDate; // 질문 작성일

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers; // 답변 리스트

    @PrePersist
    protected void onCreate() {
        this.regDate = new Date(); // 작성 시점에 현재 시간 설정
    }
}
