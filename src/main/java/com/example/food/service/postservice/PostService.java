package com.example.food.service.postservice;

import java.util.List;

import com.example.food.PostDTO;
import com.example.food.domain.Post;

public interface PostService {
	
	// 게시물 목록(공지사항 포함)
	List<PostDTO> getPostList(Integer pageNum);
	
	// 게시물 수정
	void updatePost(PostDTO postDto);
	
	// 게시물 삭제
	void deletePost(Long pSeq);
	
	// 게시물 저장
	void savePost(PostDTO postDto);

	// 전체 페이지 수 (페이징용) 
	Integer getTotalPages();
	
	// 전체 페이지 목록 (페이징용)
	Integer[] getPageList();

	// 게시물 상세
	PostDTO getPostById(Long pSeq);

	// 게시물 조회
	Post getPost(Long pSeq);
	
	// keyword 검색
	List<PostDTO> searchPostsByKeyword(String keyword, Integer pageNum);
	
	// 게시물 조회수
	void viewCount(Long pSeq);
	
	// 검색된 게시물에 대한 전체 페이지 수
	Integer getTotalPagesForSearch(String keyword);
	
	// 검색된 게시물에 대한 전체 페이지 목록
	Integer[] getPageListForSearch(String keyword, Integer pageNum);
	
	

	
	
}
