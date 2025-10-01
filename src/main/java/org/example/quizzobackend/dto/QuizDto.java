package org.example.quizzobackend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// QuizDto.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto {
    private UUID id;
    private String title;
    private String description;
    private int questionCount;
    private int totalPoints;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private boolean published;
    private LocalDateTime createdAt;
}
