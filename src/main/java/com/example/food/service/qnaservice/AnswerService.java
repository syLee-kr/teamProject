package com.example.food.service.qnaservice;

import com.example.food.entity.Answer;
import com.example.food.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnswerService {

    Answer createAnswer(Long questionId, String content, Users admin); // 답변 작성

    Answer updateAnswer(Long answerId, String content, Users user); // 답변 수정

    void deleteAnswer(Long answerId, Users user); // 답변 삭제
    
    // 질문별 답변 목록 조회 (페이징)
    Page<Answer> getAnswersByQuestion(Long questionId, Pageable pageable); 
    
    //질문별 답변 목록 조회 (페이징 + 키워드 검색)
    Page<Answer> getAnswersByQuestionAndKeyword(Long questionId, String keyword, Pageable pageable);
}





