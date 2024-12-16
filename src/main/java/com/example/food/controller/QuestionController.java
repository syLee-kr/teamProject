package com.example.food.controller;

import com.example.food.domain.Answer;
import com.example.food.domain.Question;
import com.example.food.domain.Users;
import com.example.food.service.qnaservice.AnswerService;
import com.example.food.service.qnaservice.QuestionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/qna")
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    @Autowired
    public QuestionController(QuestionService questionService, AnswerService answerService) {
        this.questionService = questionService;
        this.answerService = answerService;
    }

    // 현재 로그인된 사용자 정보를 세션에서 가져옴
    private Users getAuthenticatedUser(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            throw new SecurityException("로그인을 먼저 진행하세요.");
        }
        return user;
    }

    // QnA 채팅방 메인 페이지
    @GetMapping
    public String listQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize, // 더 많은 메시지를 한 번에 로드
            HttpSession session,
            Model model
    ) {
        // 인증된 사용자 가져오기
        Users user = getAuthenticatedUser(session);

        // 모든 질문과 답변을 시간 순으로 가져오기
        Page<Question> questionPage = questionService.getAllQuestion(PageRequest.of(page, pageSize));

        // 총 페이지 수 및 현재 페이지 계산
        int totalPages = questionPage.getTotalPages();
        int currentPage = questionPage.getNumber() + 1; // 1-based index

        // 페이징 계산 (필요 시)
        int pageGroupSize = 5; // 한 그룹에 표시할 페이지 수
        int startPage = Math.max(1, ((currentPage - 1) / pageGroupSize) * pageGroupSize + 1);
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        // 모델에 속성 추가
        model.addAttribute("questions", questionPage.getContent()); // 질문 목록
        model.addAttribute("questionPage", questionPage);           // 전체 페이지 정보
        model.addAttribute("currentPage", currentPage);             // 현재 페이지 번호
        model.addAttribute("totalPages", totalPages);               // 총 페이지 수
        model.addAttribute("startPage", startPage);                 // 현재 그룹의 시작 페이지 번호
        model.addAttribute("endPage", endPage);                     // 현재 그룹의 마지막 페이지 번호

        return "question/question"; // 렌더링할 템플릿 파일명
    }

    // 새로운 질문 생성
    @PostMapping
    public String createQuestion(
            @RequestParam String content,
            HttpSession session
    ) {
        Users user = getAuthenticatedUser(session);
        questionService.createQuestion(content, user);
        return "redirect:/qna";
    }

    // 관리자 답변 추가
    @PostMapping("/answer")
    public String createAnswer(
            @RequestParam Long qSeq,
            @RequestParam String content,
            HttpSession session
    ) {
        Users admin = getAuthenticatedUser(session);
        if (admin.getRole().equals("ROLE_ADMIN")) {
            answerService.createAnswer(qSeq, content, admin);
        }
        return "redirect:/qna";
    }
}
