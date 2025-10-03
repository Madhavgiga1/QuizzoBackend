package org.example.quizzobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing

public class QuizzoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizzoBackendApplication.class, args);
    }

}
