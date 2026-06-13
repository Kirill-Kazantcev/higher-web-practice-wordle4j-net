package ru.yandex.practicum.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exception.io.DictionaryEmptyException;
import ru.yandex.practicum.exception.io.DictionaryFileNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryLoaderTest {
    private WordleDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new WordleDictionaryLoader();
    }

    @Test
    void load_withValidFiveLetterWords_shouldLoad(@TempDir Path tempDir)
            throws DictionaryFileNotFoundException, DictionaryEmptyException, IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("аббат", "вагон", "ухарь"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getAllWords();

        assertEquals(3, words.size());
        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("вагон"));
        assertTrue(words.contains("ухарь"));
    }

    @Test
    void load_emptyFile_shouldThrowDictionaryEmptyException(@TempDir Path tempDir) throws IOException {
        Path dictFile = tempDir.resolve("empty.txt");
        Files.write(dictFile, List.of());

        assertThrows(DictionaryEmptyException.class, () -> loader.load(dictFile.toString()));
    }

    @Test
    void load_missingFile_shouldThrowDictionaryFileNotFoundException() {
        assertThrows(DictionaryFileNotFoundException.class, () -> loader.load("non_existent_file.txt"));
    }

    @Test
    void load_withMixedCase_shouldNormalizeToLowercase(@TempDir Path tempDir)
            throws DictionaryFileNotFoundException, DictionaryEmptyException, IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("АББАТ", "Вагон", "УХАРЬ"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getAllWords();

        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("вагон"));
        assertTrue(words.contains("ухарь"));
    }

    @Test
    void load_withYoReplacement_shouldReplaceYoWithE(@TempDir Path tempDir)
            throws DictionaryFileNotFoundException, DictionaryEmptyException, IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("ёжики", "ёлка"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getAllWords();

        assertTrue(words.contains("ежики"));
        assertFalse(words.contains("елка"));
        assertEquals(1, words.size());
    }

    @Test
    void load_withValidYoWords_shouldReplaceAndLoad(@TempDir Path tempDir)
            throws DictionaryFileNotFoundException, DictionaryEmptyException, IOException {
        Path dictFile = tempDir.resolve("words.txt");
        // Правильные слова длины 5 с буквой 'ё'
        Files.write(dictFile, List.of("сёгун", "тёлка", "пёсий"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getAllWords();

        assertTrue(words.contains("сегун"));
        assertTrue(words.contains("телка"));
        assertTrue(words.contains("песий"));
        assertEquals(3, words.size());
    }

    @Test
    void load_withInvalidCharacters_shouldSkipWord(@TempDir Path tempDir)
            throws DictionaryFileNotFoundException, DictionaryEmptyException, IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("аббат", "hello", "вагон", "12345"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getAllWords();

        assertEquals(2, words.size());
        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("вагон"));
        assertFalse(words.contains("hello"));
        assertFalse(words.contains("12345"));
    }
}