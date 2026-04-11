package com.quizapp.service;

import com.quizapp.model.Question;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionService {
    private List<Question> questions;
    private static final String RESULTS_FILE = "results.txt";

    public QuestionService() {
        this.questions = new ArrayList<>();
        loadQuestions();
    }

    private void loadQuestions() {
        questions.add(new Question("Which language is primarily used for Android development?", Arrays.asList("Python", "Java", "C++", "Swift"), 1));
        questions.add(new Question("What does HTML stand for?", Arrays.asList("High Tech Modern Language", "Hyperlink Text Markup Language", "HyperText Markup Language", "Home Tool Markup Language"), 2));
        questions.add(new Question("Which data structure uses LIFO (Last In First Out)?", Arrays.asList("Queue", "Array", "Stack", "Linked List"), 2));
        questions.add(new Question("Who is known as the father of Computers?", Arrays.asList("Alan Turing", "Charles Babbage", "Ada Lovelace", "Bill Gates"), 1));
        questions.add(new Question("What is the time complexity of a Binary Search?", Arrays.asList("O(n)", "O(n log n)", "O(log n)", "O(1)"), 2));
        questions.add(new Question("Which of these is NOT a reserved keyword in Java?", Arrays.asList("volatile", "strictfp", "goto", "main"), 3));
        questions.add(new Question("What is the brain of the computer?", Arrays.asList("RAM", "CPU", "GPU", "Hard Drive"), 1));
        questions.add(new Question("Which protocol is used for secure web browsing?", Arrays.asList("HTTP", "FTP", "HTTPS", "SMTP"), 2));
        questions.add(new Question("In Git, which command saves changes locally?", Arrays.asList("push", "commit", "pull", "fetch"), 1));
        questions.add(new Question("What is the purpose of a 'Constructor' in Java?", Arrays.asList("To destroy objects", "To initialize objects", "To create loops", "To import packages"), 1));
    }

    public List<Question> getAllQuestions() {
        return questions;
    }

    public void saveResult(String name, int score, int total) {
        try (FileWriter fw = new FileWriter(RESULTS_FILE, true);
             PrintWriter out = new PrintWriter(fw)) {
            out.println(LocalDateTime.now() + " | Name: " + name + " | Score: " + score + "/" + total);
        } catch (IOException e) {
            System.err.println("Could not save result: " + e.getMessage());
        }
    }
}
