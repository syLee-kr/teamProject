package com.example.camping.userService;

import com.example.camping.entity.Users;

public interface UserService {
	
	// 로그인 요청 처리
	Users login(String userId, String password);
	
	// 프로필 조회
	Users findByUserId(String userId);
	
	// 프로필 업데이트 정보 저장
	Users save(Users user);
	
	// 비밀번호 암호화 적용
	void registerUser(Users user);
	
	// 회원가입 시 사용자ID 중복체크
	Boolean usernameExists(String userId);
	
	// 비밀번호 재설정 저장
	void saveResetCode(String userId, String resetCode);
	
	// 비밀번호 재설정 코드 검증
	Boolean verifyResetCode(String resetCode);
	
	// 회원탈퇴
	void delete(Users user);
	

}
