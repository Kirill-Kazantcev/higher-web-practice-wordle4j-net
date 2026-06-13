package ru.yandex.practicum.game;

import java.util.List;

/**
 * Абстракция словаря слов.
 * Позволяет подменять реализацию (файл, БД, генератор).
 */
public interface Dictionary {
    List<String> getAllWords();
    boolean contains(String word);
    int size();
}