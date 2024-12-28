package com.example.food.repository;

import com.example.food.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers ORDER BY q.regDate DESC")
    List<Question> findAllWithAnswersOrderByRegDateDesc();

    // 특정 사용자 ID로 질문 조회 (페이징)
    Page<Question> findByUser_UserId(String userId, Pageable pageable);

    List<Question> findByUser_UserId(String userId);
    List<Question> findByUser_UserIdOrderByRegDateAsc(String userId);

    // 최신 게시물 순으로 모든 질문 가져오기
    List<Question> findAllByOrderByRegDateDesc();

}
