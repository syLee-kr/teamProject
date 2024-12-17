package com.example.food.controller;

import java.io.File;
import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;

import com.example.food.PostDTO;
import com.example.food.domain.Post;
import com.example.food.service.postservice.PostService;

import lombok.AllArgsConstructor;

@Service
@Controller
@AllArgsConstructor
@RequestMapping("/post")
public class PostController {
	
	private PostService postService;

	/*
	 * 게시물 목록
	 */
	@GetMapping
	public String PostList(Model model, @RequestParam(value="page", defaultValue="1") Integer pageNum,
										@RequestParam(value="noticeOnly", defaultValue="false") Boolean noticeOnly){
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
		
		// 전체 페이지 번호 생성
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
	public String write(@RequestParam("title") String title,
					    @RequestParam("content") String content,
					    @RequestParam("images") MultipartFile[] images) throws IOException {
		List<String> imagePaths = new ArrayList<>();
		
		// 이미지 파일 서버에 저장
		for (MultipartFile image : images) {
			String imagePath = saveImage(image);
			if(imagePath != null) {
				imagePaths.add(imagePath);
			}
		}
		
		// 게시물 저장
		PostDTO postDto = new PostDTO(); //이미지 경로포함
		postService.savePost(postDto);

		return "redirect:post/list";
	}
	
	private String saveImage(MultipartFile image) throws IOException {
		if(!image.isEmpty()) {
			String uploadDir = "src/main/resources/static/images/uploadimg/"; //이미지 저장경로(저장경로를 어디로 설정해야하는지?)
			String fileName =System.currentTimeMillis() + "_" + image.getOriginalFilename();
			File file = new File(uploadDir + fileName);
			image.transferTo(file); // 파일 서버에 저장
			return "images/uploadimg/" + fileName;
		}
		return null;  // 이미지 없으며 null
	}
	
	/*
	 * 게시물 상세보기
	 */
	@GetMapping("/detail/{pSeq}")
	public String detail(@PathVariable("pSeq") Long pSeq, Model model) {
		
		Post post = postService.getPost(pSeq);
		
		PostDTO postDto = new PostDTO(post);
		model.addAttribute("postDto", postDto);
		
		return "post/detail";
	}
	
	
	/*
	 * 게시물 수정
	 */
	@GetMapping("/edit/{pSeq}")
	public String edit(@PathVariable("pSeq") Long pSeq, 
					   @RequestParam("title") String title,
					   @RequestParam("content") String content,
					   @RequestParam("images")MultipartFile[] images) throws IOException {
		List<String> imagePaths = new ArrayList<>();
		
		for(MultipartFile image : images) {
			String imagePath = saveImage(image);
			if (imagePath != null) {
				imagePaths.add(imagePath);
			}
		}
		
		PostDTO postDto = new PostDTO();
		postDto.setPSeq(pSeq);
		postDto.setTitle(title);
		postDto.setContent(content);
		postDto.setImagePaths(imagePaths);
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
	
}
