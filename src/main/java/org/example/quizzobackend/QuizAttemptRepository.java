package org.example.quizzobackend;

import org.example.quizzobackend.auth.entity.User;
import org.example.quizzobackend.domain.AttemptStatus;
import org.example.quizzobackend.domain.Quiz;
import org.example.quizzobackend.domain.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    Optional<QuizAttempt> findByIdAndUser(UUID id, User user);

    Optional<QuizAttempt> findByQuizAndUserAndStatus(Quiz quiz, User user, AttemptStatus status);

    List<QuizAttempt> findByUserOrderByStartedAtDesc(User user);

    Page<QuizAttempt> findByUser(User user, Pageable pageable);

    Page<QuizAttempt> findByQuiz(Quiz quiz, Pageable pageable);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.status = :status AND " +
            "qa.startedAt < :expiredTime")
    List<QuizAttempt> findExpiredAttempts(
            @Param("status") AttemptStatus status,
            @Param("expiredTime") LocalDateTime expiredTime
    );

    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa WHERE qa.quiz = :quiz AND qa.status = 'COMPLETED'")
    Double getAverageScore(@Param("quiz") Quiz quiz);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz = :quiz AND qa.passed = true")
    Long countPassedAttempts(@Param("quiz") Quiz quiz);
}
