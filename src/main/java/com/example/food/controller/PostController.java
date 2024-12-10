package com.example.food.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.service.postservice.PostService;

@Service
@Controller
public class PostController {
	
	
	@Autowired
	private PostService postservice;
	
	/*
	 *테스트용 게시판 
	 */
	@RequestMapping("/testPostList")
	public String testPostList(Model model){
		List<Post> postList= new ArrayList<Post>();
		
		
		//임시 게시물
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
		// priority 내림차순 정렬
		postList.sort(Comparator.comparing(Post::getPriority).reversed());
		
		model.addAttribute("postList", postList);
		return "testPostList";
	}
	
}
