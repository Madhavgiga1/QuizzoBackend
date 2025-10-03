package org.example.quizzobackend.admin.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.quizzobackend.quiz.enitity.DifficultyLevel;
import org.example.quizzobackend.quiz.enitity.QuestionType;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateDto {
    @NotBlank(message = "Question text is required")
    @Size(max = 1000)
    private String text;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @Min(1)
    private Integer points;

    @Size(max = 2000)
    private String explanation;

    private String category;
    private DifficultyLevel difficulty;
    private Set<String> tags;
    private boolean required = true;

    @NotEmpty(message = "Options are required")
    @Size(min = 2, message = "At least 2 options are required")
    private List<OptionDto> options;
}
