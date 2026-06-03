package ru.yandex.practicum.exception;

/**
 * Исключение, выбрасываемое, если введённое пользователем слово
 * не соответствует формату (например, имеет длину не 5 символов
 * или содержит недопустимые символы).
 */
public class InvalidWordFormatException extends GameException {
    public InvalidWordFormatException(String message) {
        super(message);
    }
}