package org.example.quizzobackend.quiz.dto;
import lombok.*;
import org.example.quizzobackend.attempt.dto.QuestionResultDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private UUID attemptId;
    private String quizTitle;
    private int score;
    private int totalScore;
    private double percentage;
    private boolean passed;
    private List<QuestionResultDto> questionResults;
    private LocalDateTime completedAt;
}
