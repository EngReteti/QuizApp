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
import java.util.stream.Collectors;

public class WebController {
    private QuestionService service;

    public WebController(QuestionService service) {
        this.service = service;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Context for viewing the quiz
        server.createContext("/quiz", new QuizHandler());
        // Context for processing results
        server.createContext("/submit", new SubmitHandler());
        
        server.setExecutor(null); 
        server.start();
        System.out.println("Server live: http://localhost:8080/quiz");
    }

    // Handler to display the questions (Same as yesterday)
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

    // NEW: Handler to process the submitted answers
    class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Read the body of the POST request
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine(); // Format: q0=1&q1=2...

                // For now, let's just confirm we received the data
                String response = "<html><body><h1>Quiz Submitted!</h1>" +
                                  "<p>Data received: " + formData + "</p>" +
                                  "<a href='/quiz'>Try Again</a></body></html>";

                byte[] bytes = response.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }
}
