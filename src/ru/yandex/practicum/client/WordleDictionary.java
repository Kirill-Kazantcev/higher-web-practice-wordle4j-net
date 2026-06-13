package ru.yandex.practicum.client;

import ru.yandex.practicum.game.Dictionary;
import ru.yandex.practicum.game.GameConstants;
import ru.yandex.practicum.exception.game.GameException;
import ru.yandex.practicum.exception.game.InvalidWordLengthException;
import ru.yandex.practicum.exception.game.InvalidAlphabetException;
import ru.yandex.practicum.exception.game.WordNotFoundInDictionaryException;

import java.util.*;

public class WordleDictionary implements Dictionary, GameConstants {
    private final List<String> words;
    private final Set<String> wordSet;

    public WordleDictionary(List<String> words) {
        this.words = List.copyOf(words);
        this.wordSet = new HashSet<>(this.words);
    }

    @Override
    public List<String> getAllWords() { return words; }
    @Override
    public boolean contains(String word) { return wordSet.contains(normalize(word)); }
    @Override
    public int size() { return words.size(); }

    @Override
    public void validate(String word) throws GameException {
        String normalized = normalize(word);
        if (normalized.length() != WORD_LENGTH) {
            throw new InvalidWordLengthException("Слово должно состоять ровно из " + WORD_LENGTH + " букв.");
        }
        if (!isValidRussianWord(normalized)) {
            throw new InvalidAlphabetException("Слово должно состоять только из русских букв");
        }
        if (!contains(word)) {
            throw new WordNotFoundInDictionaryException(word);
        }
    }

    public static String normalize(String word) {
        if (word == null) return "";
        return word.trim().toLowerCase().replace('ё', 'е');
    }

    public static boolean isValidRussianWord(String word) {
        if (word == null || word.isBlank()) return false;
        String normalized = normalize(word);
        if (normalized.length() != WORD_LENGTH) return false;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch < 'а' || ch > 'я') return false;
        }
        return true;
    }
}