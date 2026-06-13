package ru.yandex.practicum;

import ru.yandex.practicum.exception.SystemException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WordleServerStatisticLoader {
    private final String filePath;

    public WordleServerStatisticLoader(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, PlayerStats> load() {
        Map<String, PlayerStats> stats = new HashMap<>();
        File file = new File(filePath);
        if (!file.exists()) return stats;
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(":");
                if (parts.length == 5) {
                    String name = parts[0].trim();
                    int wins = Integer.parseInt(parts[1].trim());
                    int losses = Integer.parseInt(parts[2].trim());
                    int hints = Integer.parseInt(parts[3].trim());
                    int totalSteps = Integer.parseInt(parts[4].trim());
                    stats.put(name, new PlayerStats(wins, losses, hints, totalSteps));
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new SystemException("Ошибка чтения файла статистики: " + filePath, e);
        }
        return stats;
    }

    public void save(Map<String, PlayerStats> stats) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            for (Map.Entry<String, PlayerStats> entry : stats.entrySet()) {
                PlayerStats ps = entry.getValue();
                writer.printf("%s:%d:%d:%d:%d%n",
                        entry.getKey(), ps.wins, ps.losses, ps.hintsUsed, ps.totalSteps);
            }
        } catch (IOException e) {
            throw new SystemException("Ошибка записи файла статистики: " + filePath, e);
        }
    }

    public static class PlayerStats {
        public int wins;
        public int losses;
        public int hintsUsed;
        public int totalSteps;

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
}