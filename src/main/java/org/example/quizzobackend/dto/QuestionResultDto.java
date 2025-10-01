package org.example.quizzobackend.dto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultDto {
    private UUID questionId;
    private String questionText;
    private boolean correct;
    private int pointsEarned;
    private int maxPoints;
    private String explanation;
}
