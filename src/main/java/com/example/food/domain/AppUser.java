package com.example.food.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class AppUser {

    @Id
    private String userId;          // 유저 아이디
    private String password;        // 유저 비밀번호
    private String name;            // 유저 이름
    private String email;           // 유저 이메일
    private String phone;           // 유저 전화번호
    private String address;         // 유저 주소

    private double height;          // 키
    private double weight;          // 체중
    private Date birthday;          // 생일
    private int gender;             // 성별 (1: 남자, 2: 여자)

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions; // 유저가 작성한 질문 리스트

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers; // 관리자가 작성한 답변 리스트

    // 기존 코드 유지
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date regdate = new Date(); // 유저 가입일

    @Column(columnDefinition = "varchar2(255) default 'images/default.png'")
    private String profileImage; // 프로필 이미지

    @Column(nullable = false, columnDefinition = "varchar(255) default 'ROLE_USER'")
    private String role; // 유저 상태 (기본값: ROLE_USER, 관리자: ADMIN)
}
