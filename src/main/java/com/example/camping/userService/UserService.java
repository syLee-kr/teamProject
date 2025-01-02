package com.example.camping.userService;

import com.example.camping.domain.Users;

public interface UserService {
	
	// 로그인 요청 처리
	Users login(String userId, String password);
	
	// 프로필 조회
	Users findByUsername(String username);
	
	// 프로필 업데이트 정보 저장
	Users save(Users user);

}
