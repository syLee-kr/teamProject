package com.example.food.repository;

import com.example.food.domain.Post;
import com.example.food.domain.Users;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
	
	// 공지글 우선순위 순 조회
	List<Post> findByIsNoticeTrueOrderByPriorityDesc();
	
	// 일반글 우선순위 순 조회
	List<Post> findByIsNoitceFalseOrderByPriorityDesc();
	
	// 제목에서 'keyword'가 포함된 게시글을 찾는 메서드
	List<Post> findByTitleContaining(String keyword);
	
	// Users 객체로 게시글 조회
	List<Post> findByUser(Users user);
}
