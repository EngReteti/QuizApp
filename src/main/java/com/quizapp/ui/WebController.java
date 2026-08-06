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
        "input[type='text'], select, input[type='submit'] { width: 100%; padding: 12px; margin: 10px 0; border-radius: 5px; border: 1px solid #ddd; box-sizing: border-box; font-size: 15px; }" +
        "input[type='submit'] { background: #3498db; color: white; border: none; cursor: pointer; font-size: 16px; font-weight: bold; }" +
        "input[type='submit']:hover { background: #2980b9; }" +
        ".question { margin-bottom: 20px; padding: 15px; border-left: 5px solid #3498db; background: #eef7fd; border-radius: 0 5px 5px 0; }" +
        "#timer { background: #e74c3c; color: white; padding: 10px; text-align: center; border-radius: 4px; margin-bottom: 20px; font-weight: bold; }" +
        "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
        "th, td { text-align: left; padding: 12px; border-bottom: 1px solid #ddd; }" +
        "th { background-color: #3498db; color: white; }" +
        ".btn { display: block; width: 100%; background: #3498db; color: white; padding: 12px; text-decoration: none; border-radius: 5px; text-align: center; box-sizing: border-box; margin-top: 10px; }" +
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
            Set<String> categories = service.getCategories();
            StringBuilder catOptions = new StringBuilder("<option value='All'>All Categories</option>");
            for (String cat : categories) {
                catOptions.append("<option value='").append(cat).append("'>").append(cat).append("</option>");
            }

            String response = "<html><head>" + CSS + "</head><body><div class='container'>" +
                "<h1>QuizApp Setup</h1>" +
                "<form action='/quiz' method='GET'>" +
                "<label><b>Your Name:</b></label>" +
                "<input type='text' name='name' required placeholder='Enter your name'>" +
                "<label><b>Select Category:</b></label>" +
                "<select name='category'>" + catOptions.toString() + "</select>" +
                "<input type='submit' value='Start Quiz'>" +
                "</form>" +
                "<a href='/leaderboard' class='btn'>View Leaderboard</a>" +
                "</div></body></html>";
            sendResponse(t, response);
        }
    }

    class QuizHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQuery(query);
            String name = queryParams.getOrDefault("name", "Anonymous");
            String category = queryParams.getOrDefault("category", "All");

            List<Question> questions = service.getQuestionsByCategory(category);
            StringBuilder resp = new StringBuilder("<html><head>" + CSS + "</head><body><div class='container'>");
            resp.append("<div id='timer'>Time Remaining: 60s</div>");
            resp.append("<h1>").append(URLDecoder.decode(name, "UTF-8")).append("'s Quiz</h1>");
            resp.append("<p style='text-align:center; color: #7f8c8d;'>Category: <b>").append(category).append("</b> (").append(questions.size()).append(" questions)</p>");
            resp.append("<form id='quizForm' action='/submit' method='POST'>");
            resp.append("<input type='hidden' name='username' value='").append(name).append("'>");

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                resp.append("<div class='question'><b>").append(i+1).append(". ").append(q.getPrompt()).append("</b><br><br>");
                for (int j = 0; j < q.getOptions().size(); j++) {
                    resp.append("<label><input type='radio' name='q").append(i).append("' value='").append(j).append("'> ").append(q.getOptions().get(j)).append("</label><br>");
                }
                resp.append("</div>");
            }
            resp.append("<input type='submit' value='Submit Quiz'></form></div>")
                .append("<script>let s=60;setInterval(()=>{if(s<=0)document.getElementById('quizForm').submit();else document.getElementById('timer').innerText='Time Remaining: '+(s--)+'s'},1000);</script></body></html>");
            sendResponse(t, resp.toString());
        }
    }

    class SubmitHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) return;
            BufferedReader r = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8"));
            String data = r.readLine();
            Map<String, String> p = new HashMap<>();
            if (data != null) {
                for (String pair : data.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length > 1) p.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
                }
            }
            String name = p.getOrDefault("username", "Anonymous");
            List<Question> questions = service.getAllQuestions();
            int score = 0;
            for (int i = 0; i < questions.size(); i++) {
                String ans = p.get("q" + i);
                if (ans != null && questions.get(i).isCorrect(Integer.parseInt(ans))) score++;
            }
            service.saveResult(name, score, questions.size());
            String resp = "<html><head>" + CSS + "</head><body><div class='container'><h1>Quiz Completed!</h1><h2>Score: " + score + "/" + questions.size() + "</h2><a href='/leaderboard' class='btn'>View Leaderboard</a><a href='/' class='btn' style='background:#2ec4b6;'>Home Page</a></div></body></html>";
            sendResponse(t, resp);
        }
    }

    class LeaderboardHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            StringBuilder rows = new StringBuilder();
            File file = new File("results.txt");
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    List<String> lines = br.lines().collect(Collectors.toList());
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

    private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) map.put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
            }
        }
        return map;
    }

    private void sendResponse(HttpExchange t, String r) throws IOException {
        byte[] b = r.getBytes();
        t.sendResponseHeaders(200, b.length);
        OutputStream os = t.getResponseBody();
        os.write(b);
        os.close();
    }
}
