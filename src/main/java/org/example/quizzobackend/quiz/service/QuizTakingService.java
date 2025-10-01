package org.example.quizzobackend.quiz.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.quizzobackend.attempt.repository.QuizAttemptRepository;
import org.example.quizzobackend.quiz.repository.QuizRepository;
import org.example.quizzobackend.admin.dto.OptionDto;
import org.example.quizzobackend.common.exception.BadRequestException;
import org.example.quizzobackend.common.exception.ResourceNotFoundException;
import org.example.quizzobackend.quiz.enitity.AttemptStatus;
import org.example.quizzobackend.quiz.enitity.QuizAttempt;
import org.example.quizzobackend.quiz.dto.QuestionDto;
import org.example.quizzobackend.quiz.dto.QuizDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizTakingService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final ScoringService scoringService;

    public Page<QuizDto> getAvailableQuizzes(Pageable pageable) {
        return quizRepository.findByPublishedTrue(pageable)
                .map(this::mapToDto);
    }

    public Page<QuizDto> searchQuizzes(String search, Pageable pageable) {
        return quizRepository.searchPublishedQuizzes(search, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public QuizAttemptDto startQuiz(UUID quizId, User user) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (!quiz.isPublished()) {
            throw new BadRequestException("Quiz is not published");
        }

        // Check for existing in-progress attempt
        Optional<QuizAttempt> existingAttempt = attemptRepository
                .findByQuizAndUserAndStatus(quiz, user, AttemptStatus.IN_PROGRESS);

        if (existingAttempt.isPresent()) {
            // Return existing attempt if not expired
            QuizAttempt attempt = existingAttempt.get();
            if (!attempt.isExpired()) {
                return mapToAttemptDto(attempt);
            } else {
                // Mark as expired
                attempt.setStatus(AttemptStatus.EXPIRED);
                attemptRepository.save(attempt);
            }
        }

        // Check max attempts
        int userAttempts = quizRepository.countUserAttempts(quizId, user.getId());
        if (quiz.getMaxAttempts() != null && userAttempts >= quiz.getMaxAttempts()) {
            throw new BadRequestException("Maximum attempts reached for this quiz");
        }

        // Create new attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .startedAt(LocalDateTime.now())
                .status(AttemptStatus.IN_PROGRESS)
                .totalScore(quiz.getTotalPoints())
                .build();

        attempt = attemptRepository.save(attempt);
        log.info("Quiz attempt started - User: {}, Quiz: {}", user.getUsername(), quizId);

        return mapToAttemptDto(attempt);
    }

    @Transactional(readOnly = true)
    public QuizPlayDto getQuizQuestions(UUID attemptId, User user) {
        QuizAttempt attempt = attemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This quiz attempt is already completed");
        }

        if (attempt.isExpired()) {
            throw new BadRequestException("This quiz attempt has expired");
        }

        Quiz quiz = attempt.getQuiz();
        List<QuestionDto> questions = quiz.getQuestions().stream()
                .map(this::mapToQuestionDto)
                .collect(Collectors.toList());

        return QuizPlayDto.builder()
                .attemptId(attempt.getId())
                .quizTitle(quiz.getTitle())
                .description(quiz.getDescription())
                .questions(questions)
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .startedAt(attempt.getStartedAt())
                .totalQuestions(questions.size())
                .build();
    }

    @Transactional
    public AnswerResponseDto submitAnswer(UUID attemptId, SubmitAnswerDto dto, User user) {
        QuizAttempt attempt = attemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot submit answer for completed attempt");
        }

        if (attempt.isExpired()) {
            attempt.setStatus(AttemptStatus.EXPIRED);
            attemptRepository.save(attempt);
            throw new BadRequestException("Quiz attempt has expired");
        }

        // Find the question
        Question question = attempt.getQuiz().getQuestions().stream()
                .filter(q -> q.getId().equals(dto.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Question not found in quiz"));

        // Check if already answered
        Optional<Answer> existingAnswer = attempt.getAnswers().stream()
                .filter(a -> a.getQuestion().getId().equals(dto.getQuestionId()))
                .findFirst();

        Answer answer;
        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
        } else {
            answer = Answer.builder()
                    .attempt(attempt)
                    .question(question)
                    .build();
        }

        // Set selected options based on question type
        switch (question.getType()) {
            case SINGLE_CHOICE, TRUE_FALSE -> {
                if (dto.getSelectedOptionId() == null) {
                    throw new BadRequestException("Option must be selected");
                }
                Option option = question.getOptions().stream()
                        .filter(o -> o.getId().equals(dto.getSelectedOptionId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Invalid option"));
                answer.setSelectedOption(option);
            }
            case MULTIPLE_CHOICE -> {
                if (dto.getSelectedOptionIds() == null || dto.getSelectedOptionIds().isEmpty()) {
                    throw new BadRequestException("At least one option must be selected");
                }
                Set<Option> options = question.getOptions().stream()
                        .filter(o -> dto.getSelectedOptionIds().contains(o.getId()))
                        .collect(Collectors.toSet());
                answer.setSelectedOptions(options);
            }
        }

        // Don't evaluate correctness yet (only on final submission)
        attempt.addAnswer(answer);
        attemptRepository.save(attempt);

        return AnswerResponseDto.builder()
                .attemptId(attemptId)
                .questionId(dto.getQuestionId())
                .saved(true)
                .message("Answer saved successfully")
                .build();
    }

    @Transactional
    public QuizResultDto submitQuiz(UUID attemptId, User user) {
        QuizAttempt attempt = attemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This quiz is already submitted");
        }

        // Calculate score
        QuizResultDto result = scoringService.calculateScore(attempt);

        // Update attempt
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setScore(result.getScore());
        attempt.setPercentage(result.getPercentage());
        attempt.setPassed(result.isPassed());

        attemptRepository.save(attempt);
        log.info("Quiz submitted - User: {}, Score: {}/{}",
                user.getUsername(), result.getScore(), result.getTotalScore());

        return result;
    }

    @Transactional(readOnly = true)
    public Page<QuizAttemptDto> getUserAttempts(User user, Pageable pageable) {
        return attemptRepository.findByUser(user, pageable)
                .map(this::mapToAttemptDto);
    }

    private QuizDto mapToDto(Quiz quiz) {
        return QuizDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .questionCount(quiz.getQuestionCount())
                .totalPoints(quiz.getTotalPoints())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .build();
    }

    private QuizAttemptDto mapToAttemptDto(QuizAttempt attempt) {
        return QuizAttemptDto.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .status(attempt.getStatus())
                .score(attempt.getScore())
                .totalScore(attempt.getTotalScore())
                .percentage(attempt.getPercentage())
                .passed(attempt.getPassed())
                .build();
    }

    private QuestionDto mapToQuestionDto(Question question) {
        List<OptionDto> options = question.getOptions().stream()
                .map(option -> OptionDto.builder()
                        .id(option.getId())
                        .text(option.getText())
                        // Don't include isCorrect for active quiz
                        .build())
                .collect(Collectors.toList());

        return QuestionDto.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType())
                .points(question.getPoints())
                .required(question.isRequired())
                .options(options)
                .build();
    }
}
