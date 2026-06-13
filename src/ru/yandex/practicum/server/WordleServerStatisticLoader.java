package ru.yandex.practicum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.yandex.practicum.exception.system.SystemException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WordleServerStatisticLoader {
    private final String filePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public WordleServerStatisticLoader(String filePath) {
        this.filePath = filePath;
    }

    public static class PlayerStats {
        public int wins;
        public int losses;
        public int hintsUsed;
        public int totalSteps;

        @SuppressWarnings("unused")
        public PlayerStats() {}

        public PlayerStats(int wins, int losses, int hintsUsed, int totalSteps) {
            this.wins = wins;
            this.losses = losses;
            this.hintsUsed = hintsUsed;
            this.totalSteps = totalSteps;
        }

        public double getWinRate() {
            int total = wins + losses;
            return total == 0 ? 0 : (double) wins / total * 100;
        }

        public double getAvgSteps() {
            return wins == 0 ? 0 : (double) totalSteps / wins;
        }
    }

    public Map<String, PlayerStats> load() {
        File file = new File(filePath);
        if (!file.exists()) return new HashMap<>();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            TypeToken<Map<String, PlayerStats>> typeToken = new TypeToken<>() {};
            Map<String, PlayerStats> loaded = gson.fromJson(reader, typeToken.getType());
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            throw new SystemException("Ошибка чтения статистики: " + filePath, e);
        }
    }

    public void save(Map<String, PlayerStats> stats) {
        try (Writer writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            gson.toJson(stats, writer);
        } catch (IOException e) {
            throw new SystemException("Ошибка записи статистики: " + filePath, e);
        }
    }
}