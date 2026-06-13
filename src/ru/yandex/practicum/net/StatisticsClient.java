package ru.yandex.practicum.net;

/**
 * Клиент для отправки и получения статистики.
 */
public interface StatisticsClient {
    boolean sendGameResult(String nickname, boolean win, int steps, int hintsUsed);
    String fetchTopPlayers();
    String fetchPlayerStats(String nickname);
}