package com.example.food.service.postservice;

import java.util.List;

import com.example.food.PostDTO;
import com.example.food.domain.Post;

public interface PostService {
	
	// 게시물 목록
	List<PostDTO> getPostList(Integer pageNum);
	
	// 게시물 업데이트
	void updatePost(PostDTO postDto);
	
	// 게시물 삭제
	void deletePost(Long pSeq);
	
	// 게시물 저장
	void savePost(PostDTO postDto);

	// 전체 게시물 페이지 수 반환
	Integer[] getPageList();

	// 게시물 상세
	PostDTO getPostById(Long pSeq);

	// 게시물 조회
	Post getPost(Long pSeq);
	
	// 
	List<PostDTO> findNotices(Integer pageNum);
	
	//testPostList
	static List<Post> postList(Integer pageNum) { 
		return null;
	}

	
	
}
