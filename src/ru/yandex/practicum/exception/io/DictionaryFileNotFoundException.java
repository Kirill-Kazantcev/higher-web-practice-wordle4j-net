package ru.yandex.practicum.exception.io;

/**
 * Исключение, если файл словаря не найден.
 */
public class DictionaryFileNotFoundException extends Exception {

    public DictionaryFileNotFoundException(String message) {
        super(message);
    }

    public DictionaryFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}