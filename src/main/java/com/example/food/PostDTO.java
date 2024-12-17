package com.example.food;

import java.time.OffsetDateTime;
import java.util.List;

import com.example.food.domain.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
	private Long pSeq; 				// 게시글 번호	
	private String title;			// 게시글 제목
	private String content;			// 게시글 본문
	private OffsetDateTime postdate;// 게시물 작성 시간
	private int priority;			// 게시물 우선순위
	private Long cnt;				// 게시물 조회수
	private String userId;			// 작성자 ID
	private String Name;			// 작성자 이름
	private List<String> imagePaths;// 이미지 경로 리스트
	
	//post 객체를 PostDTO로 변환
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
		
		this.imagePaths = post.getImagePaths(); 
	}
}
