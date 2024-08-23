package com.zsl_birdid.Repo;

import com.zsl_birdid.domain.Question;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository interface for accessing and managing {@link Question} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 */
public interface QuestionRepository extends CrudRepository<Question, Long> {
    // No additional query methods are defined here as of now.
    // Custom queries can be added if needed in the future.
}
