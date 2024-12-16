package com.example.food.repository;

import com.example.food.domain.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // 특정 질문의 답변 목록을 페이징 처리하여 조회
    Page<Answer> findByQuestion_QSeq(Long qSeq, Pageable pageable);

    Page<Answer> findByQuestion_QSeqAndContentContainingIgnoreCase(Long questionId, String keyword, Pageable pageable);
}
