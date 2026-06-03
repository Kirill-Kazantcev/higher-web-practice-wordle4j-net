package ru.yandex.practicum;

import ru.yandex.practicum.exception.SystemException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Загрузчик и сохранятель статистики побед/поражений игроков.
 * <p>
 * Данные сохраняются в файл {@code stats.txt} в формате:
 * {@code nickname:wins:losses:hintsUsed:totalSteps}
 * </p>
 */
public class WordleServerStatisticLoader {
    /** Путь к файлу со статистикой */
    private final String filePath;

    /**
     * Конструктор загрузчика статистики.
     *
     * @param filePath путь к файлу для хранения статистики
     */
    public WordleServerStatisticLoader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Загружает статистику из файла.
     * <p>
     * Если файл не существует, возвращается пустая карта.
     * Каждая строка файла должна содержать 5 полей, разделённых двоеточием.
     * </p>
     *
     * @return карта никнейм → статистика игрока
     * @throws SystemException при ошибке ввода-вывода или повреждении файла (неверный формат)
     */
    public Map<String, PlayerStats> load() {
        Map<String, PlayerStats> stats = new HashMap<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return stats;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
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

    /**
     * Сохраняет статистику в файл.
     * <p>
     * Каждая запись сохраняется в отдельной строке в формате:
     * {@code никнейм:победы:поражения:подсказки:сумма_ходов}
     * </p>
     *
     * @param stats карта статистики (никнейм → статистика игрока)
     * @throws SystemException при ошибке записи в файл
     */
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

    /**
     * Внутренний класс, представляющий статистику одного игрока.
     * <p>
     * Хранит количество побед, поражений, использованных подсказок
     * и суммарное количество ходов в победных играх.
     * </p>
     */
    public static class PlayerStats {
        /** Количество побед */
        public int wins;
        /** Количество поражений */
        public int losses;
        /** Общее количество использованных подсказок */
        public int hintsUsed;
        /** Суммарное количество ходов в победных играх */
        public int totalSteps;

        /**
         * Конструктор статистики игрока.
         *
         * @param wins       количество побед
         * @param losses     количество поражений
         * @param hintsUsed  количество использованных подсказок
         * @param totalSteps суммарное количество ходов в победных играх
         */
        public PlayerStats(int wins, int losses, int hintsUsed, int totalSteps) {
            this.wins = wins;
            this.losses = losses;
            this.hintsUsed = hintsUsed;
            this.totalSteps = totalSteps;
        }

        /**
         * Возвращает процент побед.
         * <p>
         * Вычисляется по формуле: {@code wins / (wins + losses) * 100}
         * Если игрок не сыграл ни одной игры, возвращается 0.
         * </p>
         *
         * @return процент побед (от 0 до 100)
         */
        public double getWinRate() {
            int total = wins + losses;
            return total == 0 ? 0 : (double) wins / total * 100;
        }

        /**
         * Возвращает среднее количество ходов на победу.
         * <p>
         * Вычисляется по формуле: {@code totalSteps / wins}
         * Если у игрока нет побед, возвращается 0.
         * </p>
         *
         * @return среднее количество ходов на победу, либо 0 если побед нет
         */
        public double getAvgSteps() {
            return wins == 0 ? 0 : (double) totalSteps / wins;
        }
    }
}