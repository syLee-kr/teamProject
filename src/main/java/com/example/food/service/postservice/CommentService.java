package com.example.food.service.postservice;

import java.util.List;

import com.example.food.CommentDTO;
import com.example.food.domain.Comments;

public interface CommentService {

	List<CommentDTO> getCommentByPostId(Long postId);
	
	void addComment(CommentDTO commentDto);
	
	Comments delComment(Long cSeq); //
	
}
