package org.example.quizzobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(length = 2000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 1;

    @Column(name = "is_required")
    @Builder.Default
    private boolean required = true;

    // Many-to-Many back reference to Quiz
    @ManyToMany(mappedBy = "questions")
    private Set<Quiz> quizzes = new HashSet<>();

    // One-to-Many with Options (Options belong to specific questions)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Option> options = new ArrayList<>();

    // Question Bank metadata
    @Column(name = "category")
    private String category;

    @ElementCollection
    @CollectionTable(name = "question_tags")
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    // Helper methods
    public void addOption(Option option) {
        options.add(option);
        option.setQuestion(this);
        option.setOrderIndex(options.size() - 1);
    }

    public void removeOption(Option option) {
        options.remove(option);
        option.setQuestion(null);
    }

    public List<Option> getCorrectOptions() {
        return options.stream()
                .filter(Option::isCorrect)
                .toList();
    }

    public boolean isValidConfiguration() {
        if (options.isEmpty()) return false;

        long correctCount = options.stream().filter(Option::isCorrect).count();

        return switch (type) {
            case SINGLE_CHOICE -> correctCount == 1;
            case MULTIPLE_CHOICE -> correctCount >= 1;
            case TRUE_FALSE -> options.size() == 2 && correctCount == 1;
        };
    }
}
