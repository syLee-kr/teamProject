package com.example.food.repository;

import com.example.food.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Page<Answer> findByQuestion_QSeqOrderByRegDateAsc(Long questionId, Pageable pageable);

    Page<Answer> findByQuestion_QSeqAndContentContainingIgnoreCaseOrderByRegDateAsc(Long questionId, String keyword, Pageable pageable);
}
