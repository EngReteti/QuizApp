package com.quizapp.ui;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.quizapp.service.QuestionService;
import com.quizapp.model.Question;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class WebController {
    private QuestionService service;

    public WebController(QuestionService service) {
        this.service = service;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/quiz", new QuizHandler());
        server.setExecutor(null); 
        server.start();
        System.out.println("Server started at http://localhost:8080/quiz");
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
}
