package com.example.food.dto;

import java.time.OffsetDateTime;


import com.example.food.entity.Comments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
	private Long cSeq;
	private String content;
	private Long postId;
	private String userId;
	private String userName;
	private OffsetDateTime createdAt;
	
	public CommentDTO(Comments comment) {
		this.cSeq = comment.getCSeq();
		this.content = comment.getContent();
		this.postId = comment.getPost().getPSeq();
		this.userId = comment.getUser().getUserId();
		this.userName = comment.getUser().getName();
		this.createdAt = comment.getCreatedAt();
		
	}
}
