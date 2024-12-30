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
	private Users fixedUser;
	
	@PostConstruct // 어플리케이션 초기화 시점에 실행
	public void init() {
        Users user = userRepo.findById("test_user")
                .orElse(null);
        if(user == null) {
            // 고정 사용자가 없으면 새로 생성하여 저장
            user = new Users();
            user.setUserId("test_user");
            user.setName("테스트유저");
            user.setGender(Gender.MALE);
            user.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
            userRepo.save(user);
        }else {
        	// 고정사용자 권한을 관리자 권한으로 변경
        	if(!user.getRole().equals(Users.Role.ROLE_ADMIN)) {
        		user.setRole(Users.Role.ROLE_ADMIN);
        		userRepo.save(user); //변경된 권한 저장
        		log.info("고정 사용자 권한 변경 완료, ID: {}, Role: {}", user.getUserId(), user.getRole());
        	}
        }
        // 반환된 유저를 fixedUser에 설정 - 500 오류 원인
        fixedUser = user; 
        
        log.info("사용자 정보 확인, ID: {}, Role: {}", user.getUserId(), user.getRole());
	}
	
	public Users getFixedUser() {
		return fixedUser;
	}

}
