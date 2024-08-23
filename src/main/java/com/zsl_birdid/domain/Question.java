package com.zsl_birdid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * Represents a Question entity in the system.
 * This class is used to manage questions related to birds, including options and associated media URLs.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;  // Unique identifier for the Question entity

    @ManyToOne
    private Bird mainBird;  // The main bird for this question

    @ManyToMany
    private List<Bird> options = new ArrayList<>();  // List of option birds for this question

    @ElementCollection
    @CollectionTable(name = "bird_video_urls", joinColumns = @JoinColumn(name = "question_id"))
    @MapKeyJoinColumn(name = "bird_id")
    @Column(name = "video_url")
    private Map<Bird, String> birdVideoUrls = new HashMap<>();  // Map of bird options and their corresponding video URLs

    /**
     * Selects a random option from the given bird's options and returns its media URL.
     * @param b The bird whose options are to be considered.
     * @return The media URL of a randomly selected option bird.
     */
    public String random_option(Bird b) {
        List<Bird> birdOptions = b.getOptions();
        Random random = new Random();
        int randomIndex = random.nextInt(birdOptions.size());
        Bird option = birdOptions.get(randomIndex);
        return option.getMediaUrl();
    }

    /**
     * Assigns random video URLs to all option birds and assigns the specific video URL to the main bird.
     */
    public void randomOptions() {
        // First, assign random video URLs to all options
        for (Bird op : options) {
            if (!Objects.equals(op, mainBird)) {
                String videoUrl = random_option(op);
                birdVideoUrls.put(op, videoUrl);
            }
        }

        // Then, assign the specific video URL for mainBird
        if (options.contains(mainBird)) {
            birdVideoUrls.put(mainBird, mainBird.getVideoUrl());
        }
    }
}
