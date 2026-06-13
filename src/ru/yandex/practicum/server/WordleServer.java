package ru.yandex.practicum.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.exception.io.LogFileCreationException;
import ru.yandex.practicum.util.ServerJsonUtils;
import ru.yandex.practicum.util.ConfigLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordleServer {
    private static final String STATS_FILE = "statistics.json";
    private static final String SERVER_LOG_FILE = "wordle-server.log";

    private final int port;
    private final Map<String, WordleServerStatisticLoader.PlayerStats> statistics;
    private final WordleServerStatisticLoader loader;
    private final PrintWriter log;
    private HttpServer httpServer;
    private ExecutorService executor;

    public WordleServer(int port, String statsFile, PrintWriter log) {
        this.port = port;
        this.log = log;
        this.loader = new WordleServerStatisticLoader(statsFile);
        this.statistics = loader.load();
    }

    public static void main(String[] args) {
        try (PrintWriter log = createLogWriter()) {
            try {
                ConfigLoader config = new ConfigLoader("application.properties");
                int port = config.getInt("server.port");
                WordleServer server = new WordleServer(port, STATS_FILE, log);
                server.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.println("Сервер останавливается...");
                    server.stop();
                }));
            } catch (Exception e) {
                log.println("Ошибка запуска сервера: " + e.getMessage());
                e.printStackTrace(log);
                System.err.println("Ошибка запуска сервера: " + e.getMessage());
            }
        } catch (LogFileCreationException e) {
            System.err.println("Не удалось создать лог-файл: " + e.getMessage());
        }
    }

    private static PrintWriter createLogWriter() throws LogFileCreationException {
        try {
            return new PrintWriter(SERVER_LOG_FILE, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new LogFileCreationException("Не удалось создать лог-файл: " + SERVER_LOG_FILE, e);
        }
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/result", this::handlePostResult);
        httpServer.createContext("/top", this::handleGetTop);
        httpServer.createContext("/stats", this::handleGetStats);

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "wordle-http");
            thread.setDaemon(true);
            return thread;
        });
        httpServer.setExecutor(executor);
        httpServer.start();

        System.out.println("Сервер статистики Wordle запущен на порту " + port);
        log.println("WordleServer запущен на порту " + port);
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(1);
            httpServer = null;
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
        log.println("WordleServer остановлен");
    }

    private void handlePostResult(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            String body = readBody(exchange);

            String nickname = ServerJsonUtils.extractJsonValue(body, "nickname");
            String stepsStr = ServerJsonUtils.extractJsonValue(body, "steps");
            String usedHintsStr = ServerJsonUtils.extractJsonValue(body, "usedHints");

            if (nickname == null || nickname.isBlank() || stepsStr == null) {
                sendResponse(exchange, 400, "{\"error\": \"Некорректные данные\"}");
                return;
            }

            int steps;
            try {
                steps = Integer.parseInt(stepsStr);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Некорректное количество шагов\"}");
                return;
            }

            boolean usedHints = "true".equalsIgnoreCase(usedHintsStr);

            if (steps <= 0) {
                sendResponse(exchange, 400, "{\"error\": \"Некорректные данные\"}");
                return;
            }

            WordleServerStatisticLoader.PlayerStats ps = statistics.computeIfAbsent(nickname,
                    k -> new WordleServerStatisticLoader.PlayerStats(0, 0, 0, 0));

            ps.wins++;
            ps.totalSteps += steps;
            if (usedHints) {
                ps.hintsUsed++;
            }

            loader.save(statistics);
            sendResponse(exchange, 200, "{\"status\": \"ok\"}");
            log.println("Сохранена победа игрока: " + nickname + " (шагов: " + steps + ")");

        } catch (Exception e) {
            log.println("Ошибка POST: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Внутренняя ошибка сервера\"}");
        } finally {
            exchange.close();
        }
    }

    private void handleGetTop(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            var top = statistics.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue().wins, a.getValue().wins))
                    .limit(10)
                    .toList();

            StringBuilder json = new StringBuilder();
            json.append("{\"top\":[");

            for (int i = 0; i < top.size(); i++) {
                var entry = top.get(i);
                if (i > 0) json.append(",");

                double winRate = entry.getValue().getWinRate();

                json.append(String.format(java.util.Locale.US,
                        "{\"nickname\":\"%s\",\"wins\":%d,\"losses\":%d,\"winRate\":%.1f}",
                        escapeJson(entry.getKey()),
                        entry.getValue().wins,
                        entry.getValue().losses,
                        winRate
                ));
            }

            json.append("]}");
            sendResponse(exchange, 200, json.toString());
            log.println("Отправлен топ " + top.size() + " игроков");

        } catch (Exception e) {
            log.println("Ошибка GET /top: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Внутренняя ошибка сервера\"}");
        } finally {
            exchange.close();
        }
    }

    private void handleGetStats(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            String query = exchange.getRequestURI().getQuery();
            String nickname = extractNickname(query);

            if (nickname == null || nickname.isBlank()) {
                sendResponse(exchange, 400, "{\"error\": \"nickname required\"}");
                return;
            }

            WordleServerStatisticLoader.PlayerStats ps = statistics.get(nickname);
            if (ps == null) {
                sendResponse(exchange, 200, String.format(java.util.Locale.US,
                        "{\"nickname\":\"%s\",\"wins\":0,\"losses\":0,\"hintsUsed\":0,\"avgSteps\":0.0,\"winRate\":0.0}",
                        escapeJson(nickname)));
                return;
            }

            String json = String.format(java.util.Locale.US,
                    "{\"nickname\":\"%s\",\"wins\":%d,\"losses\":%d,\"hintsUsed\":%d,\"avgSteps\":%.2f,\"winRate\":%.1f}",
                    escapeJson(nickname), ps.wins, ps.losses, ps.hintsUsed, ps.getAvgSteps(), ps.getWinRate());
            sendResponse(exchange, 200, json);
            log.println("Отправлена статистика для игрока: " + nickname);

        } catch (Exception e) {
            log.println("Ошибка GET /stats: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Внутренняя ошибка сервера\"}");
        } finally {
            exchange.close();
        }
    }

    private String extractNickname(String query) {
        if (query == null || query.isBlank()) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("nickname=")) {
                return URLDecoder.decode(param.substring(9), StandardCharsets.UTF_8);
            }
        }
        return null;
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