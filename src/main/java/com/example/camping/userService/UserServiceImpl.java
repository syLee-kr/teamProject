package com.example.camping.userService;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;


import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService{

	
	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;
	
	
	@Override
	public Users login(String userId, String password) {
		Users user = userRepo.findByUserId(userId);
		
		// 로그인 성공 시 
		if (user !=null && passwordEncoder.matches(password, user.getPassword())) {
			return user;
		}
		// 로그인 실패시 null
		return null;
	}

	// 사용자 정보 검색
	@Override
	public Users findByUserId(String userId) {
		
		return userRepo.findByUserId(userId);
	}

	
	// 사용자 정보 저장
	@Override
	public Users save(Users user) {
	
		return userRepo.save(user);
	}
	
	/*
	 * Spring Security에서 사용하는 UserDetailsService 메서드 구현 = CustomUserDetailsService
	 * 객체를 따로 구현하지 않고 UserServiceImpl에서 메서드를 통해서 구현가능
	 */
	// 사용자 인증
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// 사용자 DB에서 검색
		Users user = userRepo.findByUserId(username);
		
		if (user == null) {
			throw new UsernameNotFoundException("사용자의 UserId가 없습니다:" + username);
		}
		
		// Spring Security 사용
		return User.builder()
				   .username(user.getUserId())	  // 사용자 ID
				   .password(user.getPassword())  // 암호화된 비밀번호
				   .roles(user.getRole().name())  // 사용자의 권한
				   .build();
	}
	
	// 비밀번호 암호화
	@Override
	public void registerUser(Users user) {
		
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		
		userRepo.save(user);
	}
	
	// 회원가입 시 사용자ID 중복체크
	@Override
	public Boolean usernameExists(String userId) {
		// userId로 사용자 검색
		Users existingUser = userRepo.findByUserId(userId);
		
		// 해당 userId가 존재하면 true, 없으면 false로 반환
		return existingUser != null;
	}

}
