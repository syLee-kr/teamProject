package com.example.food.service.postservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.food.PostDTO;
import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.domain.Users.Gender;
import com.example.food.repository.PostRepository;
import com.example.food.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostServiceImpl implements PostService{
	
	private final PostRepository postRepo;
	private final UserRepository userRepo;
	
	// 게시물 상세조회(화면표시)
	@Override 
	public PostDTO getPostById(Long pSeq) {
		
		Post post = postRepo.findById(pSeq)
								  .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		
		return new PostDTO(post);
	}
	
	// 게시물 수정/삭제/조회 등에 사용(데이터)
	@Override 
	public Post getPost(Long pSeq) {
		return postRepo.findById(pSeq)
							 .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
	}
	
	// 게시물 수정
	@Override
	public void updatePost(PostDTO postDto) {
		Post post = postRepo.findById(postDto.getPSeq())
								  .orElseThrow(()-> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		post.setImagePaths(postDto.getImagePaths()); // 수정된 이미지경로 추가
		
	    // 테스트
	    // 고정 사용자 설정 (존재하는 사용자를 조회하거나 새로 생성)
	    Users user = userRepo.findById("test_user") // 고정 사용자 ID로 조회
	                         .orElseGet(() -> {
	                             // 고정 사용자가 없으면 새로 생성하여 저장
	                             Users newUser = new Users();
	                             newUser.setUserId("test_user");  // 고정 사용자 ID
	                             newUser.setName("테스트유저");    // 고정 사용자 이름
	                             newUser.setGender(Gender.MALE);  // 성별 설정 (필수라면 설정)
	                             return userRepo.save(newUser);   // 새로 저장된 사용자를 반환
	                         });
	    post.setUser(user); // Post 엔티티에 고정 사용자 설정
		
		postRepo.save(post);
	}
	
	// 게시물 삭제
	@Override
	public void deletePost(Long pSeq) {
		postRepo.deleteById(pSeq);
		
	}
	
	// 게시물 저장
	@Override
	public void savePost(PostDTO postDto) {
		Post post = new Post();
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setPriority(postDto.getPriority());
		post.setCnt(postDto.getCnt());
		post.setImagePaths(postDto.getImagePaths()); // 이미지경로 포함
		post.setIsNotice(postDto.getIsNotice() != null ? postDto.getIsNotice() : false); // 기본값 false
		
		// 테스트용 고정 사용자
	    // 고정 사용자 설정 (존재하는 사용자를 조회하거나 새로 생성)
	    Users user = userRepo.findById("test_user") // 고정 사용자 ID로 조회
	                         .orElseGet(() -> {
	                             // 고정 사용자가 없으면 새로 생성하여 저장
	                             Users newUser = new Users();
	                             newUser.setUserId("test_user");  // 고정 사용자 ID
	                             newUser.setName("테스트유저");    // 고정 사용자 이름
	                             newUser.setGender(Gender.MALE);  // 성별 설정 (필수라면 설정)
	                             return userRepo.save(newUser);   // 새로 저장된 사용자를 반환
	                         });
		post.setUser(user);// Post 엔티티에 고정 사용자 설정
		
		postRepo.save(post);
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
		Page<Post> postPage = postRepo.findByIsNoticeFalse(pageable); 
		
		return postPage.getContent().stream()						// 페이지(page<post>)에서 게시물 목록 가져와(추출)
									.map(post -> new PostDTO(post)) // post 객체를 PostDTO 객체로 변환
									.collect(Collectors.toList());	// List<PostDTO>로 return
	}
	
	// 전체 페이지 번호 처리
	@Override
	public Integer[] getPageList() {
		
		int pageSize = 10;
		int totalPosts = (int)postRepo.count(); // 전체게시물 수
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
		Page<Post> postPage = postRepo.findByIsNoticeTrue(pageable); 
		
		return postPage.getContent().stream()						// 페이지(page<post>)에서 게시물 목록 가져와(추출)
									.map(post -> new PostDTO(post)) // post 객체를 PostDTO 객체로 변환
									.collect(Collectors.toList());	// List<PostDTO>로 return
	}

}
