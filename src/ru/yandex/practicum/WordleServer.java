package ru.yandex.practicum;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.util.ConfigLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WordleServer {
    private final int port;
    private final Map<String, WordleServerStatisticLoader.PlayerStats> statistics;
    private final WordleServerStatisticLoader loader;

    public WordleServer(int port, String statsFile) {
        this.port = port;
        this.loader = new WordleServerStatisticLoader(statsFile);
        this.statistics = loader.load();
    }

    public static void main(String[] args) throws IOException {
        ConfigLoader config = new ConfigLoader("application.properties");
        int port = config.getInt("server.port");
        new WordleServer(port, "stats.txt").start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/result", this::handlePostResult);
        server.createContext("/top", this::handleGetTop);
        server.createContext("/stats", this::handleGetStats);
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер статистики Wordle запущен на порту " + port);
    }

    private void handlePostResult(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String body = readBody(exchange);
        Map<String, String> params = parseJson(body);
        String nickname = params.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            sendResponse(exchange, 400, "{\"error\": \"nickname required\"}");
            return;
        }
        boolean win = Boolean.parseBoolean(params.getOrDefault("win", "false"));
        int steps = Integer.parseInt(params.getOrDefault("steps", "0"));
        int hintsUsed = Integer.parseInt(params.getOrDefault("hintsUsed", "0"));

        WordleServerStatisticLoader.PlayerStats ps = statistics.get(nickname);
        if (ps == null) ps = new WordleServerStatisticLoader.PlayerStats(0, 0, 0, 0);
        if (win) {
            ps.wins++;
            ps.totalSteps += steps;
        } else {
            ps.losses++;
        }
        ps.hintsUsed += hintsUsed;
        statistics.put(nickname, ps);
        loader.save(statistics);
        sendResponse(exchange, 200, "{\"status\": \"ok\"}");
    }

    private void handleGetTop(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        List<Map.Entry<String, WordleServerStatisticLoader.PlayerStats>> top = statistics.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().wins, a.getValue().wins))
                .limit(10)
                .toList();

        StringBuilder json = new StringBuilder("{ \"top\": [");
        for (int i = 0; i < top.size(); i++) {
            var entry = top.get(i);
            String nickname = escapeJson(entry.getKey());
            int wins = entry.getValue().wins;
            int losses = entry.getValue().losses;
            double winRate = entry.getValue().getWinRate();
            json.append(String.format(Locale.US,
                    "{\"nickname\": \"%s\", \"wins\": %d, \"losses\": %d, \"winRate\": %.1f}",
                    nickname, wins, losses, winRate));
            if (i < top.size() - 1) json.append(", ");
        }
        json.append("] }");
        sendResponse(exchange, 200, json.toString());
    }

    private void handleGetStats(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String nickname = params.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            sendResponse(exchange, 400, "{\"error\": \"nickname required\"}");
            return;
        }
        WordleServerStatisticLoader.PlayerStats ps = statistics.get(nickname);
        if (ps == null) {
            sendResponse(exchange, 200, String.format(Locale.US,
                    "{\"nickname\": \"%s\", \"wins\": 0, \"losses\": 0, \"hintsUsed\": 0, \"avgSteps\": 0.0, \"winRate\": 0.0}",
                    escapeJson(nickname)));
            return;
        }
        String json = String.format(Locale.US,
                "{\"nickname\": \"%s\", \"wins\": %d, \"losses\": %d, \"hintsUsed\": %d, \"avgSteps\": %.2f, \"winRate\": %.1f}",
                escapeJson(nickname), ps.wins, ps.losses, ps.hintsUsed, ps.getAvgSteps(), ps.getWinRate());
        sendResponse(exchange, 200, json);
    }

    private Map<String, String> parseJson(String body) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = body.replace("{", "").replace("}", "").split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}