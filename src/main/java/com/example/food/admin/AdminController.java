package com.example.food.admin;

import com.example.food.entity.Answer;
import com.example.food.entity.Post;
import com.example.food.entity.Question;
import com.example.food.entity.Users;
import com.example.food.service.AuthenticationService;
import com.example.food.service.postservice.PostServiceImpl;
import com.example.food.service.qnaservice.AnswerService;
import com.example.food.service.qnaservice.QuestionService;
import com.example.food.service.userservice.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final AdminQnaService adminQnaService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final AuthenticationService authenticationService;
    private final PostServiceImpl postService;

    @Autowired
    public AdminController(UserService userService,
                           AdminQnaService adminQnaService,
                           QuestionService questionService,
                           AuthenticationService authenticationService,
                           AnswerService answerService,
                           PostServiceImpl postService) {
        this.userService = userService;
        this.adminQnaService = adminQnaService;
        this.questionService = questionService;
        this.answerService = answerService;
        this.authenticationService = authenticationService;
        this.postService = postService;
        logger.info("AdminController가 초기화되었습니다.");
    }

    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/login")
    public String adminLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        logger.info("관리자 로그인 페이지에 접근했습니다.");

        if (authenticationService.isAuthenticated()) {
            logger.info("이미 로그인된 관리자: {}, /admin/main으로 리다이렉트합니다.");
            return "redirect:/admin/main";
        }

        // 로그아웃 메시지 처리
        if (logout != null) {
            model.addAttribute("message", "로그아웃이 완료되었습니다.");
            logger.info("로그아웃 완료 메시지를 모델에 추가했습니다.");
        }

        // 로그인 오류 메시지 처리
        if (error != null) {
            logger.warn("관리자 로그인 오류 발생: 아이디 또는 비밀번호가 잘못되었습니다.");
            model.addAttribute("loginFail", true);
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return "admin/login"; // 로그인 페이지 템플릿 이름
    }

    /**
     * 관리자 메인 페이지
     */


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/main")
    public String adminMain(
            @Qualifier("userPageable") @PageableDefault(size = 4, sort = "userId") Pageable userPageable,
            @Qualifier("postPageable") @PageableDefault(size = 15, sort = "pSeq") Pageable postPageable,
            Model model) {
        logger.info("관리자 메인 페이지에 접근했습니다.");

        // 사용자 목록 조회
        Page<Users> allUsers = userService.getAllUsers(userPageable);
        model.addAttribute("allUser", allUsers);
        logger.info("총 {}명의 사용자를 페이지당 {}개씩 가져왔습니다.", allUsers.getTotalElements(), userPageable.getPageSize());

        // QnA 목록 추가
        List<UserQnaDto> userQnaList = adminQnaService.getUserQnaList();
        model.addAttribute("userQnaList", userQnaList);
        logger.info("총 {}개의 사용자 QnA를 가져왔습니다.", userQnaList.size());

        // 게시글 목록 추가
        Page<Post> posts = postService.getAllPosts(null, postPageable);
        model.addAttribute("posts", posts);
        logger.info("총 {}개의 게시글을 페이지당 {}개씩 가져왔습니다.", posts.getTotalElements(), postPageable.getPageSize());

        return "admin/main";
    }


    /**
     * 관리자 QnA 상세 페이지
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/qna/chat/{userId}")
    public String viewUserQna(@PathVariable String userId, Model model) {
        logger.info("사용자 ID {}의 QnA 상세 페이지에 접근했습니다.", userId);

        // 사용자의 모든 질문 조회
        List<Question> questions = questionService.getAllUserQuestions(userId);
        logger.info("사용자 {}의 질문 {}개를 가져왔습니다.", userId, questions.size());

        // 각 질문의 답변을 등록일 기준으로 정렬
        questions.forEach(question -> {
            List<Answer> answers = question.getAnswers();
            if (answers != null) {
                answers.sort(Comparator.comparing(Answer::getRegDate));
                logger.info("질문 ID {}의 답변을 정렬했습니다. 답변 개수: {}", question.getQSeq(), answers.size());
            }
        });

        model.addAttribute("questions", questions);

        // 사용자 정보 조회
        Users optionalUser = userService.getUser(userId);
        if (optionalUser != null) {
            Users user = optionalUser;
            model.addAttribute("selectedUser", user);
            logger.info("사용자 정보 조회 성공: {}", userId);
        } else {
            logger.warn("사용자 정보 조회 실패: {}", userId);
        }

        // 사이드바용 QnA 목록 추가
        List<UserQnaDto> userQnaList = adminQnaService.getUserQnaList();
        model.addAttribute("userQnaList", userQnaList);
        logger.info("사이드바용 QnA 목록을 추가했습니다. 총 {}개.", userQnaList.size());

        return "admin/qnaDetail"; // 상세 페이지 템플릿
    }

    /**
     * 관리자 답변 추가
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/qna/answer")
    public String createAnswer(
            @RequestParam Long qSeq,
            @RequestParam String content
    ) {
        logger.info("질문 ID {}에 대한 답변을 생성하려고 합니다.", qSeq);

        // 인증된 관리자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUserId = authentication.getName(); // username은 userId로 설정됨
        Users optionalAdmin = userService.getUser(adminUserId);

        // Spring Security가 이미 ADMIN 역할을 보장하므로 추가 역할 검사는 불필요
        // 단, adminUserId에 해당하는 사용자가 존재하는지 확인
        if (optionalAdmin == null) {
            logger.error("답변 생성 실패: 관리자 정보를 찾을 수 없습니다. 관리자 ID: {}", adminUserId);
            return "redirect:/admin/login"; // 로그인 페이지로 리다이렉트
        }

        Users admin = optionalAdmin;

        try {
            answerService.createAnswer(qSeq, content, admin);
            logger.info("질문 ID {}에 대한 답변을 성공적으로 생성했습니다.", qSeq);
        } catch (Exception e) {
            logger.error("답변 생성 중 오류 발생: {}", e.getMessage());
            return "redirect:/admin/main"; // 오류 발생 시 메인 페이지로 리다이렉트
        }

        // 질문 상세 정보 조회 후 사용자 ID 가져오기
        Optional<Question> optionalQuestion = questionService.getQuestionDetail(qSeq);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            String userId = question.getUser().getUserId();
            logger.info("질문 ID {}의 사용자 ID는 {}입니다. 해당 사용자 QnA 상세 페이지로 이동합니다.", qSeq, userId);
            return "redirect:/admin/qna/chat/" + userId;
        }

        logger.warn("질문 ID {}에 대한 상세 정보를 찾을 수 없습니다. 메인 페이지로 이동합니다.", qSeq);
        return "redirect:/admin/main"; // 기본 QnA 목록 페이지로 리다이렉트
    }

    /**
     * 사용자 프로필 페이지 접근
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile/{id}")
    public String profileUser(@PathVariable("id") String userId, Model model) {
        logger.info("사용자 ID {}의 프로필 페이지에 접근했습니다.", userId);
        Users optionalUser = userService.getUser(userId);
        if (optionalUser != null) {
            Users user = optionalUser;
            model.addAttribute("user", user);
            logger.info("사용자 ID {}의 프로필 정보를 추가했습니다.", userId);
        } else {
            logger.warn("사용자 ID {}의 정보를 찾을 수 없습니다.", userId);
        }
        return "admin/profiles";
    }

    /**
     * 사용자 삭제
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") String userId, RedirectAttributes redirectAttributes) {
        logger.info("사용자 ID {}를 삭제하려고 합니다.", userId);

        try {
            Users user = userService.getUser(userId); // us.getUser(userId) 메서드가 Optional<Users>를 반환한다고 가정

            if (user != null) {
                userService.deleteUser(user);
                logger.info("사용자 ID {}를 성공적으로 삭제했습니다.", userId);
                redirectAttributes.addFlashAttribute("message", "사용자가 성공적으로 삭제되었습니다.");
            } else {
                logger.warn("사용자 ID {}를 삭제하려 했으나, 사용자가 존재하지 않습니다.", userId);
                redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            logger.error("사용자 ID {} 삭제 중 오류 발생: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "사용자 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/main";
    }
    @GetMapping("/posts/detail/{id}")
    public String getPostDetail(@PathVariable("id") Long id, Model model) {
        // 게시글 조회
        Post post = postService.getPostDetail(id);

        // 모델에 게시글 추가
        model.addAttribute("post", post);

        // 뷰 이름 반환
        return "admin/adminPostDetail";  // admin/adminPostDetail.html로 렌더링
    }

    @GetMapping("/posts/write")
    public String getPostWrite(Model model) {
        model.addAttribute("post", new Post());  // 빈 객체 초기화
        return "admin/adminCreatePostForm";
    }


    @PostMapping("/posts/save")
    public String createPost(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        try {
            // 현재 인증된 사용자(관리자) 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminUserId = authentication.getName(); // 인증된 사용자의 userId를 가져옴

            Users admin = userService.getUser(adminUserId);
            if (admin == null) {
                redirectAttributes.addFlashAttribute("error", "관리자 정보를 찾을 수 없습니다.");
                return "redirect:/admin/posts/write";
            }

            // 게시글 작성자 설정
            post.setUser(admin);

            // 게시글 저장
            postService.createPost(post);

            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 작성되었습니다.");
            return "redirect:/admin/main";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/posts/write";
        }
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id);  // 게시글 삭제
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/main";  // 관리자 메인 페이지로 리다이렉트
    }
}
