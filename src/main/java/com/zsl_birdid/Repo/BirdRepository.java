package com.zsl_birdid.Repo;

import com.zsl_birdid.domain.Bird;
import org.springframework.data.repository.CrudRepository;


public interface BirdRepository extends CrudRepository<Bird, Long> {
    Bird findByBirdName(String birdName);
}
