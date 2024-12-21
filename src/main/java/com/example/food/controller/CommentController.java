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
import com.example.food.service.postservice.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 조회
    @GetMapping("/{postId}")
    public List<CommentDTO> getComments(@PathVariable Long postId) {
        log.info("댓글 조회 요청, postId: {}", postId);
        
        List<CommentDTO> comments = commentService.getCommentByPostId(postId);
        
        log.info("댓글 조회 완료, postId: {}, 댓글 개수: {}", postId, comments.size());
        return comments;
    }

    // 댓글 추가
    @PostMapping("/add")
    public void addComment(@RequestBody CommentDTO commentDto) {
        log.info("댓글 추가 요청, userId: {}, postId: {}, content: {}", 
                 commentDto.getUserId(), commentDto.getPostId(), commentDto.getContent());
        
        // 테스트용 고정된 사용자 정보
        commentDto.setUserId("test_user");
        commentDto.setUserName("테스트유저");
        
        commentService.addComment(commentDto);
        log.info("댓글 추가 완료, userId: {}, postId: {}", commentDto.getUserId(), commentDto.getPostId());
    }

    // 댓글 삭제
    @DeleteMapping("/{cSeq}")
    public void delComment(@PathVariable Long cSeq) {
        log.info("댓글 삭제 요청, cSeq: {}", cSeq);
        
        commentService.delComment(cSeq);
        log.info("댓글 삭제 완료, cSeq: {}", cSeq);
    }
}
