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
     * Find {@link Bird} entities by their name.
     *
     * @param birdName The name of the bird to find
     * @return A list of {@link Bird} entities with the specified name. The list will be empty if no birds are found.
     */
    List<Bird> findByBirdName(String birdName);


    /**
     * Find all {@link Bird} entities that are marked as main or not.
     *
     * @param isMain Whether to find main birds or not
     * @return A list of {@link Bird} entities matching the specified criteria
     */
    List<Bird> findByIsMain(boolean isMain);
}
