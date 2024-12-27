package com.example.food.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.food.domain.Users;
import com.example.food.domain.Users.Gender;
import com.example.food.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FixedUser {
	
	@Autowired
	private UserRepository userRepo;
	
	@PostConstruct // 어플리케이션 초기화 시점에 실행
	public void init() {
        Users user = userRepo.findById("test_user")
                .orElseGet(() -> {
                    // 고정 사용자가 없으면 새로 생성하여 저장
                    Users newUser = new Users();
                    newUser.setUserId("test_user");
                    newUser.setName("테스트유저");
                    newUser.setGender(Gender.MALE);
                    newUser.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
                    return userRepo.save(newUser);
                });
        
        log.info("사용자 정보 확인, ID: {}, Role: {}", user.getUserId(), user.getRole());
	}

}
