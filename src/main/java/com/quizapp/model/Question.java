package com.quizapp.model;

import java.util.List;

/**
 * Represents a single quiz question with categorized tracking.
 */
public class Question {
    private String prompt;
    private List<String> options;
    private int correctOptionIndex;
    private String category;

    public Question(String prompt, List<String> options, int correctOptionIndex, String category) {
        this.prompt = prompt;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.category = category;
    }

    // Getters
    public String getPrompt() { return prompt; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public String getCategory() { return category; }

    public boolean isCorrect(int userChoice) {
        return userChoice == correctOptionIndex;
    }
}
