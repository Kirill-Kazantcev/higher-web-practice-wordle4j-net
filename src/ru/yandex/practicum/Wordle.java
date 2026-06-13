package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.game.WordleWordMatcher;
import ru.yandex.practicum.net.HttpStatisticsClient;
import ru.yandex.practicum.net.StatisticsClient;
import ru.yandex.practicum.util.ConfigLoader;
import ru.yandex.practicum.util.JsonUtils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Wordle {
    private static final Logger LOGGER = Logger.getLogger(Wordle.class.getName());
    private static PrintWriter log;
    private static StatisticsClient statsClient;
    private static String lastNickname = null;

    public static void main(String[] args) {
        try {
            ConfigLoader config = new ConfigLoader("application.properties");
            String dictFile = config.getString("dictionary.path");
            String serverUrl = config.getString("server.url");
            String logFile = config.getString("log.file");

            log = new PrintWriter(new FileWriter(logFile, true), true);
            log.println("=== Игра Wordle запущена ===");

            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load(dictFile);
            log.println("Словарь загружен. Количество слов: " + dictionary.size());

            WordleWordMatcher matcher = new WordleWordMatcher();
            statsClient = new HttpStatisticsClient(serverUrl);

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
            if (log != null) {
                log.println("Критическая ошибка: " + e.getMessage());
            }
            LOGGER.log(Level.SEVERE, "Критическая ошибка", e);
        }
    }

    private static void runGame(WordleGame game, Scanner scanner) {
        while (!game.isFinished()) {
            System.out.println("\nОсталось попыток: " + game.getStepsLeft());
            System.out.print("Введите слово (для подсказки нажмите Enter): ");
            String input = scanner.nextLine().trim().toLowerCase().replace('ё', 'е');

            if (input.isEmpty()) {
                try {
                    System.out.println("Подсказка: " + game.getHintWord());
                    log.println("Игрок запросил подсказку.");
                } catch (GameException e) {
                    System.out.println("Не удалось получить подсказку: " + e.getMessage());
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
                    log.println("Игрок победил за " + game.getStepsUsed() + " ходов, подсказок: " + game.getHintsUsed());
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
            log.println("Игрок проиграл, подсказок: " + game.getHintsUsed());
            sendGameResult(game, false, scanner);
        }
        finishGame(game);
        System.out.println("\nВозврат в главное меню.");
    }

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

        if (statsClient.sendGameResult(nickname, win, game.getStepsUsed(), game.getHintsUsed())) {
            System.out.println("Статистика сохранена!");
            log.println("Статистика отправлена для " + nickname);
            String topJson = statsClient.fetchTopPlayers();
            if (topJson != null) printTopPlayers(topJson);
        } else {
            System.out.println("Не удалось сохранить статистику.");
            log.println("Ошибка отправки статистики.");
        }
    }

    private static void printTopPlayers(String json) {
        List<JsonUtils.TopPlayer> top = JsonUtils.parseTopPlayers(json);
        if (top.isEmpty()) {
            System.out.println("Нет данных для отображения");
            return;
        }
        int rank = 1;
        for (JsonUtils.TopPlayer p : top) {
            System.out.printf("%d. %s - %d побед (%.1f%%)%n", rank++, p.nickname, p.wins, p.winRate);
        }
    }

    private static void printPlayerStats(String json) {
        JsonUtils.PlayerStats ps = JsonUtils.parsePlayerStats(json);
        if (ps == null) {
            System.out.println("Не удалось разобрать статистику.");
            return;
        }
        System.out.printf("Игрок: %s%nПобед: %d%nПоражений: %d%nПодсказок: %d%nСреднее число ходов: %.2f%nПроцент побед: %.1f%%%n",
                ps.nickname, ps.wins, ps.losses, ps.hintsUsed, ps.avgSteps, ps.winRate);
    }

    private static void finishGame(WordleGame game) {
        log.println("Игра завершена. Победа: " + game.isWin());
        if (game.getHintsUsed() > 0) log.println("Использовано подсказок: " + game.getHintsUsed());
        log.println("История ходов: " + game.getGuesses());
        log.println("Подсказки: " + game.getHints());
    }
}