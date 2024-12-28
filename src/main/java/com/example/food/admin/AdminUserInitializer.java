package com.example.food.admin;

import com.example.food.entity.Users;
import com.example.food.entity.Users.Role;
import com.example.food.entity.Users.Gender;
import com.example.food.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Bean
    public CommandLineRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminId = "admin";

            if (userRepository.findById(adminId).isEmpty()) {
                Users admin = Users.builder()
                        .userId(adminId)
                        .password(passwordEncoder.encode("admin1234")) // 비밀번호 암호화
                        .name("관리자")
                        .phone("010-1234-5678")
                        .address("서울특별시 관리자구")
                        .birthday(null)
                        .gender(Gender.MALE)
                        .role(Role.ROLE_ADMIN)
                        .profileImage("profileimg.png") // 기본 프로필 이미지 설정
                        .build();

                userRepository.save(admin);
                logger.info("관리자 계정이 생성되었습니다. ID: {}, 비밀번호: admin1234", adminId);
            } else {
                logger.info("관리자 계정이 이미 존재합니다. ID: {}", adminId);
            }
        };
    }
}
