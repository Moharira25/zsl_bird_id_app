package com.zsl_birdid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Bird entity in the system.
 * This class is used to store information about birds, including their media and image details.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bird {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Unique identifier for the Bird entity

    private String birdName;  // Name of the bird

    private String mediaUrl;  // URL or path to the main media file (audio or video) for the bird

    private String videoUrl;  // URL or path to the video file (spectrogram) for the bird

    private String imageUrl;  // URL or path to the image file representing the bird

    private boolean isMain;   // Flag indicating whether this bird is the main bird or one of its options

    @OneToMany
    private List<Bird> options = new ArrayList<>(); // List of option birds related to the main bird

}
