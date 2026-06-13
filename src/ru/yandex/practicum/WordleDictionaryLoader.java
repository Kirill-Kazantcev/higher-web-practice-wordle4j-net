package ru.yandex.practicum;

import ru.yandex.practicum.exception.SystemException;
import ru.yandex.practicum.game.GameConstants;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader implements GameConstants {
    public WordleDictionary load(String filePath) {
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
            throw new SystemException("Ошибка загрузки словаря: " + filePath, e);
        }
        if (words.isEmpty()) {
            throw new SystemException("Словарь пуст или не содержит слов подходящей длины.");
        }
        return new WordleDictionary(words);
    }

    private String normalizeWord(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String normalized = raw.trim().toLowerCase().replace('ё', 'е');
        if (!normalized.matches("[а-я]+")) return null;
        if (normalized.length() != WORD_LENGTH) return null;
        return normalized;
    }
}