package com.zsl_birdid.services;

import com.zsl_birdid.Repo.BirdRepository;
import com.zsl_birdid.domain.Bird;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Service class for handling business logic related to {@link Bird} entities.
 */
@Service
public class BirdService {

    private final BirdRepository birdRepository;

    /**
     * Constructs a new {@link BirdService} with the specified {@link BirdRepository}.
     *
     * @param birdRepository The repository for accessing {@link Bird} entities
     */
    public BirdService(BirdRepository birdRepository) {
        this.birdRepository = birdRepository;
    }

    /**
     * Retrieves a random main bird from the repository.
     *
     * Main birds are identified by the `isMain` flag set to true.
     * If no main birds are found, returns {@code null}.
     *
     * @return A random {@link Bird} object that is a main bird, or {@code null} if no main birds exist
     */
    public Bird getRandomMainBird() {
        List<Bird> mainBirds = birdRepository.findByIsMain(true);
        if (mainBirds.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(mainBirds.size());
        return mainBirds.get(randomIndex);
    }
}
