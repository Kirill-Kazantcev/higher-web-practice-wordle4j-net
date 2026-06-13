package ru.yandex.practicum.exception.game;

/**
 * Исключение, если слово содержит недопустимые символы
 */
public class InvalidAlphabetException extends GameException {

    public InvalidAlphabetException(String message) {
        super(message);
    }
}