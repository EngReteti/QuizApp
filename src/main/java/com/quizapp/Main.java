package com.quizapp;

import com.quizapp.service.QuestionService;
import com.quizapp.ui.WebController;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        QuestionService service = new QuestionService();
        WebController webUI = new WebController(service);

        try {
            System.out.println("Initializing QuizApp Web Server...");
            webUI.startServer();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
