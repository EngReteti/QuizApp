package com.quizapp.ui;

import com.sun.net.httpserver.*;
import com.quizapp.service.QuestionService;
import com.quizapp.model.Question;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;

public class WebController {
    private QuestionService service;

    private static final String CSS = 
        "<style>" +
        "body { font-family: 'Segoe UI', sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; display: flex; justify-content: center; }" +
        ".container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); max-width: 600px; width: 100%; }" +
        "input[type='text'], input[type='submit'] { width: 100%; padding: 12px; margin: 10px 0; border-radius: 5px; border: 1px solid #ddd; box-sizing: border-box; }" +
        "input[type='submit'] { background: #3498db; color: white; border: none; cursor: pointer; font-size: 16px; }" +
        ".question { margin-bottom: 20px; padding: 15px; border-left: 5px solid #3498db; background: #eef7fd; }" +
        "#timer { background: #e74c3c; color: white; padding: 10px; text-align: center; border-radius: 4px; margin-bottom: 20px; font-weight: bold; }" +
        "</style>";

    public WebController(QuestionService service) { this.service = service; }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/quiz", new QuizHandler());
        server.createContext("/submit", new SubmitHandler());
        server.start();
        System.out.println("Server live at http://localhost:8080/");
    }

    class HomeHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "<html><head>" + CSS + "</head><body><div class='container'>" +
                "<h1>Welcome to QuizApp</h1>" +
                "<form action='/quiz' method='GET'>" +
                "<p>Enter your name to start:</p>" +
                "<input type='text' name='name' required placeholder='Your Name'>" +
                "<input type='submit' value='Start Quiz'>" +
                "</form></div></body></html>";
            sendResponse(t, response);
        }
    }

    class QuizHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String name = (query != null && query.contains("name=")) ? query.split("=")[1] : "Anonymous";
            
            List<Question> questions = service.getAllQuestions();
            StringBuilder response = new StringBuilder("<html><head>" + CSS + "</head><body><div class='container'>");
            response.append("<div id='timer'>Time Remaining: 60s</div>");
            response.append("<h1>Hi ").append(URLDecoder.decode(name, "UTF-8")).append("!</h1>");
            response.append("<form id='quizForm' action='/submit' method='POST'>");
            response.append("<input type='hidden' name='username' value='").append(name).append("'>");
            
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                response.append("<div class='question'><b>").append(i+1).append(". ").append(q.getPrompt()).append("</b><br>");
                for (int j = 0; j < q.getOptions().size(); j++) {
                    response.append("<label><input type='radio' name='q").append(i).append("' value='").append(j).append("'> ").append(q.getOptions().get(j)).append("</label><br>");
                }
                response.append("</div>");
            }
            response.append("<input type='submit' value='Finish Quiz'></form></div>" +
                "<script>let s=60;setInterval(()=>{if(s<=0)document.getElementById('quizForm').submit();else document.getElementById('timer').innerText='Time Remaining: '+(s--)+'s'},1000);</script></body></html>");
            sendResponse(t, response.toString());
        }
    }

    class SubmitHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody()));
            String data = reader.readLine();
            Map<String, String> params = new HashMap<>();
            for (String pair : data.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length > 1) params.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
            }

            String name = params.getOrDefault("username", "Anonymous");
            int score = 0;
            List<Question> questions = service.getAllQuestions();
            for (int i = 0; i < questions.size(); i++) {
                String ans = params.get("q" + i);
                if (ans != null && questions.get(i).isCorrect(Integer.parseInt(ans))) score++;
            }
            
            service.saveResult(name, score, questions.size());
            String response = "<html><head>" + CSS + "</head><body><div class='container'><h1>Well done, " + name + "!</h1>" +
                "<h2>Final Score: " + score + "/" + questions.size() + "</h2>" +
                "<a href='/'>Take another quiz</a></div></body></html>";
            sendResponse(t, response);
        }
    }

    private void sendResponse(HttpExchange t, String response) throws IOException {
        byte[] b = response.getBytes();
        t.sendResponseHeaders(200, b.length);
        OutputStream os = t.getResponseBody();
        os.write(b);
        os.close();
    }
}
