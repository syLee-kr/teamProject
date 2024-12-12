package com.example.food.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
//  게시글 객체
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq_generator")
    @SequenceGenerator(
            name = "post_seq_generator",   // 시퀀스 생성기의 이름
            sequenceName = "post_seq",     // 실제 데이터베이스 시퀀스 이름
            initialValue = 1,                // 시작 값
            allocationSize = 1               // 증가 크기
    )
    private Long pSeq;           //  게시글 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", nullable = false)
    private Users user;    //  유저 정보
    
    @Column(columnDefinition = "varchar2(255) default null")
    private String imagePath;   //  게시물 이미지

    private String title;       //  제목
    private String content;     //  본문

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // 부모 필드
    private List<Comments> comments = new ArrayList<>();

    @CreationTimestamp              //  자동으로 시간을 적용
    @Column(updatable = false)
    private LocalDateTime postdate; // 게시글 작성 시간
    
    @Column(nullable = false)
    private boolean isNotice; // 공지 여부

    @Column(nullable = false)
    private int priority;    // 게시물 우선순위(isNotice가 true인 경우 공지글 우선순위/ false인 경우 일반 게시글 우선순위) 
    
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Long cnt; // 조회수
}