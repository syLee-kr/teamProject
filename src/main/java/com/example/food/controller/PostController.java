package com.example.food.controller;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.food.CommentDTO;
import com.example.food.PostDTO;
import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.domain.Users.Gender;
import com.example.food.domain.Users.Role;
import com.example.food.service.postservice.PostService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Controller
@AllArgsConstructor
@RequestMapping("/post")
public class PostController {
	
	private PostService postService;

	/*
	 * 게시물 목록
	 */
	@GetMapping("/list")
	public String PostList(Model model, @RequestParam(value="page", defaultValue="1") Integer pageNum,
										@RequestParam(value="keyword", required = false) String keyword){
		log.info("게시물 목록 조회 요청, pageNum: {}, keyword: {}", pageNum, keyword);
		
		List<PostDTO> postList = postService.getPostList(pageNum);    // 일반글 목록
		List<PostDTO> isNoticePosts = postService.getIsNoticePosts(); // 공지글 목록
		Integer totalPages = postService.getTotalPages();  // 전체 페이지 수
		Integer[] pageList = postService.getPageList(); // 전체 페이지 번호 생성
		
	    if (keyword != null && !keyword.isEmpty()) {
	        // 검색어가 있을 경우, 제목 또는 내용에 keyword가 포함된 게시물 검색
	        postList = postService.searchPostsByKeyword(keyword, pageNum);
	        totalPages = postService.getTotalPagesForSearch(keyword);  // 검색된 게시물에 대한 총 페이지 수 계산
	        pageList = postService.getPageListForSearch(keyword, pageNum);  // 검색된 게시물에 맞는 페이지 번호 목록 생성
	    }else {
	    	// 검색어가 없으면 게시글 페이징 처리
	    	postList = postService.getPostList(pageNum);
	    	totalPages = postService.getTotalPages();  // 전체 게시물에 대한 총 페이지 수 계산
	        pageList = postService.getPageList();  // 전체 게시물에 대한 페이지 번호 목록 생성
	    }
	    
		
		// 날짜 포맷 처리
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
	    postList.forEach(post -> {
	        if (post.getPostdate() != null) {
	            String formattedDate = post.getPostdate().format(formatter);
	            post.setFormattedPostdate(formattedDate); // 포맷된 날짜를 PostDTO 객체에 저장
	        }
	    });
		
	    
	
	    
		model.addAttribute("postList", postList);
		model.addAttribute("isNoticePosts", isNoticePosts); // 공지 목록
		model.addAttribute("pageList", pageList); // 페이지 번호 목록
		model.addAttribute("totalPages", totalPages); // 전체 페이지 수
		model.addAttribute("pageNum", pageNum); // 현재 페이지
		model.addAttribute("keyword", keyword); // 검색어 전달
		
	    // 이전/다음 페이지 계산 (현재 페이지가 1보다 크면 이전 페이지로, 총 페이지 수보다 작으면 다음 페이지로 이동)
	    Integer prevPage = (pageNum > 1) ? pageNum - 1 : null; // 이전 페이지 번호(pageNum 이 1일 경우 null)
	    Integer nextPage = (pageNum <= totalPages) ? pageNum + 1 : totalPages; // 다음 페이지 번호
		
	    model.addAttribute("prevPage", prevPage);
	    model.addAttribute("nextPage", nextPage);
		
		
	    log.info("공지글 수: {}", isNoticePosts.size());
	    log.info("일반글 수: {}", postList.size());
		
		return "post/list";

	}
    /*
	@PostMapping("/list")
    public String PostListRedirect() {
        // 게시물 작성 후 /post/list로 리다이렉트
        return "redirect:/post/list";
    }*/
	
	/*
	 * 게시물 작성 페이지
	 */
	@GetMapping("/write")
	public String write(/*HttpSession session, Model model*/) {
        
		/* 고정사용자 삭제 시 활성화
		// 세션에서 로그인된 사용자 정보 가져오기
        Users user = (Users) session.getAttribute("user");  // 세션에서 "user" 객체를 가져옴

        if (user != null) {
            model.addAttribute("user", user);  // 세션에서 가져온 user 정보를 모델에 추가
        } else {
            // 로그인되지 않은 경우 처리 (필요에 따라 로그인 페이지로 리다이렉트)
            return "redirect:/login/login";
        }*/
		
		return "post/write";
	}
	
	/*
	 * 게시물 작성 처리
	 */
	@PostMapping("/write")
	public String write(@RequestParam String title,
					    @RequestParam String content,
					    @RequestParam(value="isNotice", defaultValue="false") Boolean isNotice,
					    @RequestParam MultipartFile[] images)/*, //테스트 사용자 제거 시 images 뒤에)/* 제거
						HttpSession session)*/ throws IOException {
								
		log.info("게시물 작성 요청, 제목: {}, 내용: {}, isNotice: {}", title, content, isNotice);
	
		/* 세선 사용시    
		// 세션에서 로그인 사용자 정보 가져오기
	    Users user = (Users) session.getAttribute("user");
	    if (user == null) {
	        log.warn("로그인 사용자 정보 없음");
	        throw new IllegalArgumentException("로그인이 필요합니다.");
	    } */
		 
		// 테스트용 고정된 사용자(고정 사용자만 지울것)
		Users user = new Users();
		user.setUserId("test_user"); //로그인한 사용자 ID 설정
		user.setName("테스트유저");
		user.setGender(Gender.MALE);
		user.setRole(Role.ROLE_ADMIN); 
		
		 // 로그인된 사용자 정보 확인
		log.info("로그인 사용자: {}, Role: {}", user.getUserId(), user.getRole());
		
		
		// 관지라 권한 확인
		if (isNotice && user.getRole() !=Users.Role.ROLE_ADMIN) {
			log.warn("공지글 작성 권한 없음: {}", user.getUserId());
			throw new IllegalArgumentException("공지글은 관리자만 작성할 수 있습니다.");
		}
		 
		 List<String> imagePaths = new ArrayList<>();
		
		// 이미지 파일 서버에 저장(이미지가 있을때만)
		for (MultipartFile image : images) {
			if (image != null && !image.isEmpty()) {
				String imagePath = saveImage(image);
				if(imagePath != null) {
					imagePaths.add(imagePath);
				}
			}
		}
		
		// 게시물 저장
		PostDTO postDto = new PostDTO();
		postDto.setTitle(title);
		postDto.setContent(content);
		postDto.setImagePaths(imagePaths); // 이미지 경로
		postDto.setIsNotice(isNotice != null ? isNotice : false);  // 공지여부
		
		postDto.setUserId(user.getUserId()); //로그인한 사용자 ID 설정
		postDto.setUserName(user.getName());
		
		log.info("게시물 DTO 생성 완료, 사용자: {}", user.getUserId());
		
		postService.savePost(postDto);
		
		log.info("게시물 저장 완료");

		return "redirect:/post/list";
	}
	
	// 이미지 저장
	private String saveImage(MultipartFile image) throws IOException {
		if(!image.isEmpty()) {
			String uploadDir = System.getProperty("user.dir") + "/uploadImg/"; // 애플리케이션의 루트 디렉토리 기준으로 설정
			log.info("이미지 파일 저장 시작, 경로: {}", uploadDir);
			
			//이미지 디렉토리 존재여부 체크/없으면 생성
			File directory = new File(uploadDir);
			if(!directory.exists()) {
				directory.mkdirs(); //디렉토리 생성
			}
			
			// 파일 명 생성(현재시간 + 원본파일명)
			String fileName =System.currentTimeMillis() + "_" + image.getOriginalFilename();
			File file = new File(uploadDir + fileName);
			
			image.transferTo(file); // 파일 서버에 저장
			
			log.info("이미지 파일 저장 완료, 파일명: {}", fileName);
			
			return "/uploadImg/" + fileName;// (웹에서 접근 할 수 있는 상대경로)
		}
		log.warn("이미지 파일이 비어있음");
		return null;  // 이미지 없으며 null
	}
	
	/*
	 * 게시물 상세보기
	 */
	@GetMapping("/detail/{pSeq}")
	public String detail(@PathVariable Long pSeq, Model model) {
		log.info("게시물 상세보기 요청, pSeq: {}", pSeq);
		
		// 게시물 조회 후 조회수 증가
		PostDTO postDto = postService.getPostById(pSeq);
		
		// 게시물 조회 후  조회수 1증가 처리
		postService.viewCount(pSeq);
		if (postDto == null) {
            log.error("게시물 조회 실패, 해당 게시물 없음, pSeq: {}", pSeq);
            return "redirect:/post/list";
        }
		
		model.addAttribute("post", postDto);
		
		log.info("게시물 상세보기 완료, 제목: {}", postDto.getTitle());
		
		return "post/detail";
	}
	
	
	/*
	 * 게시물 수정
	 */
	@GetMapping("/write/{pSeq}")
	public String edit(@PathVariable long pSeq, Model model) {
		
		PostDTO postDto = postService.getPostById(pSeq);
		
		model.addAttribute("post",postDto);
		
		return "post/write";
	}
	
	
	@PostMapping("/update/{pSeq}")
	public String edituUpdate(@RequestParam Long pSeq, 
						     @RequestParam String title,
						     @RequestParam String content,
						     @RequestParam(value="isNotice", defaultValue="false") Boolean isNotice,
						     @RequestParam MultipartFile[] images)/*, //테스트 사용자 제거 시 images 뒤에)/* 제거
						      HttpSession session)*/ throws IOException {
					 			
		
		log.info("게시물 수정 요청, pSeq: {}, 제목: {}, 내용: {}", pSeq, title, content);
		
	    /*
		// 세션에서 사용자 정보 가져오기
	    Users user = (Users) session.getAttribute("user");
	    
	    if (user == null) {
	        log.warn("로그인 사용자 정보 없음");
	        throw new IllegalArgumentException("로그인이 필요합니다.");
	    }*/
		
		// 테스트용 고정된 사용자(고정 사용자만 지울것)
		Users user = new Users();
		user.setUserId("test_user"); //로그인한 사용자 ID 설정
		user.setName("테스트유저");
		user.setGender(Gender.MALE);
		user.setRole(Role.ROLE_ADMIN);
		
		log.info("로그인 사용자: {}, Role: {}", user.getUserId(), user.getRole());
	
		List<String> imagePaths = new ArrayList<>();
		
		// 이미지 파일 서버에 저장(이미지가 있을때만)
		for (MultipartFile image : images) {
			if (image != null && !image.isEmpty()) { //이미지 파일 및 내용이 있는경우만
				String imagePath = saveImage(image); // 서버저장
				if(imagePath != null) {
					imagePaths.add(imagePath); // img 경로추가
					log.info("수정된 이미지 저장 완료, 경로: {}", imagePath);
				}
			}
		}

		PostDTO postDto = new PostDTO();
		postDto.setPSeq(pSeq);
		postDto.setTitle(title);
		postDto.setContent(content);
		postDto.setImagePaths(imagePaths);
		postDto.setIsNotice(isNotice);

		postDto.setUserId(user.getUserId()); //로그인한 사용자 ID 설정
		postDto.setUserName(user.getName());
		
		
		postService.updatePost(postDto);
		
		log.info("게시물 수정 완료, pSeq: {}", pSeq);
		
		return "redirect:/post/detail/" + pSeq;
		
	}
	
	/*
	 * 게시물 삭제
	 */
	@PostMapping("/delete/{pSeq}")
	public String delete(@PathVariable Long pSeq) {
		
		postService.deletePost(pSeq);
		
		log.info("게시물 삭제 완료, pSeq: {}", pSeq);
		
		return "redirect:/post/list";
	}
	
}
