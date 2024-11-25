package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pseq;           //  게시글 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;    //  유저 정보

    @Column(columnDefinition = "varchar2(255) default null")
    private String imagePath;   //  게시물 이미지

    private String title;       //  제목
    private String content;     //  본문

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date postdate;      //  게시글 작성 일자

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int priority;                 // 공지 여부 (0: 일반 게시글, 1: 공지 게시물)

    @PrePersist
    protected void onCreate() {
        this.postdate = new Date();
    }
}