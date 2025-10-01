package org.example.quizzobackend.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.quizzobackend.quiz.repository.QuestionRepository;
import org.example.quizzobackend.quiz.repository.QuizRepository;
import org.example.quizzobackend.admin.dto.QuizCreateDto;
import org.example.quizzobackend.admin.dto.QuizUpdateDto;
import org.example.quizzobackend.auth.entity.User;
import org.example.quizzobackend.common.exception.BadRequestException;
import org.example.quizzobackend.common.exception.ResourceNotFoundException;
import org.example.quizzobackend.quiz.enitity.Question;
import org.example.quizzobackend.quiz.enitity.Quiz;
import org.example.quizzobackend.quiz.dto.QuizDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizManagementService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public QuizDto createQuiz(QuizCreateDto dto, User admin) {
        Quiz quiz = Quiz.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .createdBy(admin)
                .timeLimitMinutes(dto.getTimeLimitMinutes())
                .passingScore(dto.getPassingScore())
                .maxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 1)
                .published(false)
                .build();

        quiz = quizRepository.save(quiz);
        log.info("Quiz created: {} by admin: {}", quiz.getId(), admin.getUsername());

        return mapToDto(quiz);
    }

    @Transactional
    public QuizDto updateQuiz(UUID quizId, QuizUpdateDto dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (quiz.isPublished()) {
            throw new BadRequestException("Cannot update published quiz");
        }

        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        quiz.setPassingScore(dto.getPassingScore());
        quiz.setMaxAttempts(dto.getMaxAttempts());

        quiz = quizRepository.save(quiz);
        return mapToDto(quiz);
    }

    @Transactional
    public void addQuestionsToQuiz(UUID quizId, List<UUID> questionIds) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (quiz.isPublished()) {
            throw new BadRequestException("Cannot modify published quiz");
        }

        List<Question> questions = questionRepository.findAllById(questionIds);

        for (Question question : questions) {
            if (!question.isValidConfiguration()) {
                throw new BadRequestException("Question " + question.getId() + " has invalid configuration");
            }
            quiz.addQuestion(question);
        }

        quizRepository.save(quiz);
        log.info("Added {} questions to quiz {}", questions.size(), quizId);
    }

    @Transactional
    public QuizDto publishQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Validate before publishing
        if (quiz.getQuestions().isEmpty()) {
            throw new BadRequestException("Cannot publish quiz without questions");
        }

        for (Question question : quiz.getQuestions()) {
            if (!question.isValidConfiguration()) {
                throw new BadRequestException("Question configuration is invalid");
            }
        }

        quiz.setPublished(true);
        quiz = quizRepository.save(quiz);
        log.info("Quiz published: {}", quizId);

        return mapToDto(quiz);
    }

    @Transactional
    public void deleteQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (quiz.isPublished() && !quiz.getAttempts().isEmpty()) {
            throw new BadRequestException("Cannot delete quiz with attempts");
        }

        quizRepository.delete(quiz);
        log.info("Quiz deleted: {}", quizId);
    }

    public Page<QuizDto> getAdminQuizzes(User admin, Pageable pageable) {
        return quizRepository.findByCreatedBy(admin, pageable)
                .map(this::mapToDto);
    }

    private QuizDto mapToDto(Quiz quiz) {
        return QuizDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .questionCount(quiz.getQuestionCount())
                .totalPoints(quiz.getTotalPoints())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .maxAttempts(quiz.getMaxAttempts())
                .published(quiz.isPublished())
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}
