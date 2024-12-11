package com.example.food.service.postservice;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.food.post.PostDTO;

@Service
public class PostSreviceImpl implements PostService{

	@Override
	public void updatePost(PostDTO postDto) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletePost(Long pSeq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePost(PostDTO postDto) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<PostDTO> getPostList(Integer pageNum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[] getPageList(Integer pageNum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostDTO getPostById(Long pSeq) {
		// TODO Auto-generated method stub
		return null;
	}
}
