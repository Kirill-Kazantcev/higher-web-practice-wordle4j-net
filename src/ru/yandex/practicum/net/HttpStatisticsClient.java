package ru.yandex.practicum.net;

import ru.yandex.practicum.util.JsonUtils;
import ru.yandex.practicum.client.WordleConstants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class HttpStatisticsClient implements StatisticsClient {
    private final HttpClient httpClient;
    private final String baseUrl;

    public HttpStatisticsClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.baseUrl = "http://" + WordleConstants.SERVER_HOST + ":" + WordleConstants.SERVER_PORT;
        System.out.println("HTTP Клиент настроен на: " + baseUrl);
    }

    @Override
    public boolean sendGameResult(String nickname, boolean win, int steps, int hintsUsed) {
        String json = JsonUtils.toWinSubmissionJson(nickname, steps, hintsUsed > 0);
        System.out.println("Отправка на: " + baseUrl + WordleConstants.STATS_POST_PATH);
        System.out.println("JSON: " + json);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + WordleConstants.STATS_POST_PATH))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Ответ сервера: " + response.statusCode());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка соединения: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String fetchTopPlayers() {
        String url = baseUrl + WordleConstants.TOP_GET_PATH;
        System.out.println("Запрос топа: " + url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Ответ сервера (топ): " + response.statusCode());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка получения топа: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String fetchPlayerStats(String nickname) {
        String url = baseUrl + WordleConstants.STATS_GET_PATH + "?nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);
        System.out.println("Запрос статистики: " + url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Ответ сервера (статистика): " + response.statusCode());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка получения статистики: " + e.getMessage());
            return null;
        }
    }
}