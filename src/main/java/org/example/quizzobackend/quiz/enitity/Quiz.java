package org.example.quizzobackend.quiz.enitity;


import jakarta.persistence.*;
import lombok.*;
import org.example.quizzobackend.auth.entity.User;

import java.util.*;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(name = "is_published")
    private boolean published = false;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "passing_score")
    private Integer passingScore;

    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 1;

    // Many-to-Many with Question (Question Bank approach)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderColumn(name = "question_order")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private Set<QuizAttempt> attempts = new HashSet<>();

    // Helper methods
    public void addQuestion(Question question) {
        questions.add(question);
        question.getQuizzes().add(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.getQuizzes().remove(this);
    }

    public int getTotalPoints() {
        return questions.stream()
                .mapToInt(Question::getPoints)
                .sum();
    }

    public int getQuestionCount() {
        return questions.size();
    }
}

