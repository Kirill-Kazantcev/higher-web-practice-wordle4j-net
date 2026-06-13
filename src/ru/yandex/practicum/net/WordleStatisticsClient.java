package ru.yandex.practicum.net;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Отвечает за общение с сервером статистики.
 */
public class WordleStatisticsClient {
    private final String serverUrl;
    private final HttpClient httpClient;

    public WordleStatisticsClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Отправляет результат игры на сервер.
     *
     * @param nickname  никнейм игрока
     * @param win       победа или поражение
     * @param steps     количество использованных ходов
     * @param hintsUsed количество подсказок
     * @return true, если отправка успешна
     */
    public boolean sendGameResult(String nickname, boolean win, int steps, int hintsUsed) {
        String json = String.format("{\"nickname\":\"%s\",\"win\":%b,\"steps\":%d,\"hintsUsed\":%d}",
                escapeJson(nickname), win, steps, hintsUsed);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/result"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Получает топ-10 игроков в виде JSON-строки.
     *
     * @return JSON с топом или null при ошибке
     */
    public String fetchTopPlayers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/top"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Получает статистику игрока по никнейму.
     *
     * @param nickname никнейм
     * @return JSON со статистикой или null
     */
    public String fetchPlayerStats(String nickname) {
        try {
            String encoded = URLEncoder.encode(nickname, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/stats?nickname=" + encoded))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}