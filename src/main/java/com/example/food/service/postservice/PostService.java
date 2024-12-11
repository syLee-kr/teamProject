package com.example.food.service.postservice;

import java.util.List;

import com.example.food.domain.Post;
import com.example.food.post.PostDTO;

public interface PostService {

	static List<Post> postList(Integer pageNum) {
		// TODO Auto-generated method stub
		return null;
	}

	void updatePost(PostDTO postDto);

	void deletePost(Long pSeq);

	void savePost(PostDTO postDto);

	List<PostDTO> getPostList(Integer pageNum);

	Integer[] getPageList(Integer pageNum);

	PostDTO getPostById(Long pSeq);

	Post getPost(Long pSeq);
	
}
