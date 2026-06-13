package ru.yandex.practicum.exception.game;

/**
 * Исключение, если слово имеет неверную длину.
 */
public class InvalidWordLengthException extends GameException {
    public InvalidWordLengthException(String message) {
        super(message);
    }
}