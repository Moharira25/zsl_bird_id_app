package com.zsl_birdid.Repo;

import com.zsl_birdid.domain.Bird;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository interface for accessing and managing {@link Bird} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 */
public interface BirdRepository extends CrudRepository<Bird, Long> {

    /**
     * Find a {@link Bird} entity by its name.
     *
     * @param birdName The name of the bird to find
     * @return The {@link Bird} entity with the specified name, or {@code null} if not found
     */
    Bird findByBirdName(String birdName);

    /**
     * Find all {@link Bird} entities that are marked as main or not.
     *
     * @param isMain Whether to find main birds or not
     * @return A list of {@link Bird} entities matching the specified criteria
     */
    List<Bird> findByIsMain(boolean isMain);
}
