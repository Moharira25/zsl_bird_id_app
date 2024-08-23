package com.zsl_birdid.services;

import com.zsl_birdid.Repo.QuestionRepository;
import com.zsl_birdid.domain.Bird;
import com.zsl_birdid.domain.Question;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

/**
 * Service class for handling business logic related to {@link Question} entities.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final BirdService birdService;

    /**
     * Constructs a new {@link QuestionService} with the specified {@link QuestionRepository} and {@link BirdService}.
     *
     * @param questionRepository The repository for accessing {@link Question} entities
     * @param birdService        The service for handling {@link Bird} entities
     */
    public QuestionService(QuestionRepository questionRepository, BirdService birdService) {
        this.questionRepository = questionRepository;
        this.birdService = birdService;
    }

    /**
     * Creates a new {@link Question} with a random main bird and a set of options.
     *
     * The question's options include the main bird and two additional unique birds
     * that are different from the main bird. The options are then shuffled.
     *
     * @return The created {@link Question} with randomly assigned options
     */
    public Question createQuestion() {
        Question question = new Question();
        Bird mainBird = birdService.getRandomMainBird();
        question.setMainBird(mainBird);

        // Add the main bird to the list of options
        question.getOptions().add(mainBird);

        // Add two additional unique birds to the options
        addUniqueBirdToOptions(question, mainBird);
        addUniqueBirdToOptions(question, mainBird);

        // Assign video URLs to the options
        question.randomOptions();

        // Shuffle the options to randomize their order
        Collections.shuffle(question.getOptions());

        // Save the question to the repository
        questionRepository.save(question);

        return question;
    }

    /**
     * Adds a unique bird to the options of the question, ensuring it is different from the main bird
     * and not already present in the options.
     *
     * @param question The {@link Question} to which the bird is being added
     * @param mainBird The main {@link Bird} to compare against
     */
    private void addUniqueBirdToOptions(Question question, Bird mainBird) {
        while (true) {
            Bird birdOption = birdService.getRandomMainBird();

            // Check if the bird is not already in the options and is not the main bird
            if (!question.getOptions().contains(birdOption) && !Objects.equals(birdOption.getId(), mainBird.getId())) {
                question.getOptions().add(birdOption);
                break;
            }
        }
    }
}
