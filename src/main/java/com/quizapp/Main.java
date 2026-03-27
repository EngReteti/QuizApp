package com.quizapp;

import com.quizapp.model.Question;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- QuizApp Model Test ---");

        // Creating a dummy question for testing
        Question q1 = new Question(
            "What is the default value of a boolean in Java?",
            Arrays.asList("true", "false", "null", "0"),
            1 // "false" is at index 1
        );

        System.out.println("Question: " + q1.getPrompt());
        System.out.println("Checking answer '1': " + q1.isCorrect(1));
    }
}
