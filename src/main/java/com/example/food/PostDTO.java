package com.example.food;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
	private OffsetDateTime postdate;// 게시글 작성 시간
	private String formattedPostdate; // 포맷된 게시글 작성 시간 (yyyy.MM.dd HH:mm:ss)
	private int priority;			// 게시글 우선순위
	private Long cnt;				// 게시글 조회수
	private String userId;			// 작성자 ID
	private String userName;		// 작성자 이름			
	private List<String> imagePaths;// 이미지 경로 리스트
	private Boolean isNotice; 		// 게시글 공지사항/일반글
	
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
		this.userName = post.getUser().getName();
		this.imagePaths = post.getImagePaths();
		this.isNotice = post.getIsNotice();
		
        // 날짜 포맷 처리 (yyyy-MM-dd HH:mm 형식으로 변환)
        if (this.postdate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            this.formattedPostdate = this.postdate.format(formatter); // 생성자 내에서 포맷
        }	
	}
}
