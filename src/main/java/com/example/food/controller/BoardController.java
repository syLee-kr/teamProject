package com.example.food.controller;

import com.example.food.entity.Post;
import com.example.food.entity.Comments;
import com.example.food.entity.Users;
import com.example.food.service.AuthenticationService;
import com.example.food.service.postservice.PostService;
import com.example.food.service.postservice.CommentService;
import com.example.food.service.GcsUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

	private final PostService postService;
	private final CommentService commentService;
	private final AuthenticationService authenticationService;
	private final GcsUploadService gcsUploadService;

	public BoardController(PostService postService,
						   CommentService commentService,
						   AuthenticationService authenticationService,
						   GcsUploadService gcsUploadService) {
		this.postService = postService;
		this.commentService = commentService;
		this.authenticationService = authenticationService;
		this.gcsUploadService = gcsUploadService;
	}

	@GetMapping
	public String listPosts(@RequestParam(value = "page", defaultValue = "1") Integer pageNum,
							@RequestParam(value = "size", defaultValue = "10") Integer size,
							@RequestParam(value = "keyword", required = false) String keyword,
							Model model) {
		logger.info("[게시판 목록] 페이지 번호: {}, 페이지 사이즈: {}, 검색어: {}", pageNum, size, keyword == null ? "(없음)" : keyword);

		Users user = authenticationService.getLoggedInUser();
		if (user == null) {
			logger.warn("[게시글 작성] 인증되지 않은 사용자");
			throw new AccessDeniedException("로그인이 필요합니다.");
		}

		// 공지글 조회
		List<Post> isNoticePosts = postService.getNoticePosts(keyword);
		logger.debug("[게시판 목록] 공지글 개수: {}", isNoticePosts.size());

		// 일반글 페이징 조회
		Pageable pageable = PageRequest.of(pageNum - 1, size);
		Page<Post> postPage = postService.getRegularPosts(keyword, pageable);
		List<Post> postList = postPage.getContent();
		int totalPages = postPage.getTotalPages();
		List<Integer> pageList = postService.getPageList(pageNum, totalPages);

		// 모델에 데이터 추가
		model.addAttribute("isNoticePosts", isNoticePosts);
		model.addAttribute("postList", postList);
		model.addAttribute("pageList", pageList);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("user", user);

		// 이전/다음 페이지 계산
		Integer prevPage = (pageNum > 1) ? pageNum - 1 : null;
		Integer nextPage = (pageNum < totalPages) ? pageNum + 1 : null;
		model.addAttribute("prevPage", prevPage);
		model.addAttribute("nextPage", nextPage);

		logger.info("[게시판 목록] 목록 조회 완료. 현재 페이지: {}, 다음 페이지: {}, 이전 페이지: {}",
				pageNum, nextPage, prevPage);

		return "user/board/postList"; // resources/templates/user/board/postList.html
	}

	@GetMapping("/new")
	public String createPostForm() {
		Users user = authenticationService.getLoggedInUser();
		if (user == null) {
			logger.warn("[게시글 작성 화면] 인증되지 않은 사용자 접근");
			throw new AccessDeniedException("로그인이 필요합니다.");
		}
		logger.info("[게시글 작성 화면] 사용자: {}", user.getUserId());
		return "user/board/createPostForm";
	}

	@PostMapping
	public String createPost(@RequestParam String title,
							 @RequestParam String content,
							 @RequestParam(required = false) Boolean isNotice,
							 @RequestParam(value = "images", required = false) MultipartFile[] images) {
		Users user = authenticationService.getLoggedInUser();
		if (user == null) {
			logger.warn("[게시글 작성] 인증되지 않은 사용자");
			throw new AccessDeniedException("로그인이 필요합니다.");
		}
		logger.info("[게시글 작성] 제목: {}, 작성자: {}", title, user.getUserId());

		boolean noticeFlag = (isNotice != null && isNotice);
		if (noticeFlag && user.getRole() != Users.Role.ROLE_ADMIN) {
			logger.warn("[게시글 작성] 사용자: {}는 공지글 작성 권한이 없습니다.", user.getUserId());
			throw new AccessDeniedException("공지글은 관리자만 작성할 수 있습니다.");
		}

		List<String> imagePaths = new ArrayList<>();
		if (images != null) {
			for (MultipartFile file : images) {
				if (file != null && !file.isEmpty()) {
					try {
						String uploadedUrl = gcsUploadService.uploadFile(file);
						if (uploadedUrl != null) {
							imagePaths.add(uploadedUrl);
							logger.info("[게시글 작성] GCS 업로드 성공 -> {}", uploadedUrl);
						}
					} catch (IOException e) {
						logger.error("[게시글 작성] 이미지 업로드 실패: {}", e.getMessage());
					}
				}
			}
		}

		Post post = new Post();
		post.setTitle(title);
		post.setContent(content);
		post.setIsNotice(noticeFlag);
		post.setPriority(0);
		post.setCnt(0L);
		post.setUser(user);
		post.setImagePaths(imagePaths);

		Post saved = postService.createPost(post);
		logger.info("[게시글 작성] 저장 완료. pSeq: {}", saved.getPSeq());

		return "redirect:/board";
	}

	@GetMapping("/{pSeq}")
	public String getPostDetail(@PathVariable Long pSeq, Model model) {
		logger.info("[게시글 상세] pSeq: {}", pSeq);

		Post post = postService.getPostDetail(pSeq);
		model.addAttribute("post", post);

		List<Comments> comments = commentService.getCommentsByPost(pSeq);
		model.addAttribute("comments", comments);

		logger.info("[게시글 상세] 조회 성공. 댓글 수: {}", comments.size());
		return "user/board/postDetail";
	}

	@PostMapping("/{pSeq}/delete")
	public String deletePost(@PathVariable Long pSeq, Model model) {
		Users user = authenticationService.getLoggedInUser();

		Post post = postService.getPostDetail(pSeq);
		if (post == null) {
			logger.warn("[게시글 삭제] 존재하지 않는 게시글 pSeq: {}", pSeq);
			return "redirect:/board";
		}

		boolean isAdmin = (user.getRole() == Users.Role.ROLE_ADMIN);
		boolean isOwner = post.getUser().getUserId().equals(user.getUserId());
		if (!isAdmin && !isOwner) {
			model.addAttribute("errorMessage", "삭제 권한이 없습니다.");
			return "redirect:/board";
		}

		postService.deletePost(pSeq);
		logger.info("[게시글 삭제] pSeq: {} 삭제 완료", pSeq);
		model.addAttribute("message", "게시글이 삭제 되었습니다.");
		return "redirect:/board";
	}

	@PostMapping("/{pSeq}/comments")
	public String createComment(@PathVariable Long pSeq,
								@RequestParam String content) {
		Users user = authenticationService.getLoggedInUser();
		if (user == null) {
			logger.warn("[댓글 작성] 인증되지 않은 사용자. 게시글 pSeq: {}", pSeq);
			throw new AccessDeniedException("로그인이 필요합니다.");
		}

		logger.info("[댓글 작성] 게시글: {}, 작성자: {}, 내용: {}", pSeq, user.getUserId(), content);

		commentService.createComment(pSeq, content, user);

		return "redirect:/board/" + pSeq;
	}

	@PostMapping("/comments/{cSeq}/delete")
	public String deleteComment(@PathVariable Long cSeq,
								@RequestParam Long postId) {
		Users user = authenticationService.getLoggedInUser();
		if (user == null) {
			logger.warn("[댓글 삭제] 인증되지 않은 사용자. cSeq: {}", cSeq);
			throw new AccessDeniedException("로그인이 필요합니다.");
		}

		logger.info("[댓글 삭제] cSeq: {}, 요청 사용자: {}", cSeq, user.getUserId());

		List<Comments> commentList = commentService.getCommentsByPost(postId);
		Comments targetComment = commentList.stream()
				.filter(c -> c.getCSeq().equals(cSeq))
				.findFirst()
				.orElse(null);

		if (targetComment == null) {
			logger.warn("[댓글 삭제] 존재하지 않는 댓글 cSeq: {}", cSeq);
			return "redirect:/board/" + postId;
		}

		boolean isAdmin = (user.getRole() == Users.Role.ROLE_ADMIN);
		boolean isOwner = targetComment.getUser().getUserId().equals(user.getUserId());
		if (!isAdmin && !isOwner) {
			logger.warn("[댓글 삭제] 권한 없음. 요청자: {}, 댓글 작성자: {}", user.getUserId(), targetComment.getUser().getUserId());
			throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
		}

		commentService.deleteComment(cSeq);
		logger.info("[댓글 삭제] cSeq: {} 삭제 완료", cSeq);

		return "redirect:/board/" + postId;
	}

	@GetMapping("/{pSeq}/edit")
	public String editPostForm(@PathVariable Long pSeq, Model model) {
		Users user = authenticationService.getLoggedInUser();
		if (user == null) {
			logger.warn("[게시글 수정 화면] 인증되지 않은 사용자 접근");
			throw new AccessDeniedException("로그인이 필요합니다.");
		}

		Post post = postService.getPostDetail(pSeq);
		if (post == null) {
			logger.warn("[게시글 수정 화면] 존재하지 않는 게시글 pSeq: {}", pSeq);
			return "redirect:/board";
		}

		boolean isAdmin = (user.getRole() == Users.Role.ROLE_ADMIN);
		boolean isOwner = post.getUser().getUserId().equals(user.getUserId());
		if (!isAdmin && !isOwner) {
			logger.warn("[게시글 수정 화면] 권한 없음. 요청자: {}, 작성자: {}", user.getUserId(), post.getUser().getUserId());
			throw new AccessDeniedException("수정 권한이 없습니다.");
		}

		model.addAttribute("post", post);
		model.addAttribute("user", user);

		return "user/board/createPostForm";
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> downloadFile(@RequestParam String fileUrl) {
		try {
			// GCS에서의 파일 URL을 리소스로 변환
			Path filePath = Paths.get(fileUrl);
			Resource resource = new UrlResource(filePath.toUri());

			if (resource.exists() || resource.isReadable()) {
				String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
						.body(resource);
			} else {
				logger.error("[파일 다운로드] 파일을 찾을 수 없거나 읽을 수 없습니다. URL: {}", fileUrl);
				return ResponseEntity.notFound().build();
			}
		} catch (MalformedURLException e) {
			logger.error("[파일 다운로드] URL 변환 오류: {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	private String saveImage(MultipartFile image) throws IOException {
		if (image == null || image.isEmpty()) {
			logger.debug("[이미지 저장] 비어있는 파일이므로 처리하지 않음");
			return null;
		}

		String uploadDir = System.getProperty("user.dir") + "/uploadImg/";
		logger.info("[이미지 저장] 파일 저장 시작. 경로: {}", uploadDir);

		File directory = new File(uploadDir);
		if (!directory.exists()) {
			directory.mkdirs();
			logger.debug("[이미지 저장] uploadImg 디렉토리를 생성함");
		}

		String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
		File dest = new File(uploadDir + fileName);
		image.transferTo(dest);

		logger.info("[이미지 저장] 파일 저장 완료. 실제 경로: {}", dest.getAbsolutePath());
		return "/uploadImg/" + fileName;
	}
}
