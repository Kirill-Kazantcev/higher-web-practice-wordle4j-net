package ru.yandex.practicum;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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
import java.util.stream.Collectors;

/**
 * HTTP-сервер для сбора и предоставления статистики игры Wordle.
 * <p>
 * Сервер работает на порту 8081 и предоставляет три эндпоинта:
 * <ul>
 *   <li><b>POST /result</b> – сохранение результата игры (тело JSON: nickname, win, steps, hintsUsed)</li>
 *   <li><b>GET /top</b> – получение топ-10 игроков по количеству побед (JSON)</li>
 *   <li><b>GET /stats?nickname=...</b> – получение статистики конкретного игрока (JSON)</li>
 * </ul>
 * </p>
 * <p>
 * Данные сохраняются в файл {@code stats.txt} в формате:
 * {@code nickname:wins:losses:hintsUsed:totalSteps}
 * </p>
 */
public class WordleServer {
    /** Порт для HTTP-сервера */
    private static final int PORT = 8081;
    /** Имя файла для хранения статистики */
    private static final String STATS_FILE = "stats.txt";

    /** Карта статистики игроков (никнейм -> статистика) */
    private final Map<String, WordleServerStatisticLoader.PlayerStats> statistics;
    /** Загрузчик и сохранятель статистики */
    private final WordleServerStatisticLoader loader;

    /**
     * Конструктор сервера. Загружает существующую статистику из файла.
     */
    public WordleServer() {
        loader = new WordleServerStatisticLoader(STATS_FILE);
        statistics = loader.load();
    }

    /**
     * Точка входа для запуска сервера.
     *
     * @param args аргументы командной строки (не используются)
     * @throws IOException если не удалось запустить сервер
     */
    public static void main(String[] args) throws IOException {
        new WordleServer().start();
    }

    /**
     * Запускает HTTP-сервер на указанном порту и регистрирует обработчики эндпоинтов.
     *
     * @throws IOException если не удалось запустить сервер
     */
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/result", this::handlePostResult);
        server.createContext("/top", this::handleGetTop);
        server.createContext("/stats", this::handleGetStats);
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер статистики Wordle запущен на порту " + PORT);
    }

    /**
     * Обрабатывает POST-запрос на эндпоинт /result.
     * <p>
     * Ожидает JSON вида:
     * <pre>{"nickname": "Имя", "win": true/false, "steps": N, "hintsUsed": N}</pre>
     * Обновляет статистику игрока и сохраняет её в файл.
     * </p>
     *
     * @param exchange HTTP-обмен с клиентом
     * @throws IOException при ошибке ввода-вывода
     */
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
        if (ps == null) {
            ps = new WordleServerStatisticLoader.PlayerStats(0, 0, 0, 0);
        }
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

    /**
     * Обрабатывает GET-запрос на эндпоинт /top.
     * <p>
     * Возвращает JSON с топ-10 игроков, отсортированных по убыванию количества побед.
     * Формат ответа:
     * <pre>{"top": [{"nickname": "Имя", "wins": N, "losses": N, "winRate": X.X}, ...]}</pre>
     * </p>
     *
     * @param exchange HTTP-обмен с клиентом
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleGetTop(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        List<Map.Entry<String, WordleServerStatisticLoader.PlayerStats>> top = statistics.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().wins, a.getValue().wins))
                .limit(10)
                .collect(Collectors.toList());

        StringBuilder json = new StringBuilder();
        json.append("{ \"top\": [");
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

    /**
     * Обрабатывает GET-запрос на эндпоинт /stats.
     * <p>
     * Параметр запроса: {@code nickname} – имя игрока.
     * Формат ответа:
     * <pre>{"nickname": "Имя", "wins": N, "losses": N, "hintsUsed": N, "avgSteps": X.X, "winRate": X.X}</pre>
     * </p>
     *
     * @param exchange HTTP-обмен с клиентом
     * @throws IOException при ошибке ввода-вывода
     */
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

    /**
     * Примитивный парсер JSON для извлечения пар ключ-значение.
     * <p>
     * Ожидает формат: {@code {"key1":"value1","key2":"value2"}}
     * </p>
     *
     * @param body тело запроса в формате JSON
     * @return карта с распарсенными значениями
     */
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

    /**
     * Парсит query-строку вида {@code key=value&key2=value2}.
     *
     * @param query query-строка URL
     * @return карта с параметрами запроса
     */
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

    /**
     * Читает тело HTTP-запроса в виде строки.
     *
     * @param exchange HTTP-обмен с клиентом
     * @return содержимое тела запроса
     * @throws IOException при ошибке чтения
     */
    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Отправляет HTTP-ответ клиенту.
     *
     * @param exchange   HTTP-обмен с клиентом
     * @param statusCode HTTP-статус ответа (200, 400, 405 и т.д.)
     * @param response   тело ответа
     * @throws IOException при ошибке записи
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Экранирует специальные символы для использования в JSON-строке.
     *
     * @param s исходная строка
     * @return строка с экранированными символами
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}