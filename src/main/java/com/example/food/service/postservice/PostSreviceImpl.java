package com.example.food.service.postservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.food.domain.Post;
import com.example.food.post.PostDTO;
import com.example.food.repository.PostRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostSreviceImpl implements PostService{
	
	private final PostRepository postRepository;

	@Override
	public void updatePost(PostDTO postDto) {
		Post post = postRepository.findById(postDto.getPSeq())
				.orElseThrow(()-> new IllegalArgumentException("Post not found"));
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		
		postRepository.save(post);
	}

	@Override
	public void deletePost(Long pSeq) {
		postRepository.deleteById(pSeq);
		
	}

	@Override
	public void savePost(PostDTO postDto) {
		Post post = new Post();
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		
		postRepository.save(post);
	}

	@Override
	public List<PostDTO> getPostList(Integer pageNum) {
		int pageSize = 10;
		List<Post> posts = postRepository.findAll();
		// 페이지 네이션에 맞게 수정??
		
		return posts.stream().map(post -> new PostDTO(post)).collect(Collectors.toList());
	}

	@Override
	public Integer[] getPageList(Integer pageNum) {
		
		int pageSize = 10;
		int totalPosts = (int)postRepository.count();
		int totalPages = (int)Math.ceil((double) totalPosts/pageSize);
		
		Integer[] pageList = new Integer[totalPages];
		for (int i =0; i < totalPages; i++) {
			pageList[i] = i + 1;
		}
		return pageList;
	}

	@Override
	public PostDTO getPostById(Long pSeq) {
		
		Post post = postRepository.findById(pSeq).orElseThrow(() -> new IllegalArgumentException("Post not found"));
		
		return new PostDTO(post);
	}

	@Override
	public Post getPost(Long pSeq) {
		return postRepository.findById(pSeq).orElseThrow(() -> new IllegalArgumentException("Post not found"));
	}
}
