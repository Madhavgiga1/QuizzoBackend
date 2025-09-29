package org.example.quizzobackend;

import org.example.quizzobackend.auth.entity.User;
import org.example.quizzobackend.domain.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID>, JpaSpecificationExecutor<Quiz> {

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") UUID id);

    Page<Quiz> findByPublishedTrue(Pageable pageable);

    Page<Quiz> findByCreatedBy(User createdBy, Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.published = true AND " +
            "(LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Quiz> searchPublishedQuizzes(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.user.id = :userId")
    int countUserAttempts(@Param("quizId") UUID quizId, @Param("userId") UUID userId);
}
