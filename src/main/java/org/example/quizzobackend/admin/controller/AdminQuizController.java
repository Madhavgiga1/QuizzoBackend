package org.example.quizzobackend.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.quizzobackend.admin.service.QuizManagementService;
import org.example.quizzobackend.admin.dto.QuestionCreateDto;
import org.example.quizzobackend.admin.dto.QuizCreateDto;
import org.example.quizzobackend.admin.dto.QuizUpdateDto;
import org.example.quizzobackend.auth.entity.User;
import org.example.quizzobackend.auth.service.AuthService;
import org.example.quizzobackend.quiz.enitity.Question;
import org.example.quizzobackend.quiz.dto.QuizDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuizController {

    @Autowired
    private final QuizManagementService quizManagementService;
    @Autowired
    private final QuestionManagementService questionManagementService;
    private final AuthService authService;

    // Quiz Management
    @PostMapping("/quizzes")
    public ResponseEntity<QuizDto> createQuiz(@Valid @RequestBody QuizCreateDto dto) {
        User admin = authService.getCurrentUser();
        QuizDto quiz = quizManagementService.createQuiz(dto, admin);
        return new ResponseEntity<>(quiz, HttpStatus.CREATED);
    }

    @PutMapping("/quizzes/{id}")
    public ResponseEntity<QuizDto> updateQuiz(
            @PathVariable UUID id,
            @Valid @RequestBody QuizUpdateDto dto) {
        QuizDto quiz = quizManagementService.updateQuiz(id, dto);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/quizzes/{id}/questions")
    public ResponseEntity<Void> addQuestionsToQuiz(
            @PathVariable UUID id,
            @RequestBody List<UUID> questionIds) {
        quizManagementService.addQuestionsToQuiz(id, questionIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quizzes/{id}/publish")
    public ResponseEntity<QuizDto> publishQuiz(@PathVariable UUID id) {
        QuizDto quiz = quizManagementService.publishQuiz(id);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable UUID id) {
        quizManagementService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/quizzes")
    public ResponseEntity<Page<QuizDto>> getMyQuizzes(Pageable pageable) {
        User admin = authService.getCurrentUser();
        Page<QuizDto> quizzes = quizManagementService.getAdminQuizzes(admin, pageable);
        return ResponseEntity.ok(quizzes);
    }

    // Question Management
    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody QuestionCreateDto dto) {
        Question question = questionManagementService.createQuestion(dto);
        return new ResponseEntity<>(question, HttpStatus.CREATED);
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody QuestionCreateDto dto) {
        Question question = questionManagementService.updateQuestion(id, dto);
        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionManagementService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions")
    public ResponseEntity<Page<Question>> searchQuestions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        Page<Question> questions = questionManagementService
                .searchQuestions(search, category, null, pageable);
        return ResponseEntity.ok(questions);
    }
}
