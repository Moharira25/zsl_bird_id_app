package com.zsl_birdid.services;

import com.zsl_birdid.Repo.QuestionRepository;
import com.zsl_birdid.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    public List<Question> getQuestionsBySessionId(Long sessionId) {
        return questionRepository.findBySessionId(sessionId);
    }

    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }
}