package ru.yandex.practicum.client;

import ru.yandex.practicum.exception.io.DictionaryEmptyException;
import ru.yandex.practicum.exception.io.DictionaryFileNotFoundException;
import ru.yandex.practicum.game.GameConstants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader implements GameConstants {

    public WordleDictionary load(String filePath)
            throws DictionaryFileNotFoundException, DictionaryEmptyException {

        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = normalizeWord(line);
                if (normalized != null && normalized.length() == WORD_LENGTH) {
                    words.add(normalized);
                }
            }
        } catch (IOException e) {
            throw new DictionaryFileNotFoundException("Не удалось прочитать файл словаря: " + filePath, e);
        }

        if (words.isEmpty()) {
            throw new DictionaryEmptyException("Словарь не содержит слов подходящей длины (" + WORD_LENGTH + " букв)");
        }

        return new WordleDictionary(words);
    }

    private String normalizeWord(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String normalized = raw.trim().toLowerCase().replace('ё', 'е');
        if (!normalized.matches("[а-я]+")) return null;
        return normalized;
    }
}