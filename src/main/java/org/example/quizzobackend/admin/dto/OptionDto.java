package org.example.quizzobackend.admin.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionDto {
    private UUID id;

    @NotBlank(message = "Option text is required")
    @Size(max = 500)
    private String text;

    private boolean correct;
    private String feedback;
}
