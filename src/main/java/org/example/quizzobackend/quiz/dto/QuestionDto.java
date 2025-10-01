package org.example.quizzobackend.quiz.dto;
import lombok.*;
import org.example.quizzobackend.admin.dto.OptionDto;
import org.example.quizzobackend.quiz.enitity.QuestionType;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private UUID id;
    private String text;
    private QuestionType type;
    private Integer points;
    private boolean required;
    private List<OptionDto> options;
}
