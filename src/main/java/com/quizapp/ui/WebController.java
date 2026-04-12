package com.quizapp.ui;

import com.sun.net.httpserver.*;
import com.quizapp.service.QuestionService;
import com.quizapp.model.Question;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

public class WebController {
    private QuestionService service;

    private static final String CSS = 
        "<style>" +
        "body { font-family: 'Segoe UI', sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; display: flex; justify-content: center; }" +
        ".container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); max-width: 600px; width: 100%; }" +
        "h1 { color: #2c3e50; text-align: center; }" +
        "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
        "th, td { text-align: left; padding: 12px; border-bottom: 1px solid #ddd; }" +
        "th { background-color: #3498db; color: white; }" +
        "tr:nth-child(even) { background-color: #f9f9f9; }" +
        ".btn { display: inline-block; background: #3498db; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; margin-top: 20px; text-align: center; }" +
        "</style>";

    public WebController(QuestionService service) { this.service = service; }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/quiz", new QuizHandler());
        server.createContext("/submit", new SubmitHandler());
        server.createContext("/leaderboard", new LeaderboardHandler());
        server.start();
        System.out.println("Server live at http://localhost:8080/");
    }

    class HomeHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "<html><head>" + CSS + "</head><body><div class='container'>" +
                "<h1>QuizApp</h1>" +
                "<form action='/quiz' method='GET'>" +
                "<input type='text' name='name' required placeholder='Your Name' style='width:100%; padding:10px; margin-bottom:10px;'>" +
                "<input type='submit' value='Start Quiz' style='width:100%; padding:10px; background:#3498db; color:white; border:none; cursor:pointer;'>" +
                "</form>" +
                "<a href='/leaderboard' class='btn'>View Leaderboard</a>" +
                "</div></body></html>";
            sendResponse(t, response);
        }
    }

    // LeaderboardHandler: Reads results.txt and sorts them
    class LeaderboardHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            StringBuilder rows = new StringBuilder();
            File file = new File("results.txt");
            
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    List<String> lines = br.lines().collect(Collectors.toList());
                    // Sort lines by score (simple descending order based on the string segment)
                    lines.sort((a, b) -> b.split("Score: ")[1].compareTo(a.split("Score: ")[1]));
                    
                    for (String line : lines) {
                        String[] parts = line.split(" \| ");
                        if (parts.length >= 3) {
                            rows.append("<tr><td>").append(parts[1].replace("Name: ", ""))
                                .append("</td><td>").append(parts[2].replace("Score: ", ""))
                                .append("</td><td>").append(parts[0]).append("</td></tr>");
                        }
                    }
                }
            }

            String response = "<html><head>" + CSS + "</head><body><div class='container'>" +
                "<h1>Global Leaderboard</h1>" +
                "<table><tr><th>Name</th><th>Score</th><th>Date</th></tr>" + rows.toString() + "</table>" +
                "<a href='/' class='btn'>Back Home</a>" +
                "</div></body></html>";
            sendResponse(t, response);
        }
    }

    // [Previous QuizHandler and SubmitHandler methods remain logically same, truncated for brevity]
    class QuizHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String name = (query != null && query.contains("name=")) ? query.split("=")[1] : "Anonymous";
            List<Question> questions = service.getAllQuestions();
            StringBuilder resp = new StringBuilder("<html><head>" + CSS + "</head><body><div class='container'>");
            resp.append("<h1>Hi ").append(URLDecoder.decode(name, "UTF-8")).append("!</h1>");
            resp.append("<form id='quizForm' action='/submit' method='POST'>");
            resp.append("<input type='hidden' name='username' value='").append(name).append("'>");
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                resp.append("<p><b>").append(i+1).append(". ").append(q.getPrompt()).append("</b><br>");
                for (int j = 0; j < q.getOptions().size(); j++) {
                    resp.append("<label><input type='radio' name='q").append(i).append("' value='").append(j).append("'> ").append(q.getOptions().get(j)).append("</label><br>");
                }
                resp.append("</p>");
            }
            resp.append("<input type='submit' value='Finish' style='width:100%; padding:10px; background:#3498db; color:white; border:none;'></form></div></body></html>");
            sendResponse(t, resp.toString());
        }
    }

    class SubmitHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) return;
            BufferedReader r = new BufferedReader(new InputStreamReader(t.getRequestBody()));
            String data = r.readLine();
            Map<String, String> p = new HashMap<>();
            for (String pair : data.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length > 1) p.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
            }
            String name = p.getOrDefault("username", "Anonymous");
            int score = 0;
            for (int i = 0; i < service.getAllQuestions().size(); i++) {
                String ans = p.get("q" + i);
                if (ans != null && service.getAllQuestions().get(i).isCorrect(Integer.parseInt(ans))) score++;
            }
            service.saveResult(name, score, service.getAllQuestions().size());
            String resp = "<html><head>" + CSS + "</head><body><div class='container'><h1>Score: " + score + "/10</h1><a href='/leaderboard' class='btn'>View Leaderboard</a></div></body></html>";
            sendResponse(t, resp);
        }
    }

    private void sendResponse(HttpExchange t, String r) throws IOException {
        byte[] b = r.getBytes();
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.getResponseBody().close();
    }
}
