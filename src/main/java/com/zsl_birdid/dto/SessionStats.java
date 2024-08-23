package com.zsl_birdid.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for session statistics.
 * This class is used to encapsulate and transfer statistical data about a session.
 */
@Setter
@Getter
public class SessionStats {

    private int minScore;     // Minimum score achieved in the session
    private int maxScore;     // Maximum score achieved in the session
    private int averageScore; // Average score of all participants in the session
    private int medianScore;  // Median score of all participants in the session

    /**
     * Constructor to initialize all fields of the SessionStats class.
     *
     * @param minScore Minimum score achieved in the session
     * @param maxScore Maximum score achieved in the session
     * @param averageScore Average score of all participants in the session
     * @param medianScore Median score of all participants in the session
     */
    public SessionStats(int minScore, int maxScore, int averageScore, int medianScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.averageScore = averageScore;
        this.medianScore = medianScore;
    }
}
