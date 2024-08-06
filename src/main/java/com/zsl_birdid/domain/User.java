package com.zsl_birdid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String username;
    private String role = "user";
    private String password;

    @OneToOne(mappedBy = "admin", cascade = CascadeType.ALL)
    private Session managedSession;


}
