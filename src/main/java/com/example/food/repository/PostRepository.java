package com.example.food.repository;

import com.example.food.entity.Post;
import com.example.food.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	/**
	 * 전체 게시글을 공지글 우선, 그 다음 우선순위 및 작성일 순으로 정렬하여 조회
	 * 키워드가 있을 경우 제목이나 내용에 포함된 게시글을 필터링
	 */
	@Query("SELECT p FROM Post p WHERE (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.isNotice DESC, p.priority DESC, p.postdate DESC")
	Page<Post> findAllPosts(@Param("keyword") String keyword, Pageable pageable);


	/**
	 * 공지글을 제목이나 내용에 키워드가 포함된 경우 필터링하여 조회
	 * 키워드가 없을 경우 모든 공지글을 조회
	 */
	@Query("SELECT p FROM Post p " +
			"WHERE p.isNotice = true AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
			"ORDER BY p.postdate DESC")
	List<Post> findNoticePosts(@Param("keyword") String keyword);

	/**
	 * 일반 게시글을 제목이나 내용에 키워드가 포함된 경우 필터링하여 페이징 조회
	 * 키워드가 없을 경우 모든 일반 게시글을 페이징 조회
	 */
	@Query("SELECT p FROM Post p " +
			"WHERE p.isNotice = false AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
			"ORDER BY p.postdate DESC")
	Page<Post> findRegularPosts(@Param("keyword") String keyword, Pageable pageable);

	/**
	 * 전체 게시글 수를 키워드로 필터링하여 계산
	 */
	@Query("SELECT COUNT(p) FROM Post p " +
			"WHERE (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
	long countAllPosts(@Param("keyword") String keyword);

	/**
	 * 공지글 수를 키워드로 필터링하여 계산
	 */
	@Query("SELECT COUNT(p) FROM Post p " +
			"WHERE p.isNotice = true AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
	long countNoticePosts(@Param("keyword") String keyword);

	/**
	 * 일반 게시글 수를 키워드로 필터링하여 계산
	 */
	@Query("SELECT COUNT(p) FROM Post p " +
			"WHERE p.isNotice = false AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
	long countRegularPosts(@Param("keyword") String keyword);

	/**
	 * 특정 유저의 게시글 목록을 페이징 조회
	 */
	Page<Post> findByUser(Users user, Pageable pageable);
}
