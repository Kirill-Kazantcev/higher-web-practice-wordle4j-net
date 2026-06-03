package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс, управляющий состоянием и логикой игры Wordle.
 * <p>
 * Хранит загаданное слово, количество оставшихся попыток,
 * историю введённых слов и полученных подсказок, а также
 * количество использованных подсказок.
 * </p>
 * <p>
 * Игрок имеет 6 попыток, чтобы угадать слово из 5 букв.
 * После каждого хода вычисляется подсказка с символами:
 * '+' — буква на правильной позиции,
 * '^' — буква есть в слове, но на другой позиции,
 * '-' — буквы нет в загаданном слове.
 * </p>
 */
public class WordleGame {
    /** Максимальное количество попыток за игру */
    public static final int MAX_STEPS = 6;

    /** Словарь допустимых слов */
    private final WordleDictionary dictionary;
    /** Загаданное слово (ответ) */
    private final String answer;
    /** Количество оставшихся попыток */
    private int stepsLeft;
    /** Список введённых слов-догадок */
    private final List<String> guesses = new ArrayList<>();
    /** Список полученных подсказок для каждой догадки */
    private final List<String> hints = new ArrayList<>();
    /** Счётчик использованных подсказок за текущую игру */
    private int hintsUsed;

    /**
     * Конструктор для новой игры со случайным словом из словаря.
     *
     * @param dictionary словарь, из которого выбирается загаданное слово
     */
    public WordleGame(WordleDictionary dictionary) {
        this(dictionary, chooseRandomAnswer(dictionary));
    }

    /**
     * Конструктор для тестирования, позволяющий задать конкретное загаданное слово.
     *
     * @param dictionary словарь для проверки допустимости слов
     * @param answer     загаданное слово (должно быть в словаре)
     */
    WordleGame(WordleDictionary dictionary, String answer) {
        this.dictionary = dictionary;
        this.answer = answer;
        this.stepsLeft = MAX_STEPS;
        this.hintsUsed = 0;
    }

    /**
     * Выбирает случайное слово из словаря.
     *
     * @param dictionary словарь
     * @return случайное слово
     */
    private static String chooseRandomAnswer(WordleDictionary dictionary) {
        List<String> words = dictionary.getWords();
        return words.get(new Random().nextInt(words.size()));
    }

    /**
     * Обрабатывает ход игрока.
     *
     * @param word слово, введённое игроком
     * @return строка-подсказка из символов +, ^, -
     * @throws InvalidWordFormatException      если слово имеет неверную длину (не 5 букв)
     * @throws WordNotFoundInDictionaryException если слово отсутствует в словаре
     * @throws IllegalStateException           если игра уже завершена (победа или конец попыток)
     */
    public String makeMove(String word) throws InvalidWordFormatException, WordNotFoundInDictionaryException {
        if (isFinished()) {
            throw new IllegalStateException("Игра уже завершена.");
        }
        dictionary.validateWord(word);

        String hint = WordleDictionary.analyze(word, answer);
        guesses.add(word);
        hints.add(hint);
        stepsLeft--;

        return hint;
    }

    /**
     * Генерирует подсказку — подходящее слово из словаря на основе истории ходов.
     * <p>
     * Метод увеличивает счётчик использованных подсказок и находит первое слово
     * из словаря, которое соответствует всем ранее введённым словам и их подсказкам.
     * </p>
     *
     * @return слово-подсказка
     * @throws GameException если не найдено ни одного подходящего слова
     */
    public String getHintWord() throws GameException {
        hintsUsed++;
        List<String> candidates = getPossibleWords();
        if (candidates.isEmpty()) {
            throw new GameException("Нет подходящих слов для подсказки.");
        }
        return candidates.getFirst();
    }

    /**
     * Возвращает список слов из словаря, которые соответствуют всем ранее сделанным ходам.
     * <p>
     * Для каждого кандидата проверяется, что подсказки для всех предыдущих догадок
     * совпадают с подсказками, которые были бы получены, если бы кандидат был ответом.
     * </p>
     *
     * @return список возможных слов (кандидатов)
     */
    public List<String> getPossibleWords() {
        List<String> candidates = new ArrayList<>();
        outer:
        for (String candidate : dictionary.getWords()) {
            for (int i = 0; i < guesses.size(); i++) {
                String expectedHint = hints.get(i);
                String actualHint = WordleDictionary.analyze(guesses.get(i), candidate);
                if (!expectedHint.equals(actualHint)) {
                    continue outer;
                }
            }
            candidates.add(candidate);
        }
        return candidates;
    }

    /**
     * Проверяет, выиграна ли игра.
     *
     * @return true, если последняя догадка совпадает с загаданным словом, иначе false
     */
    public boolean isWin() {
        return !guesses.isEmpty() && guesses.getLast().equals(answer);
    }

    /**
     * Проверяет, завершена ли игра.
     * <p>
     * Игра считается завершённой при победе или когда закончились попытки.
     * </p>
     *
     * @return true, если игра завершена, иначе false
     */
    public boolean isFinished() {
        return isWin() || stepsLeft <= 0;
    }

    /**
     * Возвращает количество сделанных ходов в текущей игре.
     *
     * @return количество догадок
     */
    public int getStepsUsed() {
        return guesses.size();
    }

    /**
     * Возвращает количество использованных подсказок в текущей игре.
     *
     * @return количество подсказок
     */
    public int getHintsUsed() {
        return hintsUsed;
    }

    /**
     * Возвращает количество оставшихся попыток.
     *
     * @return количество оставшихся попыток
     */
    public int getStepsLeft() {
        return stepsLeft;
    }

    /**
     * Возвращает загаданное слово.
     *
     * @return загаданное слово
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Возвращает неизменяемую копию списка всех догадок.
     *
     * @return список догадок
     */
    public List<String> getGuesses() {
        return List.copyOf(guesses);
    }

    /**
     * Возвращает неизменяемую копию списка всех подсказок.
     *
     * @return список подсказок
     */
    public List<String> getHints() {
        return List.copyOf(hints);
    }
}