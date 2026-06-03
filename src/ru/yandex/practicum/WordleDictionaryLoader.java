package ru.yandex.practicum;

import ru.yandex.practicum.exception.SystemException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для загрузки словаря из текстового файла.
 * <p>
 * Загрузчик читает файл построчно, нормализует каждое слово
 * (приводит к нижнему регистру, заменяет 'ё' на 'е') и отбирает
 * только слова, состоящие ровно из 5 букв.
 * </p>
 */
public class WordleDictionaryLoader {

    /**
     * Загружает словарь из файла по указанному пути.
     *
     * @param filePath путь к файлу словаря.
     * @return загруженный и отфильтрованный словарь.
     * @throws SystemException если файл не найден или произошла ошибка ввода-вывода.
     */
    public WordleDictionary load(String filePath) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = normalizeWord(line);
                if (normalized != null && normalized.length() == 5) {
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

    /**
     * Приводит слово к нормализованной форме для игры.
     *
     * @param raw слово, прочитанное из файла.
     * @return нормализованное слово, или null, если входная строка пуста.
     */
    private String normalizeWord(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase();
        normalized = normalized.replace('ё', 'е');
        // Дополнительно можно проверить, что слово состоит только из букв
        if (!normalized.matches("[а-я]+")) {
            return null;
        }
        return normalized;
    }
}