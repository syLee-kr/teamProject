package com.example.food.admin;

import com.example.food.entity.Question;
import com.example.food.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminQnaService {

    private static final Logger logger = LoggerFactory.getLogger(AdminQnaService.class);

    private final QuestionRepository questionRepository;

    public List<UserQnaDto> getUserQnaList() {
        logger.info("모든 질문을 가져오기 시작합니다.");
        List<Question> allQuestions;

        try {
            allQuestions = questionRepository.findAllByOrderByRegDateDesc();
            logger.info("총 {}개의 질문을 데이터베이스에서 가져왔습니다.", allQuestions.size());
        } catch (Exception e) {
            logger.error("질문을 가져오는 중 오류가 발생했습니다: {}", e.getMessage());
            return Collections.emptyList(); // 오류 발생 시 빈 리스트 반환
        }

        Map<String, UserQnaDto> userMap = new LinkedHashMap<>();

        for (Question q : allQuestions) {
            String uId = q.getUser().getUserId();
            if (!userMap.containsKey(uId)) {
                try {
                    UserQnaDto dto = new UserQnaDto(
                            q.getUser().getUserId(),
                            q.getUser().getName(),
                            q.getContent(),
                            q.getRegDate()
                    );
                    userMap.put(uId, dto);
                    logger.info("사용자 ID {}에 대한 QnA 정보를 추가했습니다.", uId);
                } catch (Exception e) {
                    logger.error("사용자 ID {}에 대한 QnA 정보를 생성하는 중 오류가 발생했습니다: {}", uId, e.getMessage());
                }
            }
        }

        List<UserQnaDto> userQnaList = new ArrayList<>(userMap.values());
        logger.info("최종적으로 {}개의 사용자 QnA 목록을 생성했습니다.", userQnaList.size());

        return userQnaList;
    }
}
