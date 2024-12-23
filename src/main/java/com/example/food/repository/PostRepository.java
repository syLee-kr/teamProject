package com.example.food.repository;

import com.example.food.PostDTO;
import com.example.food.domain.Post;
import com.example.food.domain.Users;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
	
	@Query("SELECT p FROM Post p ORDER BY p.isNotice DESC, p.priority DESC, p.postdate DESC")
	// 공지사항 제외한 일반 게시글 페이징 처리
	Page<Post> findByIsNoticeFalse(Pageable pageable);
	
	//공지사항 목록 조회
	List<Post> findByIsNoticeTrueOrderByPostdateDesc();
	
	// Users 객체로 게시글 조회
	List<Post> findByUser(Users user);
	
	// 제목이나 내용에 keyword가 포함된 게시물 수
	int countByTitleContainingOrContentContaining(String keyword, String keyword2);
	
	// 제목이나 내용에 keyword가 포함된 게시물 검색(공지사항 제외)
	Page<Post> findByTitleContainingOrContentContainingAndIsNoticeFalse(String keyword, String keyword2,
			Pageable pageable);

}
