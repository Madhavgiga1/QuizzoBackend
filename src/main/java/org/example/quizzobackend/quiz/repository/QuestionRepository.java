package org.example.quizzobackend.quiz.repository;

import org.example.quizzobackend.quiz.enitity.DifficultyLevel;
import org.example.quizzobackend.quiz.enitity.Question;
import org.example.quizzobackend.quiz.enitity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByCategory(String category);

    List<Question> findByDifficulty(DifficultyLevel difficulty);

    List<Question> findByType(QuestionType type);

    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t WHERE t IN :tags")
    List<Question> findByTags(@Param("tags") Set<String> tags);

    @Query("SELECT q FROM Question q WHERE q.category = :category AND q.difficulty = :difficulty")
    List<Question> findByCategoryAndDifficulty(
            @Param("category") String category,
            @Param("difficulty") DifficultyLevel difficulty
    );

    // For question bank management
    @Query("SELECT q FROM Question q WHERE " +
            "LOWER(q.text) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Question> searchQuestions(@Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT q.category FROM Question q WHERE q.category IS NOT NULL")
    List<String> findAllCategories();
}
