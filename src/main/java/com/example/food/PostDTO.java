package com.example.food;

import java.time.OffsetDateTime;

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
	private OffsetDateTime postdate;
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
		// post 객체의 user 필드를 통해 userId/name 가져옴
		this.userId = post.getUser().getUserId();
		this.Name = post.getUser().getName();
	}
}
