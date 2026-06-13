package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.game.WordleWordMatcher;
import ru.yandex.practicum.net.WordleStatisticsClient;
import ru.yandex.practicum.util.ConfigLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Главный класс игры Wordle.
 * <p>
 * Отвечает за инициализацию, взаимодействие с пользователем,
 * логирование и запуск игрового цикла.
 * </p>
 */
public class Wordle {
    private static PrintWriter log;
    private static WordleStatisticsClient statsClient;
    private static String lastNickname = null;

    /**
     * Точка входа в программу. Загружает словарь, запускает игровой цикл.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            // Загрузка конфигурации
            ConfigLoader config = new ConfigLoader("application.properties");
            String dictFile = config.getString("dictionary.path");
            String serverUrl = config.getString("server.url");
            String logFile = config.getString("log.file");

            // Настройка лога
            log = new PrintWriter(new FileWriter(logFile, true), true);
            log.println("=== Игра Wordle запущена ===");

            // Загрузка словаря
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load(dictFile);
            log.println("Словарь загружен. Количество слов: " + dictionary.size());

            // Создание зависимостей
            WordleWordMatcher matcher = new WordleWordMatcher();
            statsClient = new WordleStatisticsClient(serverUrl);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Добро пожаловать в игру Wordle!");

            boolean running = true;
            while (running) {
                System.out.println("\n--- МЕНЮ ---");
                System.out.println("1. Начать новую игру");
                System.out.println("2. Топ-10 игроков");
                System.out.println("3. Статистика игрока");
                System.out.println("4. Выход");
                System.out.print("Ваш выбор: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        WordleGame game = new WordleGame(dictionary, matcher);
                        runGame(game, scanner);
                        break;
                    case "2":
                        String topJson = statsClient.fetchTopPlayers();
                        if (topJson != null) {
                            printTopPlayers(topJson);
                        } else {
                            System.out.println("Не удалось получить топ игроков.");
                        }
                        break;
                    case "3":
                        System.out.print("Введите никнейм: ");
                        String nickname = scanner.nextLine().trim();
                        if (nickname.isBlank()) nickname = "Аноним";
                        String statsJson = statsClient.fetchPlayerStats(nickname);
                        if (statsJson != null) {
                            printPlayerStats(statsJson);
                        } else {
                            System.out.println("Не удалось получить статистику.");
                        }
                        break;
                    case "4":
                        System.out.println("До свидания!");
                        running = false;
                        break;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, введите 1, 2, 3 или 4.");
                }
            }

            log.println("=== Игра Wordle завершена ===");
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            if (log != null) log.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Запускает игровой цикл для одной игры.
     *
     * @param game    текущая игра
     * @param scanner сканнер для ввода с клавиатуры
     */
    private static void runGame(WordleGame game, Scanner scanner) {
        while (!game.isFinished()) {
            System.out.println("\nОсталось попыток: " + game.getStepsLeft());
            System.out.print("Введите слово (для подсказки нажмите Enter): ");
            String input = scanner.nextLine().trim().toLowerCase().replace('ё', 'е');

            if (input.isEmpty()) {
                try {
                    String hintWord = game.getHintWord();
                    System.out.println("Подсказка: " + hintWord);
                    log.println("Игрок запросил подсказку. Предложено: " + hintWord);
                } catch (GameException e) {
                    System.out.println("Не удалось получить подсказку: " + e.getMessage());
                    log.println("Ошибка подсказки: " + e.getMessage());
                }
                continue;
            }

            try {
                String hint = game.makeMove(input);
                System.out.println("Результат: " + hint);
                log.println("Ход: " + input + " -> " + hint + " (осталось попыток: " + game.getStepsLeft() + ")");

                if (game.isWin()) {
                    System.out.println("\nПоздравляем! Вы угадали слово \"" + game.getAnswer() + "\"!");
                    System.out.println("Вы использовали " + game.getStepsUsed() + " попыток и " + game.getHintsUsed() + " подсказок.");
                    log.println("Игрок победил за " + game.getStepsUsed() + " ходов, использовано подсказок: " + game.getHintsUsed());
                    sendGameResult(game, true, scanner);
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                log.println("Ошибка ввода: " + e.getMessage() + " (" + input + ")");
            }
        }
        if (!game.isWin()) {
            System.out.println("\nВы проиграли. Загаданное слово: " + game.getAnswer());
            System.out.println("Вы использовали " + game.getStepsUsed() + " попыток и " + game.getHintsUsed() + " подсказок.");
            log.println("Игрок проиграл, использовано подсказок: " + game.getHintsUsed());
            sendGameResult(game, false, scanner);
        }
        finishGame(game);
        System.out.println("\nВозврат в главное меню.");
    }

    /**
     * Отправляет результат игры на сервер статистики.
     *
     * @param game    текущая игра
     * @param win     флаг победы (true - победа, false - поражение)
     * @param scanner сканнер для ввода никнейма
     */
    private static void sendGameResult(WordleGame game, boolean win, Scanner scanner) {
        String nickname = lastNickname;
        if (nickname == null) {
            System.out.print("Введите ваш никнейм для сохранения статистики: ");
            nickname = scanner.nextLine().trim();
            if (nickname.isBlank()) nickname = "Аноним";
            lastNickname = nickname;
        } else {
            System.out.print("Сохранить статистику для " + nickname + "? (д/н): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("д") && !confirm.equals("да") && !confirm.equals("y") && !confirm.equals("yes")) {
                System.out.print("Введите другой никнейм: ");
                nickname = scanner.nextLine().trim();
                if (nickname.isBlank()) nickname = "Аноним";
                lastNickname = nickname;
            }
        }

        boolean success = statsClient.sendGameResult(nickname, win, game.getStepsUsed(), game.getHintsUsed());
        if (success) {
            System.out.println("Статистика сохранена!");
            log.println("Статистика отправлена для " + nickname);
            String topJson = statsClient.fetchTopPlayers();
            if (topJson != null) {
                printTopPlayers(topJson);
            }
        } else {
            System.out.println("Не удалось сохранить статистику.");
            log.println("Ошибка отправки статистики.");
        }
    }

    /**
     * Выводит в консоль топ-10 игроков из JSON-строки.
     * <p>
     * Метод парсит JSON, извлекая массив top и выводя каждого игрока
     * с порядковым номером, никнеймом, количеством побед и процентом побед.
     * </p>
     *
     * @param json JSON-строка от сервера в формате {"top": [{"nickname": "...", "wins": N, "winRate": X.X}, ...]}
     */
    private static void printTopPlayers(String json) {
        try {
            int topIdx = json.indexOf("\"top\":");
            if (topIdx == -1) {
                System.out.println("Не найден ключ 'top'");
                return;
            }

            int start = json.indexOf('[', topIdx);
            int end = json.lastIndexOf(']');
            if (start == -1 || end == -1 || start >= end) {
                System.out.println("Не найден массив top");
                return;
            }

            String content = json.substring(start + 1, end);
            if (content.trim().isEmpty()) {
                System.out.println("Топ пуст");
                return;
            }

            int rank = 1;
            int braceCount = 0;
            int lastStart = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') {
                    if (braceCount == 0) {
                        lastStart = i;
                    }
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        String obj = content.substring(lastStart, i + 1);
                        String nickname = extractJsonValue(obj, "nickname");
                        String winsStr = extractJsonValue(obj, "wins");
                        String winRateStr = extractJsonValue(obj, "winRate");

                        if (nickname != null && winsStr != null) {
                            try {
                                int wins = Integer.parseInt(winsStr);
                                double winRate = winRateStr != null ? Double.parseDouble(winRateStr) : 0.0;
                                System.out.printf("%d. %s - %d побед (%.1f%%)%n", rank, nickname, wins, winRate);
                                rank++;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }

            if (rank == 1) {
                System.out.println("Нет данных для отображения");
            }
        } catch (Exception e) {
            System.out.println("Ошибка вывода топа: " + e.getMessage());
        }
    }

    /**
     * Выводит в консоль статистику игрока из JSON-строки.
     *
     * @param json JSON-строка с данными игрока
     */
    private static void printPlayerStats(String json) {
        String nickname = extractJsonValue(json, "nickname");
        String wins = extractJsonValue(json, "wins");
        String losses = extractJsonValue(json, "losses");
        String hints = extractJsonValue(json, "hintsUsed");
        String avgSteps = extractJsonValue(json, "avgSteps");
        String winRate = extractJsonValue(json, "winRate");
        if (nickname != null && wins != null) {
            System.out.printf("Игрок: %s%nПобед: %s%nПоражений: %s%nИспользовано подсказок: %s%nСреднее число ходов: %s%nПроцент побед: %s%%%n",
                    nickname, wins, losses, hints, avgSteps, winRate);
        } else {
            System.out.println("Не удалось разобрать статистику.");
        }
    }

    /**
     * Извлекает значение поля из фрагмента JSON.
     * <p>
     * Поддерживает как строковые значения в кавычках, так и числовые значения без кавычек.
     * </p>
     *
     * @param jsonPart фрагмент JSON (например, {"nickname": "Никнейм", "wins": 20})
     * @param key      имя ключа для поиска
     * @return значение в виде строки или null, если ключ не найден
     */
    private static String extractJsonValue(String jsonPart, String key) {
        String pattern = "\"" + key + "\":";
        int idx = jsonPart.indexOf(pattern);
        if (idx == -1) return null;
        idx += pattern.length();
        while (idx < jsonPart.length() && (jsonPart.charAt(idx) == ' ' || jsonPart.charAt(idx) == '\t')) {
            idx++;
        }
        if (idx >= jsonPart.length()) return null;
        if (jsonPart.charAt(idx) == '"') {
            int startQuote = idx + 1;
            int endQuote = jsonPart.indexOf('"', startQuote);
            if (endQuote == -1) return null;
            return jsonPart.substring(startQuote, endQuote);
        } else {
            int startNum = idx;
            int endNum = startNum;
            while (endNum < jsonPart.length() && (Character.isDigit(jsonPart.charAt(endNum)) ||
                    jsonPart.charAt(endNum) == '.' || jsonPart.charAt(endNum) == '-')) {
                endNum++;
            }
            return jsonPart.substring(startNum, endNum);
        }
    }

    /**
     * Записывает в лог финальную информацию об игре.
     *
     * @param game завершённая игра
     */
    private static void finishGame(WordleGame game) {
        log.println("Игра завершена. Победа: " + game.isWin());
        if (game.getHintsUsed() > 0) {
            log.println("Игрок использовал подсказок: " + game.getHintsUsed());
        }
        log.println("История ходов: " + game.getGuesses());
        log.println("Подсказки: " + game.getHints());
    }
}