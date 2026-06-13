package ru.yandex.practicum.exception.io;

/**
 * Исключение, возникающее при невозможности создать лог-файл.
 */
public class LogFileCreationException extends Exception {

    public LogFileCreationException(String message) {
        super(message);
    }

    public LogFileCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}