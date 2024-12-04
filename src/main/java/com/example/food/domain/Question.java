package com.example.food.domain;

import jakarta.persistence.*;
import lombok.*;
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
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq_generator")
    @SequenceGenerator(
            name = "question_seq_generator",   // 시퀀스 생성기의 이름
            sequenceName = "question_seq",     // 실제 데이터베이스 시퀀스 이름
            initialValue = 1,                // 시작 값
            allocationSize = 1               // 증가 크기
    )
    private Long qSeq; // 질문 시퀀스 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 질문을 한 유저

    private String title;   // 질문 제목
    private String content; // 질문 내용

    @CreationTimestamp              //  자동으로 시간을 적용
    @Column(updatable = false)
    private LocalDateTime regDate;  // 질문 작성일

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers; // 답변 리스트

}
