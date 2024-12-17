package com.example.food.service.postservice;

import java.util.List;

import com.example.food.CommentDTO;

public interface CommentService {

	List<CommentDTO> getCommentByPostId(Long postId);
	
	void addComment(CommentDTO commentDto);
	
	void delComment(Long cSeq);
	
}
