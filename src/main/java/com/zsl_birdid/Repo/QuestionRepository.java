package com.zsl_birdid.Repo;

import com.zsl_birdid.domain.Question;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionRepository extends CrudRepository<Question, Integer> {
    List<Question> findBySessionId(Long sessionId);
}
