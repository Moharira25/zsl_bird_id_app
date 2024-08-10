package com.zsl_birdid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bird {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String birdName;
    private String mediaUrl;  // Use this for audio or video files
    private String imageUrl;  // Use this for the bird image
    private boolean isMain;   // Indicates if this is the main bird or an option

    @OneToMany
    private List<Bird> options = new ArrayList<>(); // List of options (not main birds)
}

