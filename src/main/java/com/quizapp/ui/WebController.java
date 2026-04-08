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

    // CSS for a professional look
    private static final String CSS = 
        "<style>" +
        "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; display: flex; justify-content: center; }" +
        ".container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); max-width: 600px; width: 100%; }" +
        "h1 { color: #2c3e50; text-align: center; border-bottom: 2px solid #3498db; padding-bottom: 10px; }" +
        ".question { margin-bottom: 20px; padding: 15px; border-left: 5px solid #3498db; background: #eef7fd; }" +
        "input[type='radio'] { margin-right: 10px; }" +
        "input[type='submit'] { background: #3498db; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; width: 100%; font-size: 16px; margin-top: 20px; }" +
        "input[type='submit']:hover { background: #2980b9; }" +
        "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
        "th, td { text-align: left; padding: 12px; border-bottom: 1px solid #ddd; }" +
        "th { background-color: #3498db; color: white; }" +
        "</style>";

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
            response.append("<html><head><title>QuizApp</title>").append(CSS).append("</head><body>");
            response.append("<div class='container'><h1>Computer Science Quiz</h1>");
            response.append("<form action='/submit' method='POST'>");
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                response.append("<div class='question'><b>").append(i + 1).append(". ").append(q.getPrompt()).append("</b><br><br>");
                List<String> options = q.getOptions();
                char label = 'A';
                for (int j = 0; j < options.size(); j++) {
                    response.append("<label><input type='radio' name='q").append(i)
                            .append("' value='").append(j).append("'> ")
                            .append(label).append(") ").append(options.get(j)).append("</label><br>");
                    label++;
                }
                response.append("</div>");
            }
            response.append("<input type='submit' value='Submit Quiz'>");
            response.append("</form></div></body></html>");
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
                report.append("<table><tr><th>#</th><th>Question</th><th>Result</th></tr>");

                for (int i = 0; i < questions.size(); i++) {
                    Question q = questions.get(i);
                    String userChoiceStr = answers.get("q" + i);
                    String status = "<b style='color:#e74c3c;'>Incorrect</b>";
                    if (userChoiceStr != null && q.isCorrect(Integer.parseInt(userChoiceStr))) {
                        score++;
                        status = "<b style='color:#27ae60;'>Correct</b>";
                    }
                    report.append("<tr><td>").append(i + 1).append("</td><td>").append(q.getPrompt()).append("</td><td>").append(status).append("</td></tr>");
                }
                report.append("</table>");

                String resultPage = "<html><head>" + CSS + "</head><body><div class='container'><h1>Results</h1>" +
                                    "<h2 style='text-align:center;'>Score: " + score + " / " + questions.size() + "</h2>" +
                                    report.toString() +
                                    "<br><a href='/quiz' style='display:block; text-align:center; color:#3498db; text-decoration:none;'>Try Again</a></div></body></html>";

                byte[] bytes = resultPage.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }
}
