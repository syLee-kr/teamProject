package com.example.food.service.postservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.food.PostDTO;
import com.example.food.domain.Post;
import com.example.food.repository.PostRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostSreviceImpl implements PostService{
	
	private final PostRepository postRepository;
	
	// DTO return
	@Override 
	public PostDTO getPostById(Long pSeq) {
		
		Post post = postRepository.findById(pSeq).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		
		return new PostDTO(post);
	}
	
	// Entity return
	@Override 
	public Post getPost(Long pSeq) {
		return postRepository.findById(pSeq).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
	}
	
	// Post update
	@Override
	public void updatePost(PostDTO postDto) {
		Post post = postRepository.findById(postDto.getPSeq())
				.orElseThrow(()-> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		
		postRepository.save(post);
	}
	
	// Post del
	@Override
	public void deletePost(Long pSeq) {
		postRepository.deleteById(pSeq);
		
	}
	
	// Post save
	@Override
	public void savePost(PostDTO postDto) {
		Post post = new Post();
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		
		postRepository.save(post);
	}
	
	// Paging process
	@Override
	public List<PostDTO> getPostList(Integer pageNum) {
		int pageSize = 10; // 페이지당 게시물 수 10개
		Pageable pageable = PageRequest.of(pageNum -1, pageSize);
		Page<Post> postPage = postRepository.findAll(pageable); // 페이지 요청해서 게시물 목록 가저옴
		
		return postPage.getContent().stream()						// 페이지에서 게시물 목록 가져와
									.map(post -> new PostDTO(post)) // post entity를 postDTO로 변환
									.collect(Collectors.toList());	// List<PostDTO>로 return
	}

	@Override
	public Integer[] getPageList(Integer pageNum) {
		
		int pageSize = 10;
		int totalPosts = (int)postRepository.count(); // 전체게시물
		int totalPages = (int)Math.ceil((double) totalPosts/pageSize); // 전체 페이지 수(올림처리) 
		
		Integer[] pageList = new Integer[totalPages]; // 페이지 수만큼 배열
		for (int i =0; i < totalPages; i++) { // 전체 페이지 수만큼 반복
			pageList[i] = i + 1; // 페이지 번호 1부터 시작
		}
		return pageList;
	}

}
