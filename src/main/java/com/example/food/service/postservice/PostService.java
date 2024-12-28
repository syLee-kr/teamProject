package com.example.food.service.postservice;

import com.example.food.entity.Post;
import com.example.food.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

	/**
	 * 전체 게시글을 공지글 우선, 그 다음 우선순위 및 작성일 순으로 페이징 조회
	 */
	Page<Post> getAllPosts(String keyword, Pageable pageable);

	/**
	 * 게시글 상세 보기 + 조회수 증가 처리
	 */
	Post getPostDetail(Long pSeq);

	/**
	 * 게시글 작성
	 */
	Post createPost(Post post);

	/**
	 * 게시글 삭제
	 */
	void deletePost(Long pSeq);

	/**
	 * 특정 유저의 게시글 목록 (페이징)
	 */
	Page<Post> getPostsByUser(Users user, Pageable pageable);

	/**
	 * 공지글 중 키워드로 페이징 조회
	 */
	List<Post> getNoticePosts(String keyword);

	/**
	 * 일반글 중 키워드로 페이징 조회
	 */
	Page<Post> getRegularPosts(String keyword, Pageable pageable);

	/**
	 * 전체 페이지 수 계산 (공지글과 일반글을 합산)
	 */
	int getTotalPages(String keyword, int size);

	/**
	 * 페이지 번호 리스트 생성 (현재 페이지 기준 ±2)
	 */
	List<Integer> getPageList(int currentPage, int totalPages);
}
