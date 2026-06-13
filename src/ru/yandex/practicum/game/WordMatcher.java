package ru.yandex.practicum.game;

/**
 * Интерфейс для вычисления строки подсказки (+ ^ -).
 */
@FunctionalInterface
public interface WordMatcher {
    String match(String guess, String secret);
}