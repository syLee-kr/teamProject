package com.example.food.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.food.PostDTO;
import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.service.postservice.PostService;

import lombok.AllArgsConstructor;

@Service
@Controller
@AllArgsConstructor
@RequestMapping("post")
public class PostController {
	
	private PostService postService;
	
	/*
	 * 게시물 목록
	 */
	@GetMapping({"", "/list"})
	public String PostList(Model model, @RequestParam(value="page", defaultValue="1") Integer pageNum,
										@RequestParam(value="noticeOnly", defaultValue="false") boolean noticeOnly){
		List<PostDTO> postList;
		
		if (noticeOnly) {
			// 공지글만 조회
			postList = postService.findNotices(pageNum);
		}else {
			// 모든 게시글 조회
			postList = postService.getPostList(pageNum);
		}
		
		// 우선순위 내림차순 정렬
		postList.sort(Comparator.comparing(PostDTO::getPriority).reversed());
		
		// 페이지 번호 목록
		Integer[] pageList = postService.getPageList();
		
		model.addAttribute("postList", postList);
		model.addAttribute("pageList", pageList);
		model.addAttribute("noticeOnly", noticeOnly);
		
		return "post/list";

	}
	/*
	 * 게시물 작성 페이지
	 */
	@GetMapping("/write")
	public String write() {
		return "post/write";
	}
	
	/*
	 * 게시물 작성 처리
	 */
	@PostMapping("/write")
	public String write(PostDTO postDto) {
		postService.savePost(postDto);
		
		return "redirect:post/list";
	}
	
	/*
	 * 게시물 상세보기
	 */
	@GetMapping("/detail/{pSeq}")
	public String detail(@PathVariable("pSeq") Long pSeq, Model model) {
		
		Post post = postService.getPost(pSeq);
		
		PostDTO postDto = new PostDTO(post);
		model.addAttribute("postDto", postDto);
		
		return "detail";
		//return "post/detail";
	}
	
	/*
	 * test detail
	 */
	@GetMapping("/detail")
	public String detail() {
		return "post/detail";
	}
	
	/*
	 * 게시물 수정
	 */
	@GetMapping("/edit/{pSeq}")
	public String edit(@PathVariable("pSeq") Long pSeq, PostDTO postDto) {
		postDto.setPSeq(pSeq);
		postService.updatePost(postDto);
		
		return "redirect:/post/list";
		
	}
	
	/*
	 * 게시물 삭제
	 */
	@PostMapping("/delete/{pSeq}")
	public String delete(@PathVariable("pSeq") Long pSeq) {
		postService.deletePost(pSeq);
		
		return "redirect:/post/list";
	}
	
	/*
	 *test list 
	 */
	@GetMapping("/testPostList")
	public String testPostList(Model model){
		List<Post> postList= new ArrayList<Post>();
		
		
		//temporal post
		for (int i =0; i <9; i++) {
			Post post = new Post();
			post.setPSeq((long) i);
			post.setTitle("제목   " +i);
			
			Users user = new Users();
			post.setUser(user);
			
			post.setContent("글내용 " + i);
			post.setPostdate(LocalDateTime.now());
			
			if(i == 0) {
				post.setPriority(1); // 공지	
			}else {
				post.setPriority(0); // 일반
			}
			
			post.setCnt(0L);
			postList.add(post);
			
		}
		// test priority 내림차순 정렬
		postList.sort(Comparator.comparing(Post::getPriority).reversed());
		
		model.addAttribute("postList", postList);
		return "post/test/testPostList";
	}
}
