package com.example.food.service.qnaservice;

import com.example.food.entity.Question;
import com.example.food.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface QuestionService {

    Page<Question> getQuestionsByUser(String userId, Pageable pageable); // 페이징 처리된 질문 목록 조회
    List<Question> getAllQuestions();
    List<Question> getAllUserQuestions(String userId);
    Optional<Question> getQuestionDetail(Long qSeq); // 특정 질문 상세 조회
    void createQuestion(String content, Users user); // 질문 생성
    void updateQuestion(Long qSeq, String content); // 질문 수정
    void deleteQuestion(Long qSeq);
}
