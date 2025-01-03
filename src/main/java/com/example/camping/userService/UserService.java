package com.example.camping.userService;

import com.example.camping.domain.Users;

public interface UserService {
	
	// 로그인 요청 처리
	Users login(String userId, String password);
	
	// 프로필 조회
	Users findByUserId(String userId);
	
	// 프로필 업데이트 정보 저장
	Users save(Users user);
	
	// 비밀번호 암호화 적용
	void registerUser(Users user);
	

}
