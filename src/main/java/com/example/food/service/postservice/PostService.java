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
	
	// 공지사항만 필터링하여 페이징 처리된 게시물 목록 반환
	List<PostDTO> findNotices(Integer pageNum);

	
	
}
