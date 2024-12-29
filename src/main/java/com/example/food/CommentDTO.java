package com.example.food;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.example.food.domain.Comments;

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
	private OffsetDateTime createdAt; 		// 댓글 작성 시간
	private String formattedCreatedAt;		// 포맷된 게시글 작성시간
	private OffsetDateTime updatedAt;       // 댓글 수정 시간 //
	private String formattedUpdatedAt;		// 포맷된 게시글 수정시간 //
	
	public CommentDTO(Comments comment) {
		this.cSeq = comment.getCSeq();
		this.content = comment.getContent();
		this.postId = comment.getPost().getPSeq();
		this.userId = comment.getUser().getUserId();
		this.userName = comment.getUser().getName();
		this.createdAt = comment.getCreatedAt();
		this.updatedAt = comment.getUpdatedAt();//
		
		// 날짜 포맷 처리 (yyyy-MM-dd HH:mm 형식으로 변환)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        this.formattedCreatedAt = this.createdAt.format(formatter); // 생성자 내에서 포맷
        this.formattedUpdatedAt = this.updatedAt.format(formatter);//
	}
}
