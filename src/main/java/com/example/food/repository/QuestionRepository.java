package com.example.food.repository;

import com.example.food.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 특정 사용자 ID로 질문 조회 (페이징)
    Page<Question> findByUser_UserId(String userId, Pageable pageable);

    // 최신 게시물 순으로 모든 질문 가져오기 (페이징 포함)
    Page<Question> findAllByOrderByRegDateDesc(Pageable pageable);
}
