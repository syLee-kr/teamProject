package com.example.food.service.postservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.food.CommentDTO;
import com.example.food.domain.Comments;
import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.repository.CommentRepository;
import com.example.food.repository.PostRepository;
import com.example.food.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // @Slf4j 추가

@Slf4j  
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    // 특정 게시물 댓글 조회
    @Override
    public List<CommentDTO> getCommentByPostId(Long postId) {
        log.info("댓글 조회 요청, postId: {}", postId);
        
        Post post = postRepo.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("ID가 " + postId + "인 게시물이 존재하지 않습니다."));
        
        List<Comments> comments = commentRepo.findByPost(post);
        
        if (comments.isEmpty()) {
            log.warn("ID가 {}인 게시물에 댓글이 존재하지 않습니다.", postId);
            throw new IllegalArgumentException("ID가 " + postId + "인 게시물에 댓글이 존재하지 않습니다.");
        }
        
        log.info("댓글 조회 완료, postId: {}, 댓글 개수: {}", postId, comments.size());
        return comments.stream()
                       .map(CommentDTO::new)
                       .collect(Collectors.toList());
    }
    
    // 댓글 추가
    @Override
    public void addComment(CommentDTO commentDto) {
        log.info("댓글 추가 요청, postId: {}, userId: {}, content: {}", 
                 commentDto.getPostId(), commentDto.getUserId(), commentDto.getContent());

        // 댓글과 연결된 게시물 조회
        Post post = postRepo.findById(commentDto.getPostId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        
		/*
		// 댓글 작성자 조회
		Users user = userRepo.findById(commentDto.getUserId())
							.orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));
		*/
        
        // 고정 사용자 설정 (테스트용 고정 사용자)
        Users user = userRepo.findById("test_user")
                             .orElseGet(() -> {
                                 // 고정 사용자가 없으면 새로 생성하여 저장
                                 Users newUser = new Users();
                                 newUser.setUserId("test_user");
                                 newUser.setName("테스트유저");
                                 return userRepo.save(newUser); // 새로 저장된 사용자를 반환
                             });
        
        // 댓글 생성 및 저장
        Comments comment = Comments.builder()
                                   .content(commentDto.getContent())
                                   .post(post)
                                   .user(user)
                                   .build();
        
        commentRepo.save(comment);
        log.info("댓글이 추가되었습니다: postId = {}, userId = {}", commentDto.getPostId(), commentDto.getUserId());
    }

    // 댓글 삭제
    @Override
    public Comments delComment(Long cSeq) {
        log.info("댓글 삭제 요청, cSeq: {}", cSeq);
        
        Comments comment = commentRepo.findById(cSeq)
                                      .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        
        // 삭제된 댓글이 속한 게시글의 ID
        Long postId = comment.getPost().getPSeq(); // 
        
        commentRepo.delete(comment);
        
        log.info("댓글이 삭제되었습니다: cSeq = {}", cSeq);
		
        return comment;
    }
}
