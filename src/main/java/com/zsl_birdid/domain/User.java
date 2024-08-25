package com.zsl_birdid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a User entity in the system.
 * This class manages user-specific information, including their scores, answered questions, and session details.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;  // Unique identifier for the User entity

    private int sessionScore = 0;  // Score achieved by the user in the current session

    @ElementCollection
    private Set<Long> answeredQuestions = new HashSet<>();  // Set of question IDs that the user has answered

    @OneToOne(mappedBy = "admin", cascade = CascadeType.ALL)
    private Session managedSession;  // The session that the user manages (admin role)

    private boolean inSession;  // Indicates whether the user is currently in a session

    @ManyToOne
    private Session CurrentSession;  // The session that the user is currently participating in
}
