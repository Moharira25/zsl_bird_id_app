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
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private boolean active;
    private String name;
    @OneToMany(fetch = FetchType.EAGER) // Ensure users are fetched eagerly if needed
    private List<User> userList = new ArrayList<>();

    @OneToOne
    private User admin;

}
