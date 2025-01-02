package com.example.camping.userService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.camping.domain.Users;
import com.example.camping.repository.UserRepository;


import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

	
	private UserRepository userRepo;
	
	
	@Override
	public Users login(String userId, String password) {
		Users user = userRepo.findByUserId(userId);
		
		// 로그인 성공 시 
		if (user !=null && user.getPassword().equals(password)) {
			return user;
		}
		// 로그인 실패시 null
		return null;
	}

	// 사용자 정보 검색
	@Override
	public Users findByUsername(String username) {
		
		return userRepo.findByUserId(username);
	}

	
	// 사용자 정보 저장
	@Override
	public Users save(Users user) {
	
		return userRepo.save(user);
	}

}
