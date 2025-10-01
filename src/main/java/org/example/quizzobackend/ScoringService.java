package org.example.quizzobackend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

    public QuizResultDto calculateScore(QuizAttempt attempt) {
        int totalScore = 0;
        int earnedScore = 0;
        List<QuestionResultDto> questionResults = new ArrayList<>();

        for (Answer answer : attempt.getAnswers()) {
            // Evaluate each answer
            answer.evaluateAnswer();

            totalScore += answer.getQuestion().getPoints();
            earnedScore += answer.getPointsEarned();

            // Create result for each question
            QuestionResultDto questionResult = QuestionResultDto.builder()
                    .questionId(answer.getQuestion().getId())
                    .questionText(answer.getQuestion().getText())
                    .correct(answer.getCorrect())
                    .pointsEarned(answer.getPointsEarned())
                    .maxPoints(answer.getQuestion().getPoints())
                    .explanation(answer.getQuestion().getExplanation())
                    .build();

            questionResults.add(questionResult);
        }

        double percentage = totalScore > 0 ? (earnedScore * 100.0) / totalScore : 0;
        boolean passed = attempt.getQuiz().getPassingScore() != null &&
                percentage >= attempt.getQuiz().getPassingScore();

        return QuizResultDto.builder()
                .attemptId(attempt.getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(earnedScore)
                .totalScore(totalScore)
                .percentage(percentage)
                .passed(passed)
                .questionResults(questionResults)
                .completedAt(attempt.getCompletedAt())
                .build();
    }
}
