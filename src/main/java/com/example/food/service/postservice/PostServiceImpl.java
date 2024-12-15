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
public class PostServiceImpl implements PostService{
	
	private final PostRepository postRepository;
	
	// 게시물 상세조회(화면표시)
	@Override 
	public PostDTO getPostById(Long pSeq) {
		
		Post post = postRepository.findById(pSeq)
								  .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		
		return new PostDTO(post);
	}
	
	// 게시물 수정/삭제/조회 등에 사용(데이터)
	@Override 
	public Post getPost(Long pSeq) {
		return postRepository.findById(pSeq)
							 .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
	}
	
	// 게시물 수정
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
	
	// 게시물 삭제
	@Override
	public void deletePost(Long pSeq) {
		postRepository.deleteById(pSeq);
		
	}
	
	// 게시물 저장
	@Override
	public void savePost(PostDTO postDto) {
		Post post = new Post();
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		
		postRepository.save(post);
	}
	
	// 페이지 번호 유효성 검증 메서드
	// 페이지 번호가 1보다 작은값으로 전달 혹은 총페이지 수 초과 시 문제에 대한 대응
	private Integer validatePageNum(Integer pageNum) {
		return (pageNum == null || pageNum < 1) ? 1 : pageNum;
	}
	
	// 페이징 처리
	@Override
	public List<PostDTO> getPostList(Integer pageNum) {
		pageNum = validatePageNum(pageNum); // 검증 로직 호출
		int pageSize = 10; // 페이지당 게시물 수 10개
		Pageable pageable = PageRequest.of(pageNum -1, pageSize);
		
		// 일반(isNotice = false)만 필터링하고 페이지 요청해서 게시물 목록 가저옴
		Page<Post> postPage = postRepository.findByIsNoticeFalse(pageable); 
		
		return postPage.getContent().stream()						// 페이지(page<post>)에서 게시물 목록 가져와(추출)
									.map(post -> new PostDTO(post)) // post 객체를 postDTO 객체로 변환
									.collect(Collectors.toList());	// List<PostDTO>로 return
	}
	
	// 전체 페이지 번호 처리
	@Override
	public Integer[] getPageList() {
		
		int pageSize = 10;
		int totalPosts = (int)postRepository.count(); // 전체게시물 수
		int totalPages = (int)Math.ceil((double) totalPosts/pageSize); // 전체 페이지 수 계산(올림처리) 
		
		if (totalPages == 0) {
			totalPages = 1; // 최소 1페이지 유지
		}
		
		Integer[] pageList = new Integer[totalPages]; // 페이지 수만큼 배열
		for (int i = 0; i < totalPages; i++) { // 전체 페이지 수만큼 반복
			pageList[i] = i + 1; // 페이지 번호 1부터 시작
		}
		return pageList;
	}
	
	// 공지사항 페이징 처리
	@Override
	public List<PostDTO> findNotices(Integer pageNum) {
		pageNum = validatePageNum(pageNum); // 검증 로직 호출
		int pageSize = 10; // 페이지당 게시물 수 10개
		Pageable pageable = PageRequest.of(pageNum -1, pageSize);
		// 공지(isNotice = True)만 필터링하고 페이지 요청해서 게시물 목록 가저옴
		Page<Post> postPage = postRepository.findByIsNoticeTrue(pageable); 
		
		return postPage.getContent().stream()						// 페이지(page<post>)에서 게시물 목록 가져와(추출)
									.map(post -> new PostDTO(post)) // post 객체를 postDTO 객체로 변환
									.collect(Collectors.toList());	// List<PostDTO>로 return
	}

}
