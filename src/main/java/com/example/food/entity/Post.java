package com.example.food.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long pSeq; // 게시글 번호

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private Users user; // 유저 정보


    @ElementCollection
    @CollectionTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id")
    )
    // MySQL에서는 columnDefinition으로 "varchar(255)" 라고 표기 가능
    @Column(columnDefinition = "varchar(255) default null")
    private List<String> imagePaths = new ArrayList<>(); // 게시물 이미지 경로

    private String title;   // 제목
    private String content; // 본문

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comments> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime postdate; // 작성시간

    @Column(nullable = false)
    private Boolean isNotice = false; // 공지 여부

    @Column(nullable = false)
    private int priority; // 우선순위 (공지 시 우선순위, 일반 게시글 시 무관하게 사용 가능)

    // MySQL에서 Long(자바) <-> BIGINT(DB) 사용
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long cnt; // 조회수

    public String getFormattedPostdate() {
        if (this.postdate == null) {
            return "";
        }
        // 한국 시간대 (Asia/Seoul)로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return this.postdate.atZoneSameInstant(ZoneId.of("Asia/Seoul")).format(formatter);
    }
}
