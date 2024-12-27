package com.example.food.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.food.CommentDTO;
import com.example.food.domain.Comments;
import com.example.food.service.postservice.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
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
    public String addComment(@RequestParam String content,
    					   @RequestParam Long postId, 
    					   RedirectAttributes redirectAttr) {//추가
        log.info("댓글 추가 요청, postId: {}, content: {}", content, postId);
                 
        // 테스트용 고정된 사용자 정보
        CommentDTO commentDto = new CommentDTO(); 
        
        commentDto.setUserId("test_user");
        commentDto.setUserName("테스트유저");
        commentDto.setContent(content);
        commentDto.setPostId(postId);
        
        commentService.addComment(commentDto);
        log.info("댓글 추가 완료, userId: {}, postId: {}", commentDto.getUserId(), commentDto.getPostId());
        
        // 댓글 추가 후, 게시글 상세 페이지로 
        redirectAttr.addAttribute("postId", postId); //
        return "redirect:/post/detail/{postId}";//
    }

    // 댓글 삭제
    @DeleteMapping("/{cSeq}")
    public String delComment(@PathVariable Long cSeq,
    						RedirectAttributes redirectAttr) {//
        log.info("댓글 삭제 요청, cSeq: {}", cSeq);
        
        // 댓글 정보 
        Comments comment = commentService.delComment(cSeq);
        Long postId = comment.getPost().getPSeq(); //
        
        log.info("댓글 삭제 완료, cSeq: {}", cSeq);
        
        commentService.delComment(cSeq);
        
        // 댓글 삭제 후 detail 페이지로 
        redirectAttr.addAttribute("postId", postId);//
        return "redirect:/post/detail/{postId}";//
    }
}
