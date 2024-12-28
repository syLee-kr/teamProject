package com.example.food.service.qnaservice;

import com.example.food.entity.Question;
import com.example.food.entity.Users;
import com.example.food.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepo) {
        this.questionRepo = questionRepo;
    }

    /**
     * 사용자별 질문 목록을 페이지 단위로 조회
     */
    @Override
    public Page<Question> getQuestionsByUser(String userId, Pageable pageable) {
        return questionRepo.findByUser_UserId(userId, pageable);
    }

    /**
     * 모든 질문을 답변과 함께 등록일 내림차순으로 조회
     */
    @Override
    public List<Question> getAllQuestions() {
        return questionRepo.findAllWithAnswersOrderByRegDateDesc();
    }

    /**
     * 특정 사용자별 질문을 등록일 오름차순으로 조회
     */
    @Override
    public List<Question> getAllUserQuestions(String userId) {
        return questionRepo.findByUser_UserIdOrderByRegDateAsc(userId);
    }

    /**
     * 특정 질문의 상세 정보를 조회
     */
    @Override
    public Optional<Question> getQuestionDetail(Long qSeq) {
        return questionRepo.findById(qSeq);
    }

    /**
     * 새로운 질문 생성
     */
    @Override
    @Transactional
    public void createQuestion(String content, Users user) {
        Question question = Question.builder()
                .content(content)
                .user(user)
                .build();
        questionRepo.save(question);
    }

    /**
     * 특정 질문의 내용을 업데이트
     */
    @Override
    @Transactional
    public void updateQuestion(Long qSeq, String content) {
        Question question = getQuestionDetail(qSeq)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다. ID: " + qSeq));
        question.setContent(content);
        questionRepo.save(question);
    }

    /**
     * 특정 질문을 삭제
     */
    @Override
    @Transactional
    public void deleteQuestion(Long qSeq) {
        Question question = getQuestionDetail(qSeq)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다. ID: " + qSeq));
        questionRepo.delete(question);
    }
}
