package org.example.quizzobackend.quiz.enitity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // For single choice questions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private Option selectedOption;

    // For multiple choice questions
    @ManyToMany
    @JoinTable(
            name = "answer_selected_options",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private Set<Option> selectedOptions = new HashSet<>();

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    // Helper method to check correctness
    public void evaluateAnswer() {
        switch (question.getType()) {
            case SINGLE_CHOICE, TRUE_FALSE -> {
                this.correct = selectedOption != null && selectedOption.isCorrect();
            }
            case MULTIPLE_CHOICE -> {
                Set<Option> correctOptions = new HashSet<>(question.getCorrectOptions());
                this.correct = selectedOptions.equals(correctOptions);
            }
        }
        this.pointsEarned = this.correct ? question.getPoints() : 0;
    }
}
