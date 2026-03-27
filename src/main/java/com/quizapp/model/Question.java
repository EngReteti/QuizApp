package com.quizapp.model;

import java.util.List;

/**
 * Represents a single quiz question.
 */
public class Question {
    private String prompt;
    private List<String> options;
    private int correctOptionIndex;

    public Question(String prompt, List<String> options, int correctOptionIndex) {
        this.prompt = prompt;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    // Getters
    public String getPrompt() { return prompt; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }

    /**
     * Helper to check if a user's answer is correct.
     */
    public boolean isCorrect(int userChoice) {
        return userChoice == correctOptionIndex;
    }
}
