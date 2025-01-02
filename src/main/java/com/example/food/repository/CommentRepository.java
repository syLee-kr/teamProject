package com.example.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.food.domain.Comments;
import com.example.food.domain.Post;
import java.util.List;


public interface CommentRepository extends JpaRepository<Comments, Long>{
		
	@Query("SELECT c FROM Comments c WHERE c.post = :post ORDER BY c.createdAt DESC")
	List<Comments> findByPost(Post post);
	
}
