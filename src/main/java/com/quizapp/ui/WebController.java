package com.quizapp.ui;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.quizapp.service.QuestionService;
import com.quizapp.model.Question;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class WebController {
    private QuestionService service;

    public WebController(QuestionService service) {
        this.service = service;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/quiz", new QuizHandler());
        server.createContext("/submit", new SubmitHandler());
        server.setExecutor(null); 
        server.start();
        System.out.println("Server live: http://localhost:8080/quiz");
    }

    class QuizHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Question> questions = service.getAllQuestions();
            StringBuilder response = new StringBuilder();
            response.append("<html><head><title>QuizApp</title></head><body>");
            response.append("<h1>Computer Science Quiz</h1>");
            response.append("<form action='/submit' method='POST'>");
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                response.append("<p><b>").append(i + 1).append(". ").append(q.getPrompt()).append("</b></p>");
                List<String> options = q.getOptions();
                char label = 'A';
                for (int j = 0; j < options.size(); j++) {
                    response.append("<input type='radio' name='q").append(i)
                            .append("' value='").append(j).append("'> ")
                            .append(label).append(") ").append(options.get(j)).append("<br>");
                    label++;
                }
            }
            response.append("<br><input type='submit' value='Submit Quiz'>");
            response.append("</form></body></html>");
            byte[] bytes = response.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
                String formData = br.readLine(); 
                
                Map<String, String> answers = new HashMap<>();
                if (formData != null) {
                    for (String pair : formData.split("&")) {
                        String[] kv = pair.split("=");
                        if (kv.length > 1) answers.put(kv[0], kv[1]);
                    }
                }

                List<Question> questions = service.getAllQuestions();
                int score = 0;
                StringBuilder report = new StringBuilder();
                report.append("<table border='1'><tr><th>#</th><th>Question</th><th>Result</th></tr>");

                for (int i = 0; i < questions.size(); i++) {
                    Question q = questions.get(i);
                    String userChoiceStr = answers.get("q" + i);
                    boolean isCorrect = false;
                    String status = "<span style='color:red;'>Incorrect</span>";

                    if (userChoiceStr != null) {
                        int userChoice = Integer.parseInt(userChoiceStr);
                        if (q.isCorrect(userChoice)) {
                            score++;
                            isCorrect = true;
                            status = "<span style='color:green;'>Correct</span>";
                        }
                    }

                    report.append("<tr>")
                          .append("<td>").append(i + 1).append("</td>")
                          .append("<td>").append(q.getPrompt()).append("</td>")
                          .append("<td>").append(status).append("</td>")
                          .append("</tr>");
                }
                report.append("</table>");

                String resultPage = "<html><body><h1>Your Results</h1>" +
                                    "<h2>Final Score: " + score + " / " + questions.size() + "</h2>" +
                                    report.toString() +
                                    "<br><a href='/quiz'>Try Again</a></body></html>";

                byte[] bytes = resultPage.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }
}
