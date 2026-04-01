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
        // Start server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Create an endpoint at "/quiz"
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
            
            response.append("<html><body><h1>Computer Science Quiz</h1><ul>");
            for (Question q : questions) {
                response.append("<li><b>").append(q.getPrompt()).append("</b><br>");
                response.append("Options: ").append(String.join(", ", q.getOptions())).append("</li><br>");
            }
            response.append("</ul></body></html>");

            byte[] bytes = response.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
