package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {
    private WordleDictionary dictionary;

    @BeforeEach
    void setUp() {
        dictionary = new WordleDictionary(List.of("аббат", "вагон", "ухарь", "сосна"));
    }

    @Test
    void validateWord_validWord_shouldPass() {
        assertDoesNotThrow(() -> dictionary.validate("аббат"));
        assertDoesNotThrow(() -> dictionary.validate("вагон"));
    }

    @Test
    void validateWord_invalidLength_shouldThrowInvalidWordFormatException() {
        assertThrows(InvalidWordFormatException.class, () -> dictionary.validate("привет"));
        assertThrows(InvalidWordFormatException.class, () -> dictionary.validate("кот"));
        assertThrows(InvalidWordFormatException.class, () -> dictionary.validate("елка"));
    }

    @Test
    void validateWord_wordNotInDictionary_shouldThrowWordNotFoundInDictionaryException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> dictionary.validate("пенёк"));
    }

    @Test
    void getAllWords_shouldReturnCopy() {
        List<String> words = dictionary.getAllWords();
        assertEquals(4, words.size());
        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("вагон"));
        assertTrue(words.contains("ухарь"));
        assertTrue(words.contains("сосна"));
    }
}