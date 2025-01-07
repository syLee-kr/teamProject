package com.example.camping.config;

import com.example.camping.domain.Users;
import com.example.camping.domain.Users.Role;
import com.example.camping.domain.Users.Gender;
import com.example.camping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 일반 유저 추가
        if (userRepository.existsById("user1")) {
            System.out.println("일반 유저(user1)는 이미 생성되었습니다.");
        } else {
            Users user = Users.builder()
                    .userId("user1")
                    .password(passwordEncoder.encode("password123")) // 비밀번호 암호화
                    .name("일반유저")
                    .email("user1@example.com")
                    .phone("010-1234-5678")
                    .address("서울특별시 강남구")
                    .birthday(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);
            System.out.println("일반 유저(user1)가 생성되었습니다.");
        }

        // 관리자 유저 추가
        if (userRepository.existsById("admin1")) {
            System.out.println("관리자 유저(admin1)는 이미 생성되었습니다.");
        } else {
            Users admin = Users.builder()
                    .userId("admin1")
                    .password(passwordEncoder.encode("admin123")) // 비밀번호 암호화
                    .name("관리자")
                    .email("admin1@example.com")
                    .phone("010-8765-4321")
                    .address("서울특별시 종로구")
                    .birthday(LocalDate.of(1985, 5, 15))
                    .gender(Gender.FEMALE)
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("관리자 유저(admin1)가 생성되었습니다.");
        }
    }
}
