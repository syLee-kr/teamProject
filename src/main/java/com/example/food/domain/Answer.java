package com.example.food.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_seq_generator")
    @SequenceGenerator(
            name = "answer_seq_generator",   // 시퀀스 생성기의 이름
            sequenceName = "answer_seq",     // 실제 데이터베이스 시퀀스 이름
            initialValue = 1,                // 시작 값
            allocationSize = 1               // 증가 크기
    )
    private Long aSeq; // 답변 시퀀스 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 해당 답변의 질문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Users admin; // 답변을 작성한 관리자

    private String content; // 답변 내용

    @Column(updatable = false)
    @CreationTimestamp
    private OffsetDateTime regDate;
}
