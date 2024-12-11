package com.example.food;

import java.time.LocalDateTime;

import com.example.food.domain.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
	private Long pSeq;
	private String title;
	private String content;
	private LocalDateTime postdate;
	private int priority;
	private Long cnt;
	
	private String userId;
	private String Name;
	
	public PostDTO(Post post) {
		this.pSeq = post.getPSeq();
		this.title = post.getTitle();
		this.content = post.getContent();
		this.postdate = post.getPostdate();
		this.priority = post.getPriority();
		this.cnt = post.getCnt();
		this.userId = post.getUserId();
		this.Name = post.getName();
	}
}
