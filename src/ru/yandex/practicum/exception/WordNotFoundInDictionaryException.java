package ru.yandex.practicum.exception;

/**
 * Исключение, если слово отсутствует в словаре.
 */
public class WordNotFoundInDictionaryException extends GameException {
    public WordNotFoundInDictionaryException(String word) {
        super("Слово \"" + word + "\" не найдено в словаре.");
    }
}