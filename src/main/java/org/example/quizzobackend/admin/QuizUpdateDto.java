package org.example.quizzobackend.admin;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizUpdateDto {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
}
