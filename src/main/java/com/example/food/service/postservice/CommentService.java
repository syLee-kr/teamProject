package com.example.food.service.postservice;

import com.example.food.entity.Comments;
import com.example.food.entity.Users;

import java.util.List;

public interface CommentService {

	/**
	 * 댓글 작성
	 */
	Comments createComment(Long postId, String content, Users user);

	/**
	 * 댓글 삭제
	 */
	void deleteComment(Long cSeq);

	/**
	 * 특정 게시글의 댓글 목록(작성 일자 순)
	 */
	List<Comments> getCommentsByPost(Long postId);
}
