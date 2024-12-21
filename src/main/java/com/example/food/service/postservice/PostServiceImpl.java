package com.example.food.service.postservice;

import java.util.ArrayList;
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

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // @Slf4j 추가

@Slf4j 
@Service
@AllArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;

    
    // 게시물 조회 등에 사용(데이터)
    @Override 
    public Post getPost(Long pSeq) {
        log.info("게시물 조회 요청, pSeq: {}", pSeq);
        
        Post post = postRepo.findById(pSeq)
                            .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        
        log.info("게시물 조회 완료, pSeq: {}", pSeq);
        return post;
    }
    
    
    // 게시물 상세조회(화면표시)
    @Override 
    public PostDTO getPostById(Long pSeq) {
        log.info("게시물 상세조회 요청, pSeq: {}", pSeq);
        
        Post post = postRepo.findById(pSeq)
                            .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        
        log.info("게시물 상세조회 완료, pSeq: {}", pSeq);
        return new PostDTO(post);
    }
    

    // 게시물 수정
    @Override
    public void updatePost(PostDTO postDto/*,HttpSession session*/) {
        log.info("게시물 수정 요청, pSeq: {}", postDto.getPSeq());
        
        /*
        // 세션에서 로그인한 사용자 정보 가져오기 >> Users 지정 문제 있을수 있음
        String Users = (String) session.getAttribute("Users"); // 세션에서 사용자 정보를 가져옴

        if (Users == null) {
            throw new IllegalArgumentException("사용자가 로그인하지 않았습니다.");
        }
        */
        
        // 고정 사용자 설정
        Users user = userRepo.findById("test_user")
                             .orElseGet(() -> {
                                 // 고정 사용자가 없으면 새로 생성하여 저장
                                 Users newUser = new Users();
                                 newUser.setUserId("test_user");
                                 newUser.setName("테스트유저");
                                 newUser.setGender(Gender.MALE);
                                 newUser.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
                                 return userRepo.save(newUser);
                             });

        Post post = postRepo.findById(postDto.getPSeq())
                            .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setPriority(postDto.getPriority());
        post.setCnt(postDto.getCnt());
        post.setImagePaths(postDto.getImagePaths()); // 수정된 이미지경로 추가
        

        post.setUser(user); // Post 엔티티에 고정 사용자 설정
        
        postRepo.save(post);
        log.info("게시물 수정 완료, pSeq: {}", postDto.getPSeq());
    }
    
    // 게시물 삭제
    @Override
    public void deletePost(Long pSeq) {
        log.info("게시물 삭제 요청, pSeq: {}", pSeq);
        
        postRepo.deleteById(pSeq);
        log.info("게시물 삭제 완료, pSeq: {}", pSeq);
    }
    
    // 게시물 저장
    @Override
    public void savePost(PostDTO postDto/*,HttpSesstion session*/) {
        log.info("게시물 저장 요청, 제목: {}, 내용: {}", postDto.getTitle(), postDto.getContent());
        
        /* 고정사용자 삭제시 활성화 시켜야함
    	// 세션에서 로그인한 사용자 정보 가져오기 >> Users 문제 있을수있음
    	String Users = (String) session.getAttribute("Users"); // 세션에서 사용자 정보를 가져옴

    	if (Users == null) {
        	throw new IllegalArgumentException("사용자가 로그인하지 않았습니다.");
    	}
      
        // 작성자 정보 확인
        Users user = userRepo.findById(postDto.getUserId())
                             .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음"));
        */
        
        // 고정 사용자 설정
        Users user = userRepo.findById("test_user")
                             .orElseGet(() -> {
                                 // 고정 사용자가 없으면 새로 생성하여 저장
                                 Users newUser = new Users();
                                 newUser.setUserId("test_user");
                                 newUser.setName("테스트유저");
                                 newUser.setGender(Gender.MALE);
                                 newUser.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
                                 return userRepo.save(newUser);
                             });
        
        log.info("사용자 정보 확인, ID: {}, Role: {}", user.getUserId(), user.getRole());

        //공지글 권한 확인
        if (postDto.getIsNotice() && user.getRole() !=Users.Role.ROLE_ADMIN) {
            log.warn("공지글 저장 권한 없음: {}", user.getUserId());
            throw new IllegalArgumentException("공지글은 관리자만 작성할 수 있습니다.");
        }
        
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setPriority(postDto.getPriority());
        post.setCnt(postDto.getCnt());
        post.setImagePaths(postDto.getImagePaths()); // 이미지경로 포함
        post.setIsNotice(postDto.getIsNotice() != null ? postDto.getIsNotice() : false); // 기본값 false
        post.setUser(user); // Post 엔티티에 로그인된 사용자 설정
        
        postRepo.save(post);
        log.info("게시물 저장 완료, pSeq: {}", post.getPSeq());
    }
    
    // 페이지 번호 유효성 검증 메서드
    private Integer validatePageNum(Integer pageNum) {
        return (pageNum == null || pageNum < 1) ? 1 : pageNum;
    }
    
    // 페이징 처리
    @Override
    public List<PostDTO> getPostList(Integer pageNum) {
        pageNum = validatePageNum(pageNum); 
        int pageSize = 10; // 페이지당 게시물 수
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        log.info("전체 게시물(공지사항 포함) 페이징 처리 요청, pageNum: {}", pageNum);

        // 공지사항과 일반 게시물을 통합하여 페이징 처리
        Page<Post> postPage = postRepo.findAllPosts(pageable);

        // Page<Post> 객체를 List<PostDTO>로 변환
        List<PostDTO> postList = postPage.getContent().stream()
                                         .map(PostDTO::new)
                                         .collect(Collectors.toList());

        log.info("전체 게시물 페이징 처리 완료, 페이지: {}, 게시물 개수: {}", pageNum, postList.size());
        return postList;
    }
    
    // 페이징 목록 조회
    @Override
    public Integer[] getPageList() {
        log.info("전체 페이지 목록 조회 요청");

        int pageSize =10; // 페이지당 게시물 수
        long totalPosts = postRepo.count(); // 전체 게시물 수
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);

        // 최소 1페이지는 유지
        if (totalPages == 0) {
            totalPages = 1;
        }

        Integer[] pageList = new Integer[totalPages];
        for (int i = 0; i < totalPages; i++) {
            pageList[i] = i + 1; // 페이지 번호 1부터 시작
        }

        log.info("전체 페이지 목록 조회 완료, 총 페이지 수: {}", totalPages);
        return pageList;
    }


    
    // keyword로 게시글 검색
    @Override
    public List<PostDTO> searchPostsByKeyword(String keyword, Integer pageNum) {
        log.info("게시물 검색 요청, keyword: {}, pageNum: {}", keyword, pageNum);
        
        //페이징처리
        Pageable pageable = PageRequest.of(pageNum - 1, 10);
        
        // 제목, 내용에 키워드가 포함된 게시물 검색
        Page<Post> posts = postRepo.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        
        // 검색 결과가 없을떄
        if (posts == null || posts.isEmpty()) {
        	log.info("검색된 게시물이 없습니다. keyword: {}", keyword);
        	return new ArrayList<>(); // 빈 리스트 전달
        }
        
        // 검색된 게시물목록 PostDTO로 
        List<PostDTO> postDtos = posts.stream()
        							  .map(PostDTO::new)
        							  .collect(Collectors.toList());
        log.info("게시물 검색 완료, keyword: {}, 결과 개수: {}", keyword, posts.getTotalElements());
        
        return postDtos;
    }

    
    // 게시물 조회수
	@Override
	public void viewCount(Long pSeq) {
		// pSeq로 게시물 조회
		Post post = postRepo.findById(pSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
		
		// 조회수 증가
		post.setCnt(post.getCnt() + 1);
		
		postRepo.save(post);
		
	}
}
