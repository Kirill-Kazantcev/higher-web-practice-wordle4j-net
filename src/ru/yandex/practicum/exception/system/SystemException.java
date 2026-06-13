package ru.yandex.practicum.exception.system;

/**
 * Исключение для системных ошибок (ввод-вывод, некорректное состояние).
 */
public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}