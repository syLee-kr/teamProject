package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class AppUser {

    @Id
    private String userId;          //  유저 아이디
    private String password;        //  유저 비밀번호
    private String name;            //  유저 이름
    private String email;           //  유저 이메일
    private String phone;           //  유저 전화번호
    private String address;         //  유저 주소

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date regdate = new Date();      //  유저 가입일

    @Column(columnDefinition = "varchar2(255) default 'images/default.png'")
    private String profileImage;   //  프로필 이미지

    @Column(nullable = false, columnDefinition = "varchar(255) default 'ROLE_USER'")
    private String role;            //  유저 상태 -> 일반 유저인지 관리자인지 구분하는 로직. 기본값은 ROLE_USER, 관리자는 ADMIN
}
