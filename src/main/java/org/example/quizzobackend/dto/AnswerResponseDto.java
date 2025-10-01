package org.example.quizzobackend.dto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponseDto {
    private UUID attemptId;
    private UUID questionId;
    private boolean saved;
    private String message;
}
