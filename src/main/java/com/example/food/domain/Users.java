package com.example.food.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.*;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Users {

    @Id
    private String userId;          // 유저 아이디
    private String password;        // 유저 비밀번호
    private String name;            // 유저 이름
    private String email;           // 유저 이메일
    private String phone;           // 유저 전화번호
    private String address;         // 유저 주소

    private double height;          // 키
    private double weight;          // 체중

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;          // 생일

    public int getAge() {
        if (this.birthday == null) {
            return 0;
        }
        return Period.between(this.birthday, LocalDate.now()).getYears();
    }

    // Gender Enum 추가
    public enum Gender {
        MALE, FEMALE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender; // 성별

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions; // 유저가 작성한 질문 리스트

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers; // 관리자가 작성한 답변 리스트

    @CreationTimestamp              //  자동으로 시간을 적용
    @Column(updatable = false)
    private OffsetDateTime regdate;

    @Column(columnDefinition = "varchar2(255) default 'images/default.png'")
    private String profileImage; // 프로필 이미지

    // Role Enum 추가
    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_USER; // 유저 상태

}
