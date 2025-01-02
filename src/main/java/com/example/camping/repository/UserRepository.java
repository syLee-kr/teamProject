package com.example.camping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.camping.domain.Users;

public interface UserRepository extends JpaRepository<Users, Long>{
	
	// 사용자 정보 조회
	Users findByUserId(String userId);

}
