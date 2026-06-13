package ru.yandex.practicum.game;

/**
 * Определяет, как вычисляется строка подсказки (+ ^ -) для пары "догадка – ответ".
 */
@FunctionalInterface
public interface WordMatcher {
    String match(String guess, String secret);
}