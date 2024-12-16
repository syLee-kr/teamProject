package com.example.food.service.qnaservice;

import com.example.food.domain.Question;
import com.example.food.domain.Users;
import com.example.food.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;

    @Autowired 
    public QuestionServiceImpl(QuestionRepository questionRepo) {
    	this.questionRepo = questionRepo; }

    @Override
    public Page<Question> getQuestionsByUser(String userId, Pageable pageable) {
        return questionRepo.findByUser_UserId(userId, pageable);
    }

    @Override
    public Page<Question> getAllQuestion(Pageable pageable) {
        return questionRepo.findAllByOrderByRegDateDesc(pageable);
    }

    @Override
    public Question getQuestionDetail(Long qSeq, Users user) {
        return questionRepo.findById(qSeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));
    }

    @Override
    public void createQuestion(String content, Users user) {
        Question question = Question.builder()
                .content(content)
                .user(user)
                .build();
        questionRepo.save(question);
    }

    @Override
    public void updateQuestion(Long qSeq, String title, String content, Users user) {
        Question question = getQuestionDetail(qSeq, user);
        question.setContent(content);
        questionRepo.save(question);
    }

    @Override
    public void deleteQuestion(Long qSeq, Users user) {
        Question question = getQuestionDetail(qSeq, user);
        questionRepo.delete(question);
    }
}

	 
