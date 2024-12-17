package com.example.food.service.postservice;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.food.CommentDTO;
import com.example.food.domain.Comments;
import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.repository.CommentRepository;
import com.example.food.repository.PostRepository;
import com.example.food.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepo;
	private final PostRepository postRepo;
	private final UserRepository userRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);
	
	// 특정 게시물 댓글 조회
	@Override
	public List<CommentDTO> getCommentByPostId(Long postId) {
		Post post = postRepo.findById(postId)
							.orElseThrow(() -> new IllegalArgumentException("ID가 " + postId + "인 게시물이 존재하지 않습니다."));
		
		List<Comments> comments = commentRepo.findByPost(post);
		
		if (comments.isEmpty()) {
			throw new IllegalArgumentException("ID가 " + postId + "인 게시물에 댓글이 존재하지 않습니다.");
		}
		
		
		return comments.stream()
					   .map(CommentDTO::new)
					   .collect(Collectors.toList());
	}
	
	// 댓글 추가
	@Override
	public void addComment(CommentDTO commentDto) {
		// 댓글과 연결되 게시물 조회
		Post post = postRepo.findById(commentDto.getPostId())
							.orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		
		// 댓글 작성자 조회
		Users user = userRepo.findById(commentDto.getUserId())
							.orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));
		
		
		// 댓글 생성 및 저장
		Comments comment = Comments.builder()
								   .content(commentDto.getContent())
								   .post(post)
								   .user(user)
								   .build();
		
		commentRepo.save(comment);
		logger.info("댓글이 추가되었습니다: postId = {}, userId = {}", commentDto.getPostId(), commentDto.getUserId());
	}

	@Override
	public void delComment(Long cSeq) {
		Comments comment = commentRepo.findById(cSeq)
									  .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
		commentRepo.delete(comment);
		
		logger.info("댓글이 삭제되었습니다: cSeq = {}", cSeq);
	}

}
