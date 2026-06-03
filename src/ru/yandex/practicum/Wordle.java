package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

/**
 * Главный класс игры Wordle.
 * <p>
 * Отвечает за инициализацию, взаимодействие с пользователем,
 * логирование и запуск игрового цикла.
 * </p>
 */
public class Wordle {
    /** Путь к файлу словаря */
    private static final String DICTIONARY_FILE = "words_ru.txt";
    /** Путь к файлу лога */
    private static final String LOG_FILE = "wordle.log";
    /** URL сервера статистики */
    private static final String SERVER_URL = "http://localhost:8081";
    /** HTTP клиент для отправки запросов к серверу статистики */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    /** Последний использованный никнейм (для автоматической подстановки) */
    private static String lastNickname = null;

    /**
     * Точка входа в программу. Загружает словарь, запускает игровой цикл.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(new FileWriter(LOG_FILE, true), true);
             Scanner scanner = new Scanner(System.in)) {

            log.println("=== Игра Wordle запущена ===");
            System.out.println("Добро пожаловать в игру Wordle!");
            System.out.println("У вас есть 6 попыток, чтобы угадать слово из 5 букв.");
            System.out.println("Для получения подсказки нажмите Enter.");
            System.out.println("Доступные команды: /top, /stats, /exit");

            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load(DICTIONARY_FILE);
            log.println("Словарь загружен. Количество слов: " + dictionary.getWords().size());

            boolean playing = true;
            while (playing) {
                WordleGame game = new WordleGame(dictionary);
                runGame(game, scanner, log);
                System.out.print("\nХотите сыграть ещё? (д/н): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                if (!answer.equals("д") && !answer.equals("да") && !answer.equals("y") && !answer.equals("yes")) {
                    playing = false;
                }
            }

            log.println("=== Игра Wordle завершена ===");
            System.out.println("Спасибо за игру!");
        } catch (IOException e) {
            System.err.println("Не удалось создать лог-файл. Подробности в консоли.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Произошла непредвиденная ошибка. Подробности в логе.");
            e.printStackTrace();
        }
    }

    /**
     * Запускает игровой цикл для одной игры.
     *
     * @param game    текущая игра
     * @param scanner сканнер для ввода с клавиатуры
     * @param log     логгер для записи событий
     */
    private static void runGame(WordleGame game, Scanner scanner, PrintWriter log) {
        while (!game.isFinished()) {
            System.out.println("\nОсталось попыток: " + game.getStepsLeft());
            System.out.print("Введите слово (или /top, /stats, /exit): ");
            String input = scanner.nextLine().trim().toLowerCase().replace('ё', 'е');

            if (input.equals("/exit")) {
                System.out.println("Выход из игры. До свидания!");
                log.println("Игрок завершил игру досрочно.");
                System.exit(0);
            }

            if (input.startsWith("/")) {
                handleCommand(input, scanner, log);
                continue;
            }

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
                    log.println("Игрок победил за " + game.getStepsUsed() + " ходов.");
                    sendGameResult(game, true, scanner, log);
                }
            } catch (GameException e) {
                System.out.println("Ошибка: " + e.getMessage());
                log.println("Ошибка ввода: " + e.getMessage() + " (" + input + ")");
            }
        }

        if (!game.isWin()) {
            System.out.println("\nВы проиграли. Загаданное слово: " + game.getAnswer());
            log.println("Игрок проиграл.");
            sendGameResult(game, false, scanner, log);
        }
        finishGame(game, log);
    }

    /**
     * Отправляет результат игры на сервер статистики.
     *
     * @param game    текущая игра
     * @param win     флаг победы (true - победа, false - поражение)
     * @param scanner сканнер для ввода никнейма
     * @param log     логгер для записи событий
     */
    private static void sendGameResult(WordleGame game, boolean win, Scanner scanner, PrintWriter log) {
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

        int stepsUsed = game.getStepsUsed();
        int hintsUsed = game.getHintsUsed();
        String json = String.format("{\"nickname\": \"%s\", \"win\": %b, \"steps\": %d, \"hintsUsed\": %d}",
                escapeJson(nickname), win, stepsUsed, hintsUsed);

        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/result"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> postResponse = HTTP_CLIENT.send(postRequest, HttpResponse.BodyHandlers.ofString());
            if (postResponse.statusCode() == 200) {
                System.out.println("Статистика сохранена!");
                log.println("Статистика отправлена для " + nickname + " (win=" + win + ", steps=" + stepsUsed + ", hintsUsed=" + hintsUsed + ")");
                requestTopPlayers(log);
            } else {
                System.out.println("Не удалось сохранить статистику (код " + postResponse.statusCode() + ")");
                log.println("Ошибка отправки статистики: " + postResponse.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Ошибка связи с сервером: " + e.getMessage());
            log.println("Сетевая ошибка: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает введённые команды пользователя.
     *
     * @param cmd     введённая команда (начинается с '/')
     * @param scanner сканнер для ввода параметров команды
     * @param log     логгер для записи событий
     */
    private static void handleCommand(String cmd, Scanner scanner, PrintWriter log) {
        if (cmd.equals("/top")) {
            requestTopPlayers(log);
        } else if (cmd.equals("/stats")) {
            System.out.print("Введите никнейм для статистики: ");
            String nickname = scanner.nextLine().trim();
            if (nickname.isBlank()) nickname = "Аноним";
            requestPlayerStats(nickname, log);
        } else {
            System.out.println("Неизвестная команда. Доступны: /top, /stats, /exit");
        }
    }

    /**
     * Запрашивает у сервера топ-10 игроков и выводит результат в консоль.
     *
     * @param log логгер для записи событий
     */
    private static void requestTopPlayers(PrintWriter log) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/top"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                System.out.println("\n=== Топ-10 игроков ===");
                printTopPlayers(body);
                log.println("Топ игроков получен");
            } else {
                System.out.println("Не удалось получить топ игроков (код " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            System.out.println("Ошибка связи с сервером: " + e.getMessage());
            log.println("Ошибка получения топа: " + e.getMessage());
        }
    }

    /**
     * Запрашивает у сервера статистику конкретного игрока и выводит результат в консоль.
     *
     * @param nickname никнейм игрока
     * @param log      логгер для записи событий
     */
    private static void requestPlayerStats(String nickname, PrintWriter log) {
        try {
            String url = SERVER_URL + "/stats?nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("\n=== Статистика игрока ===");
                printPlayerStats(response.body());
                log.println("Статистика для " + nickname + " получена");
            } else {
                System.out.println("Не удалось получить статистику (код " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            System.out.println("Ошибка связи с сервером: " + e.getMessage());
            log.println("Ошибка получения статистики: " + e.getMessage());
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
     * Записывает в лог финальную информацию об игре.
     *
     * @param game завершённая игра
     * @param log  логгер
     */
    private static void finishGame(WordleGame game, PrintWriter log) {
        log.println("Игра завершена. Победа: " + game.isWin());
        if (game.getHintsUsed() > 0) {
            log.println("Игрок использовал подсказок: " + game.getHintsUsed());
        }
        log.println("История ходов: " + game.getGuesses());
        log.println("Подсказки: " + game.getHints());
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
            e.printStackTrace();
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
     * Экранирует специальные символы для использования в JSON.
     *
     * @param s исходная строка
     * @return экранированная строка
     */
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}