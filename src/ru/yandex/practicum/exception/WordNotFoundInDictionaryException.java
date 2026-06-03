package ru.yandex.practicum.exception;

/**
 * Исключение, выбрасываемое, если введённое пользователем слово
 * отсутствует в игровом словаре.
 */
public class WordNotFoundInDictionaryException extends GameException {
    public WordNotFoundInDictionaryException(String word) {
        super("Слово \"" + word + "\" не найдено в словаре.");
    }
}