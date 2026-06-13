package ru.yandex.practicum.game;

import ru.yandex.practicum.exception.game.GameException;
import java.util.List;

/**
 * Интерфейс словаря слов.
 */
public interface Dictionary {
    List<String> getAllWords();
    boolean contains(String word);
    int size();
    void validate(String word) throws GameException;
}