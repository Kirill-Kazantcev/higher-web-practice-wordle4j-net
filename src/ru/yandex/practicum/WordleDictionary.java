package ru.yandex.practicum;

import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.util.List;

/**
 * Класс, представляющий словарь игры.
 * <p>
 * Хранит список допустимых слов, обеспечивает проверку наличия
 * слова в словаре и предоставляет методы для анализа совпадений.
 * </p>
 */
public class WordleDictionary {
    private final List<String> words;

    /**
     * Конструктор словаря.
     *
     * @param words список допустимых слов.
     */
    public WordleDictionary(List<String> words) {
        this.words = words;
    }

    /**
     * Возвращает копию списка слов.
     *
     * @return список всех слов в словаре.
     */
    public List<String> getWords() {
        return List.copyOf(words);
    }

    /**
     * Проверяет, что переданное слово есть в словаре и имеет правильную длину.
     *
     * @param word слово для проверки.
     * @throws InvalidWordFormatException      если длина слова не равна 5.
     * @throws WordNotFoundInDictionaryException если слово отсутствует в словаре.
     */
    public void validateWord(String word) throws InvalidWordFormatException, WordNotFoundInDictionaryException {
        if (word.length() != 5) {
            throw new InvalidWordFormatException("Слово должно состоять ровно из 5 букв.");
        }
        if (!words.contains(word)) {
            throw new WordNotFoundInDictionaryException(word);
        }
    }

    /**
     * Сравнивает слово-догадку с загаданным словом и возвращает
     * строку-подсказку.
     * <p>
     * Символы в строке-результате означают:
     * '+' — буква есть на правильной позиции,
     * '^' — буква есть в слове, но на другой позиции,
     * '-' — буквы нет в загаданном слове.
     * </p>
     *
     * @param guess   слово-догадка.
     * @param answer загаданное слово.
     * @return строка подсказки длиной 5 символов.
     */
    public static String analyze(String guess, String answer) {
        StringBuilder result = new StringBuilder("-----");
        boolean[] usedInAnswer = new boolean[5];

        // Сначала отмечаем точные совпадения
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result.setCharAt(i, '+');
                usedInAnswer[i] = true;
            }
        }

        // Затем ищем буквы, которые есть в слове, но не на своей позиции
        for (int i = 0; i < 5; i++) {
            if (result.charAt(i) == '+') {
                continue;
            }
            char gChar = guess.charAt(i);
            for (int j = 0; j < 5; j++) {
                if (!usedInAnswer[j] && gChar == answer.charAt(j)) {
                    result.setCharAt(i, '^');
                    usedInAnswer[j] = true;
                    break;
                }
            }
        }

        return result.toString();
    }
}