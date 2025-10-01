package org.example.quizzobackend.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.quizzobackend.quiz.repository.QuestionRepository;
import org.example.quizzobackend.admin.dto.OptionDto;
import org.example.quizzobackend.admin.dto.QuestionCreateDto;
import org.example.quizzobackend.common.exception.ResourceNotFoundException;
import org.example.quizzobackend.quiz.enitity.Option;
import org.example.quizzobackend.quiz.enitity.Question;
import org.example.quizzobackend.quiz.enitity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionManagementService {

    private final QuestionRepository questionRepository;

    @Transactional
    public Question createQuestion(QuestionCreateDto dto) throws BadRequestException {
        // Validate
        if (dto.getText().length() > 1000) {
            throw new BadRequestException("Question text too long (max 1000 characters)");
        }

        Question question = Question.builder()
                .text(dto.getText())
                .type(dto.getType())
                .points(dto.getPoints() != null ? dto.getPoints() : 1)
                .explanation(dto.getExplanation())
                .category(dto.getCategory())
                .difficulty(dto.getDifficulty())
                .required(dto.isRequired())
                .build();

        // Add tags
        if (dto.getTags() != null) {
            question.getTags().addAll(dto.getTags());
        }

        // Add options
        for (OptionDto optionDto : dto.getOptions()) {
            Option option = Option.builder()
                    .text(optionDto.getText())
                    .correct(optionDto.isCorrect())
                    .feedback(optionDto.getFeedback())
                    .build();
            question.addOption(option);
        }

        // Validate question configuration
        if (!question.isValidConfiguration()) {
            throw new BadRequestException("Invalid question configuration for type: " + dto.getType());
        }

        question = questionRepository.save(question);
        log.info("Question created: {}", question.getId());

        return question;
    }

    @Transactional
    public Question updateQuestion(UUID questionId, QuestionCreateDto dto) throws BadRequestException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Check if question is part of published quiz
        boolean isInPublishedQuiz = question.getQuizzes().stream()
                .anyMatch(quiz -> quiz.isPublished());

        if (isInPublishedQuiz) {
            throw new BadRequestException("Cannot modify question used in published quiz");
        }

        question.setText(dto.getText());
        question.setType(dto.getType());
        question.setPoints(dto.getPoints());
        question.setExplanation(dto.getExplanation());
        question.setCategory(dto.getCategory());
        question.setDifficulty(dto.getDifficulty());

        // Update options
        question.getOptions().clear();
        for (OptionDto optionDto : dto.getOptions()) {
            Option option = Option.builder()
                    .text(optionDto.getText())
                    .correct(optionDto.isCorrect())
                    .feedback(optionDto.getFeedback())
                    .build();
            question.addOption(option);
        }

        if (!question.isValidConfiguration()) {
            throw new BadRequestException("Invalid question configuration");
        }

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(UUID questionId) throws BadRequestException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Check if question is part of any quiz
        if (!question.getQuizzes().isEmpty()) {
            throw new BadRequestException("Cannot delete question that is part of quiz");
        }

        questionRepository.delete(question);
        log.info("Question deleted: {}", questionId);
    }

    public Page<Question> searchQuestions(String search, String category,
                                          QuestionType type, Pageable pageable) {
        // Implement search logic based on parameters
        if (search != null && !search.isEmpty()) {
            return questionRepository.searchQuestions(search, pageable);
        }
        return questionRepository.findAll(pageable);
    }

    public List<String> getAllCategories() {
        return questionRepository.findAllCategories();
    }
}
