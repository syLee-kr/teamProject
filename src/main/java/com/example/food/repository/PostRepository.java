package com.example.food.repository;

import com.example.food.domain.Post;
import com.example.food.domain.Users;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
	
	// 게시글 페이징처리
	@Query("SELECT p FROM Post p ORDER BY p.isNotice DESC, p.priority DESC, p.postdate DESC")
	Page<Post> findAllPosts(Pageable pageable); // 공지사항 상단고정/ 공지사항 우선순위에 따라 정렬/최신게시물 우선 정렬
	
	// Users 객체로 게시글 조회
	List<Post> findByUser(Users user);
	
	// 제목이나 내용에 keyword가 포함된 게시물 검색
	Page<Post> findByTitleContainingOrContentContaining(String keyword, String keyword2, Pageable pageable);
}
