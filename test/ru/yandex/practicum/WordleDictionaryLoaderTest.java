package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exception.SystemException;

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
    void load_withValidFiveLetterWords_shouldLoad(@TempDir Path tempDir) throws IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("аббат", "вагон", "ухарь"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getWords();

        assertEquals(3, words.size());
        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("вагон"));
        assertTrue(words.contains("ухарь"));
    }

    @Test
    void load_emptyFile_shouldThrowSystemException(@TempDir Path tempDir) throws IOException {
        Path dictFile = tempDir.resolve("empty.txt");
        Files.write(dictFile, List.of());

        assertThrows(SystemException.class, () -> loader.load(dictFile.toString()));
    }

    @Test
    void load_missingFile_shouldThrowSystemException() {
        assertThrows(SystemException.class, () -> loader.load("non_existent_file.txt"));
    }

    @Test
    void load_withMixedCase_shouldNormalizeToLowercase(@TempDir Path tempDir) throws IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("АББАТ", "Вагон", "УХАРЬ"));

        WordleDictionary dictionary = loader.load(dictFile.toString());
        List<String> words = dictionary.getWords();

        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("вагон"));
        assertTrue(words.contains("ухарь"));
    }

    @Test
    void load_withYoReplacement_shouldReplaceYoWithE(@TempDir Path tempDir) throws IOException {
        Path dictFile = tempDir.resolve("words.txt");
        Files.write(dictFile, List.of("ёжик", "ёлка", "пёс"));

        assertThrows(SystemException.class, () -> loader.load(dictFile.toString()));
    }
}