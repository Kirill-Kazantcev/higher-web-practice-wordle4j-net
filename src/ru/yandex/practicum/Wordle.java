package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;

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
    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    public static void main(String[] args) {
        // Используем try-with-resources для автоматического закрытия ресурсов
        try (PrintWriter log = new PrintWriter(new FileWriter(LOG_FILE, true), true);
             Scanner scanner = new Scanner(System.in)) {

            log.println("=== Игра Wordle запущена ===");
            System.out.println("Добро пожаловать в игру Wordle!");
            System.out.println("У вас есть 6 попыток, чтобы угадать слово из 5 букв.");
            System.out.println("Для получения подсказки нажмите Enter.");

            // Загрузка словаря
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load(DICTIONARY_FILE);
            log.println("Словарь загружен. Количество слов: " + dictionary.getWords().size());

            // Инициализация игры
            WordleGame game = new WordleGame(dictionary);
            runGame(game, scanner, log);

            log.println("=== Игра Wordle завершена ===");
        } catch (IOException e) {
            System.err.println("Не удалось создать лог-файл. Подробности в консоли.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Произошла непредвиденная ошибка. Подробности в логе.");
            e.printStackTrace();
        }
    }

    private static void runGame(WordleGame game, Scanner scanner, PrintWriter log) {
        while (!game.isFinished()) {
            System.out.println("\nОсталось попыток: " + game.getStepsLeft());
            System.out.print("Введите слово: ");
            String input = scanner.nextLine().trim().toLowerCase().replace('ё', 'е');

            if (input.isEmpty()) {
                // Запрос подсказки
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
                    log.println("Игрок победил за " + (WordleGame.MAX_STEPS - game.getStepsLeft()) + " ходов.");
                }
            } catch (GameException e) {
                System.out.println("Ошибка: " + e.getMessage());
                log.println("Ошибка ввода: " + e.getMessage() + " (" + input + ")");
            }
        }

        if (!game.isWin()) {
            System.out.println("\nВы проиграли. Загаданное слово: " + game.getAnswer());
            log.println("Игрок проиграл. Загаданное слово: " + game.getAnswer());
        }
        finishGame(game, log);
    }

    private static void finishGame(WordleGame game, PrintWriter log) {
        log.println("Игра завершена. Победа: " + game.isWin());
        if (game.wasHintUsed()) {
            log.println("Игрок использовал подсказку.");
        }
        log.println("История ходов: " + game.getGuesses());
        log.println("Подсказки: " + game.getHints());
    }
}