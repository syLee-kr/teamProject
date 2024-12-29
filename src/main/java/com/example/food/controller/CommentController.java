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
import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import com.example.food.service.postservice.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepo;

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
        
        Users user = userRepo.findById("test_user")
        					 .orElseThrow(()-> new IllegalArgumentException("test_user가 존재하지않습니다."));
        
        
        // 댓글 추가 객체
        CommentDTO commentDto = new CommentDTO(); 
        commentDto.setUserId(user.getUserId());
        commentDto.setUserName(user.getName());
        commentDto.setContent(content);
        commentDto.setPostId(postId);
        
        commentService.addComment(commentDto);
        log.info("댓글 추가 완료, userId: {}, postId: {}", commentDto.getUserId(), commentDto.getPostId());
        
        // 댓글 추가 후, 게시글 상세 페이지로 
        redirectAttr.addAttribute("postId", postId); //
        return "redirect:/post/detail/{postId}";//
    }
    
    //댓글 수정 폼
    @GetMapping("/{cSeq}/edit")
    public String editCommentForm(@PathVariable Long cSeq,
    						  RedirectAttributes redirectAttr) {
    	log.info("댓글 수정 폼 요청,  cSeq: {}", cSeq);
    	
    	Comments comment = commentService.getCommentById(cSeq);
    	
    	if (comment == null){
    		log.error("댓글을 찾을수 없습니다. , cSeq: {}", cSeq);
    		return "redirect:/post/detail"; // 댓글이 없으면 상세페이지로 
    	}
    	
    	redirectAttr.addFlashAttribute("comment", comment);
    	return "post/detail";
    }
    
    // 댓글 수정 처리
    @PostMapping("{cSeq}/edit")
    public String updateComment(@PathVariable Long cSeq,
    							@RequestParam String content,
    							RedirectAttributes redirectAttr) {
    	log.info("댓글 수정 요청, cSeq: {}, content: {}", cSeq, content);
    	commentService.updateComment(cSeq, content);
    	return "redirect:/post/detail/{postId}";
    }
    
    
    // 댓글 삭제
    @PostMapping("/{cSeq}/detele")
    public String delComment(@PathVariable Long cSeq,
    						RedirectAttributes redirectAttr) {//
        log.info("댓글 삭제 요청, cSeq: {}", cSeq);
        
        // 댓글 삭제
        Comments comment = commentService.delComment(cSeq);
        // 댓글이 속한 게시글 ID
        Long postId = comment.getPost().getPSeq(); //
        
        log.info("댓글 삭제 완료, cSeq: {}", cSeq);
        
        // 댓글 삭제 후 detail 페이지로 
        redirectAttr.addAttribute("postId", postId);//
        return "redirect:/post/detail/{postId}";//
    }
}
