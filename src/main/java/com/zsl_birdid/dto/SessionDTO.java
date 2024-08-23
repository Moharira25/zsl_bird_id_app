package com.zsl_birdid.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for transferring session data.
 * This class is used to encapsulate the data required to create or update a session.
 */
@NoArgsConstructor
@Getter
@Setter
public class SessionDTO {

    private String name;  // Name of the session

    private String sessionType;  // Type of the session (e.g., individual or group)
}
