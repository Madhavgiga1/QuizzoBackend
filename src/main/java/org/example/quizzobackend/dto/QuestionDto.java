package org.example.quizzobackend.dto;
import lombok.*;
import org.example.quizzobackend.admin.OptionDto;
import org.example.quizzobackend.domain.QuestionType;

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
