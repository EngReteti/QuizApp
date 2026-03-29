package com.quizapp;

import com.quizapp.service.QuestionService;

public class Main {
    public static void main(String[] args) {
        QuestionService service = new QuestionService();
        
        System.out.println("--- QuizApp Database Status ---");
        System.out.println("Total Questions Loaded: " + service.getAllQuestions().size());
        System.out.println("Status: Ready for Browser Integration.");
    }
}
