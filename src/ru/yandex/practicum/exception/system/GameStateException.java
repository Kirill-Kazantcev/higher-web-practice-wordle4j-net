package ru.yandex.practicum.exception.system;

/**
 * Unchecked исключение для ошибок состояния игры.
 */
public class GameStateException extends RuntimeException {

    public GameStateException(String message) {
        super(message);
    }
}