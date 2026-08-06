package com.quizapp.service;

import com.quizapp.model.Question;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class QuestionService {
    private List<Question> questions;
    private static final String RESULTS_FILE = "results.txt";
    private static final String QUESTIONS_FILE = "questions.csv";

    public QuestionService() {
        this.questions = new ArrayList<>();
        loadQuestionsFromCSV();
    }

    private void loadQuestionsFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(QUESTIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String prompt = parts[0];
                    List<String> options = Arrays.asList(parts[1].split(","));
                    int correctIndex = Integer.parseInt(parts[2]);
                    String category = parts[3];
                    questions.add(new Question(prompt, options, correctIndex, category));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
        }
    }

    public List<Question> getAllQuestions() {
        return questions;
    }

    public List<Question> getQuestionsByCategory(String category) {
        if (category == null || category.isEmpty() || category.equalsIgnoreCase("All")) {
            return questions;
        }
        return questions.stream()
                .filter(q -> q.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public Set<String> getCategories() {
        return questions.stream().map(Question::getCategory).collect(Collectors.toSet());
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
