package com.zsl_birdid.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Session entity in the system.
 * This class manages information about a session, including its participants, questions, and scores.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(value = { "userList", "questionList", "minScore", "maxScore", "averageScore", "medianScore", "admin" })
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;  // Unique identifier for the Session entity

    private boolean active;  // Indicates whether the session is currently active

    private String name;  // Name of the session

    @OneToMany(fetch = FetchType.EAGER)  // Fetch users eagerly to ensure they are loaded when accessing the session
    private List<User> userList = new ArrayList<>();  // List of users participating in the session

    private int questionIndex = 0;  // Index of the current question in the session

    @OneToOne
    private User admin;  // The admin or creator of the session

    private boolean individual;  // Indicates whether the session is for individual users

    @OneToMany
    private List<Question> questionList = new ArrayList<>();  // List of questions in the session

    private int minScore;  // Minimum score achieved in the session

    private int maxScore;  // Maximum score achieved in the session

    private int averageScore;  // Average score of all participants in the session

    private int medianScore;  // Median score of all participants in the session
}
