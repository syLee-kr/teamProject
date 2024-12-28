package com.example.food.repository;

import com.example.food.entity.Comments;
import com.example.food.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comments, Long> {

	// 특정 게시글에 달린 댓글 목록 (작성 일자 순)
	// JpaRepository 상에서는 기본 정렬을 위해 findByPostOrderByCreatedAtAsc/Desc 등 만들 수 있음
	List<Comments> findByPostOrderByCreatedAtAsc(Post post);

}
