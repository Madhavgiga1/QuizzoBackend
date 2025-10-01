package org.example.quizzobackend.attempt.dto;

import lombok.*;
import org.example.quizzobackend.quiz.enitity.AttemptStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDto {
    private UUID id;
    private UUID quizId;
    private String quizTitle;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private AttemptStatus status;
    private Integer score;
    private Integer totalScore;
    private Double percentage;
    private Boolean passed;
}
