package ru.yandex.practicum.exception.io;

/**
 * Исключение, если словарь не содержит подходящих слов для игры.
 */
public class DictionaryEmptyException extends Exception {

    public DictionaryEmptyException(String message) {
        super(message);
    }
}