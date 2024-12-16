package com.example.food.service.qnaservice;

import com.example.food.domain.Question;
import com.example.food.domain.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
	
    Page<Question> getQuestionsByUser(String userId, Pageable pageable); // 페이징 처리된 질문 목록 조회
    Page<Question> getAllQuestion(Pageable pageable);
    Question getQuestionDetail(Long qSeq, Users user); // 특정 질문 상세 조회
    void createQuestion(String content, Users user); // 질문 생성
    void updateQuestion(Long qSeq, String title, String content, Users user); // 질문 수정
    void deleteQuestion(Long qSeq, Users user);
}
