package com.example.food.service.qnaservice;

import com.example.food.entity.Answer;
import com.example.food.entity.Question;
import com.example.food.entity.Users;
import com.example.food.repository.AnswerRepository;
import com.example.food.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepo;
    private final QuestionRepository questionRepo;

    @Autowired
    public AnswerServiceImpl(AnswerRepository answerRepo, QuestionRepository questionRepo) {
        this.answerRepo = answerRepo;
        this.questionRepo = questionRepo;
    }

    @Override
    public Answer createAnswer(Long questionId, String content, Users admin) {
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 질문입니다."));

        Answer answer = new Answer();
        answer.setContent(content);
        answer.setQuestion(question);
        answer.setAdmin(admin);
        return answerRepo.save(answer);
    }

    @Override
    public Answer updateAnswer(Long answerId, String content, Users user) {
        Answer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 답변입니다."));

        answer.setContent(content);
        return answerRepo.save(answer);
    }

    @Override
    public void deleteAnswer(Long answerId, Users user) {
        Answer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 답변입니다."));

        answerRepo.delete(answer);
    }

    @Override
    public Page<Answer> getAnswersByQuestion(Long questionId, Pageable pageable) {
        return answerRepo.findByQuestion_QSeqOrderByRegDateAsc(questionId, pageable); // 답변을 오름차순 정렬
    }

    @Override
    public Page<Answer> getAnswersByQuestionAndKeyword(Long questionId, String keyword, Pageable pageable) {
        return answerRepo.findByQuestion_QSeqAndContentContainingIgnoreCaseOrderByRegDateAsc(questionId, keyword, pageable); // 답변을 오름차순 정렬
    }
}
