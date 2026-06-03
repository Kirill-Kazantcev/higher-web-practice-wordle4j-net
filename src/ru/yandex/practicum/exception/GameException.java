package ru.yandex.practicum.exception;

/**
 * Базовое исключение для всех игровых ошибок.
 * Используется для ситуаций, возникающих в процессе игры
 * (например, нарушение правил, логические ошибки).
 */
public class GameException extends Exception {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}