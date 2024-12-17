package com.example.food.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.food.CommentDTO;
import com.example.food.domain.Comments;
import com.example.food.domain.Post;
import java.util.List;


public interface CommentRepository extends JpaRepository<Comments, Long>{

		List<Comments> findByPost(Post post);

		






}
