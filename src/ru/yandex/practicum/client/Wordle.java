package ru.yandex.practicum.client;

import ru.yandex.practicum.exception.game.GameException;
import ru.yandex.practicum.exception.io.DictionaryEmptyException;
import ru.yandex.practicum.exception.io.DictionaryFileNotFoundException;
import ru.yandex.practicum.game.WordleWordMatcher;
import ru.yandex.practicum.net.HttpStatisticsClient;
import ru.yandex.practicum.util.JsonUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class Wordle {
    private static PrintWriter log;
    private static HttpStatisticsClient httpClient;
    private static String lastNickname = null;

    public static void main(String[] args) {
        try {
            log = new PrintWriter(new FileWriter(WordleConstants.LOG_FILE, true), true);
            log.println("=== Игра Wordle запущена ===");

            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load(WordleConstants.DICTIONARY_FILE);
            log.println("Словарь загружен. Количество слов: " + dictionary.size());

            WordleWordMatcher matcher = new WordleWordMatcher();
            httpClient = new HttpStatisticsClient();

            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            System.out.println("Добро пожаловать в игру Wordle!");
            System.out.println("Сервер статистики: " + WordleConstants.SERVER_HOST + ":" + WordleConstants.SERVER_PORT);
            System.out.println("Угадайте слово из 5 букв. У вас 6 попыток.");
            System.out.println("Пустой ввод — подсказка.");

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
                        String topJson = httpClient.fetchTopPlayers();
                        if (topJson != null) {
                            printTopPlayers(topJson);
                        } else {
                            System.out.println("Не удалось получить топ игроков.");
                        }
                        break;
                    case "3":
                        System.out.print("Введите никнейм для просмотра статистики: ");
                        String statsNickname = scanner.nextLine().trim();
                        if (statsNickname.isBlank()) {
                            System.out.println("Никнейм не указан.");
                            break;
                        }
                        String statsJson = httpClient.fetchPlayerStats(statsNickname);
                        if (statsJson != null) {
                            printPlayerStats(statsJson);
                        } else {
                            System.out.println("Не удалось получить статистику для игрока: " + statsNickname);
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
            log.close();

        } catch (DictionaryFileNotFoundException e) {
            System.err.println("Ошибка: файл словаря не найден - " + e.getMessage());
            if (log != null) {
                log.println("Ошибка: файл словаря не найден - " + e.getMessage());
            }
        } catch (DictionaryEmptyException e) {
            System.err.println("Ошибка: словарь пуст - " + e.getMessage());
            if (log != null) {
                log.println("Ошибка: словарь пуст - " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
            if (log != null) {
                log.println("Ошибка ввода-вывода: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            if (log != null) {
                log.println("Критическая ошибка: " + e.getMessage());
                e.printStackTrace(log);
            }
        }
    }

    private static void runGame(WordleGame game, Scanner scanner) {
        while (!game.isFinished()) {
            System.out.println("\nОсталось попыток: " + game.getStepsLeft());
            System.out.print("Введите слово (для подсказки нажмите Enter): ");
            String input = scanner.nextLine().trim().toLowerCase().replace('ё', 'е');

            if (input.isEmpty()) {
                try {
                    String hint = game.getHintWord();
                    System.out.println("Подсказка: " + hint);
                    log.println("Игрок запросил подсказку: " + hint);
                } catch (GameException e) {
                    System.out.println("Не удалось получить подсказку: " + e.getMessage());
                    log.println("Ошибка получения подсказки: " + e.getMessage());
                }
                continue;
            }

            try {
                String hint = game.makeMove(input);
                System.out.println("Результат: " + hint);
                log.println("Ход: " + input + " -> " + hint);

                if (game.isWin()) {
                    System.out.println("\nПоздравляем! Вы угадали слово \"" + game.getAnswer() + "\"!");
                    System.out.println("Вы использовали " + game.getStepsUsed() + " попыток и " + game.getHintsUsed() + " подсказок.");
                    log.println("Игрок победил за " + game.getStepsUsed() + " ходов, подсказок: " + game.getHintsUsed());
                    sendGameResult(game, scanner);
                    break;
                }
            } catch (GameException e) {
                System.out.println("Ошибка: " + e.getMessage());
                log.println("Ошибка ввода: " + e.getMessage());
            }
        }

        if (!game.isWin()) {
            System.out.println("\nВы проиграли. Загаданное слово: " + game.getAnswer());
            System.out.println("Вы использовали " + game.getStepsUsed() + " попыток и " + game.getHintsUsed() + " подсказок.");
            log.println("Игрок проиграл, подсказок: " + game.getHintsUsed());
        }

        log.println("Игра завершена. Победа: " + game.isWin());
        System.out.println("\nВозврат в главное меню.");
    }

    private static void sendGameResult(WordleGame game, Scanner scanner) {
        String nickname = lastNickname;
        if (nickname == null) {
            System.out.print("Введите ваш никнейм для сохранения статистики: ");
            nickname = scanner.nextLine().trim();
            if (nickname.isBlank()) {
                nickname = "Аноним";
            }
            lastNickname = nickname;
        } else {
            System.out.print("Сохранить статистику для " + nickname + "? (д/н): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("д") && !confirm.equals("да") && !confirm.equals("y") && !confirm.equals("yes")) {
                System.out.print("Введите другой никнейм: ");
                nickname = scanner.nextLine().trim();
                if (nickname.isBlank()) {
                    nickname = "Аноним";
                }
                lastNickname = nickname;
            }
        }

        boolean success = httpClient.sendGameResult(nickname, true, game.getStepsUsed(), game.getHintsUsed());
        if (success) {
            System.out.println("Статистика сохранена!");
            log.println("Статистика отправлена для " + nickname);

            String topJson = httpClient.fetchTopPlayers();
            if (topJson != null) {
                printTopPlayers(topJson);
            }
        } else {
            System.out.println("Не удалось сохранить статистику.");
            log.println("Ошибка отправки статистики для " + nickname);
        }
    }

    private static void printTopPlayers(String json) {
        List<JsonUtils.TopPlayerEntry> top = JsonUtils.parseTopResponse(json);
        if (top.isEmpty()) {
            System.out.println("Нет данных для отображения");
            return;
        }
        System.out.println("\n=== ТОП ИГРОКОВ ===");
        for (JsonUtils.TopPlayerEntry entry : top) {
            String winsWord = entry.wins() == 1 ? "победа" : "побед";
            System.out.printf("%d. %s - %d %s%n", entry.rank(), entry.nickname(), entry.wins(), winsWord);
        }
    }

    private static void printPlayerStats(String json) {
        JsonUtils.PlayerStats stats = JsonUtils.parsePlayerStats(json);
        if (stats == null) {
            System.out.println("Не удалось разобрать статистику.");
            return;
        }

        System.out.println("\n=== СТАТИСТИКА ИГРОКА ===");
        System.out.printf("Игрок: %s%n", stats.nickname());
        System.out.printf("Побед: %d%n", stats.wins());
        System.out.printf("Поражений: %d%n", stats.losses());
        System.out.printf("Всего игр: %d%n", stats.wins() + stats.losses());
        System.out.printf("Использовано подсказок: %d%n", stats.hintsUsed());
        System.out.printf("Среднее число ходов: %.2f%n", stats.avgSteps());
        System.out.printf("Процент побед: %.1f%%%n", stats.winRate());
        System.out.println("=========================");
    }
}