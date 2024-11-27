package com.example.food.domain;

import jakarta.persistence.*;
import lombok.*;
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
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aSeq; // 답변 시퀀스 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 해당 답변의 질문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private AppUser admin; // 답변을 작성한 관리자

    private String content; // 답변 내용

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date regDate; // 답변 작성일

    @PrePersist
    protected void onCreate() {
        this.regDate = new Date(); // 작성 시점에 현재 시간 설정
    }
}
