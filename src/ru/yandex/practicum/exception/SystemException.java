package ru.yandex.practicum.exception;

/**
 * Исключение для системных ошибок, связанных с работой программы.
 * Например, ошибки ввода-вывода, некорректное состояние словаря.
 */
public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}