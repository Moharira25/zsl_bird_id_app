package com.zsl_birdid.config;


import com.zsl_birdid.domain.Session;
import com.zsl_birdid.Repo.SessionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {
    @Bean
    CommandLineRunner initDatabase(SessionRepository sessionRepository) {
        return args -> {
            if (sessionRepository.count() == 0) {
                Session session = new Session();
                session.setName("ZSL Class");
                session.setActive(true);
                sessionRepository.save(session);
            }
        };
        }
    }

