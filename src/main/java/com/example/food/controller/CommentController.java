package com.example.food.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.food.CommentDTO;
import com.example.food.domain.Users;
import com.example.food.domain.Users.Gender;
import com.example.food.service.postservice.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
	
	
	private final CommentService commentService;
	
	@GetMapping("/{postId}")
	public List<CommentDTO> getComments(@PathVariable Long postId){
		return commentService.getCommentByPostId(postId);
	}
	
	// 댓글 추가
	@PostMapping("/add")
	public void addComment(@RequestBody CommentDTO commentDto) {
		
		//테스트용 고정된 사용자 정보
		commentDto.setUserId("test_user");
		commentDto.setUserName("테스트유저");
		
		commentService.addComment(commentDto);
	}
	
	// 댓글 삭제
	@DeleteMapping("/{cSeq}")
	public void delComment(@PathVariable Long cSeq) {
		commentService.delComment(cSeq);
	}
}
