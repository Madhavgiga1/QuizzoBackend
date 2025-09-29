package org.example.quizzobackend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.quizzobackend.auth.entity.User;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "score")
    private Integer score;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "is_passed")
    private Boolean passed;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers = new HashSet<>();

    // Unique constraint to prevent multiple active attempts
    @Table(
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"user_id", "quiz_id", "status"},
                    name = "uk_user_quiz_active"
            )
    )

    // Helper methods
    public void addAnswer(Answer answer) {
        answers.add(answer);
        answer.setAttempt(this);
    }

    public boolean isExpired() {
        if (quiz.getTimeLimitMinutes() == null) return false;
        return startedAt.plusMinutes(quiz.getTimeLimitMinutes()).isBefore(LocalDateTime.now());
    }

    public void complete() {
        this.status = AttemptStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
