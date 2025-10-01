package org.example.quizzobackend.admin.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// QuizCreateDto.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateDto {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @Min(1)
    private Integer timeLimitMinutes;

    @Min(0)
    @Max(100)
    private Integer passingScore;

    @Min(1)
    private Integer maxAttempts;
}
