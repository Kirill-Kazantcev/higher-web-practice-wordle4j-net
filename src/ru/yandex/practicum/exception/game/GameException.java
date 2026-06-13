package ru.yandex.practicum.exception.game;

/**
 * Базовое исключение для всех игровых ошибок.
 */
public class GameException extends Exception {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}