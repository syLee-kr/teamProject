package com.example.food.service.postservice;

import java.util.List;

import com.example.food.CommentDTO;
import com.example.food.domain.Comments;

public interface CommentService {
	
	//댓글 조회
	List<CommentDTO> getCommentByPostId(Long postId);
	
	//댓글 추가
	void addComment(CommentDTO commentDto);
	
	//댓글 수정
	Comments getCommentById(Long cSeq);//
	
	//댓글 수정 처리
	void updateComment(Long cSeq, String content);//
	
	//댓글 삭제
	Comments delComment(Long cSeq); //
	
}
