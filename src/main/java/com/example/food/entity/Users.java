package com.example.food.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.*;
import java.util.ArrayList;
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
    @Column(nullable = false)
    private String userId; // 유저 아이디 (PK)

    @Column(nullable = true)
    private String password; // 유저 비밀번호

    private String name;     // 유저 이름
    private String phone;    // 유저 전화번호
    private String address;  // 유저 주소

    private double height;   // 키
    private double weight;   // 체중

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday; // 생일

    public int getAge() {
        if (this.birthday == null) {
            return 0;
        }
        return Period.between(this.birthday, LocalDate.now()).getYears();
    }

    public enum Gender {
        MALE("M"), FEMALE("F");

        private final String code;

        Gender(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Gender fromCode(String code) {
            for (Gender gender : Gender.values()) {
                if (gender.code.equalsIgnoreCase(code)) {
                    return gender;
                }
            }
            throw new IllegalArgumentException("Invalid gender code: " + code);
        }
    }


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender; // 성별

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Question> questions = new ArrayList<>(); // 유저가 작성한 질문 리스트

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>(); // 관리자가 작성한 답변 리스트

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<SaveFood> saveFoods = new ArrayList<>(); // 저장된 식단 리스트

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime regdate;

    // MySQL에서는 varchar로 설정
    @Column(nullable = false, columnDefinition = "varchar(255) default 'profileimg.png'")
    private String profileImage; // 프로필 이미지

    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_USER; // 유저 상태

}