package ru.yandex.practicum.exception;

/**
 * Исключение, если слово имеет неверную длину или содержит недопустимые символы.
 */
public class InvalidWordFormatException extends GameException {
    public InvalidWordFormatException(String message) {
        super(message);
    }
}