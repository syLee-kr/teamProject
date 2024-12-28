package com.example.food.controller;

import com.example.food.entity.Question;
import com.example.food.entity.Users;
import com.example.food.service.qnaservice.AnswerService;
import com.example.food.service.qnaservice.QuestionService;
import com.example.food.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// 필요한 추가 import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/qna")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final AuthenticationService authenticationService; // AuthenticationService 추가

    @Autowired
    public QuestionController(QuestionService questionService,
                              AnswerService answerService,
                              AuthenticationService authenticationService) { // AuthenticationService 주입
        this.questionService = questionService;
        this.answerService = answerService;
        this.authenticationService = authenticationService;
        logger.info("QuestionController가 초기화되었습니다.");
    }

    /**
     * QnA 채팅방 메인 페이지
     */
    @GetMapping
    public String listQuestions(Model model, RedirectAttributes redirectAttributes) {
        logger.info("QnA 목록 페이지에 접근했습니다.");

        // 인증된 사용자 가져오기
        Users user = authenticationService.getLoggedInUser();

        if (user == null) {
            logger.warn("사용자가 로그인하지 않았습니다. 로그인 페이지로 리다이렉트합니다.");
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login"; // Spring Security의 로그인 페이지로 리다이렉트
        }

        try {
            List<Question> questions = questionService.getAllUserQuestions(user.getUserId());
            // Collections.reverse(questions); // 최신 항목을 위에 보이도록 정렬 (필요 시 사용)
            model.addAttribute("questions", questions);
            model.addAttribute("user", user);
            logger.info("사용자 ID: {}의 QnA 목록을 성공적으로 조회했습니다. 총 질문 개수: {}", user.getUserId(), questions.size());
        } catch (Exception e) {
            logger.error("QnA 목록 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("errorMessage", "QnA 목록을 불러오는 중 문제가 발생했습니다.");
            return "error/errorPage"; // 오류 페이지로 이동 (필요에 따라 수정)
        }

        return "user/question/question"; // 렌더링할 템플릿 파일명
    }

    /**
     * 새로운 질문 생성
     */
    @PostMapping
    public String createQuestion(
            @RequestParam String content,
            RedirectAttributes redirectAttributes
    ) {
        logger.info("새로운 질문 생성 요청이 들어왔습니다.");

        // 인증된 사용자 가져오기
        Users user = authenticationService.getLoggedInUser();

        if (user == null) {
            logger.warn("질문 생성 시 사용자가 로그인되어 있지 않습니다. 로그인 페이지로 리다이렉트합니다.");
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login"; // Spring Security의 로그인 페이지로 리다이렉트
        }

        if (content == null || content.trim().isEmpty()) {
            logger.warn("질문 내용이 비어 있습니다. 질문 생성을 취소합니다.");
            // 에러 메시지를 플래시 속성으로 추가하여 리다이렉트된 페이지에서 표시
            redirectAttributes.addFlashAttribute("error", "질문 내용이 비어 있습니다.");
            return "redirect:/qna";
        }

        try {
            questionService.createQuestion(content, user);
            logger.info("사용자 ID: {}가 새로운 질문을 성공적으로 생성했습니다.", user.getUserId());
            redirectAttributes.addFlashAttribute("message", "질문이 성공적으로 생성되었습니다.");
        } catch (Exception e) {
            logger.error("질문 생성 중 오류 발생: 사용자 ID = {}, 오류 메시지 = {}", user.getUserId(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "질문 생성 중 오류가 발생했습니다.");
            return "redirect:/qna";
        }

        return "redirect:/qna";
    }
}
