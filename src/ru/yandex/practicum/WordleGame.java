package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;
import ru.yandex.practicum.game.Dictionary;
import ru.yandex.practicum.game.WordMatcher;

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
 * После каждого хода вычисляется подсказка с помощью {@link WordMatcher}.
 * </p>
 */
public class WordleGame {
    /** Максимальное количество попыток за игру */
    public static final int MAX_STEPS = 6;

    private final Dictionary dictionary;
    private final WordMatcher matcher;
    private final String answer;
    private int stepsLeft;
    private final List<String> guesses = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private int hintsUsed;

    /**
     * Конструктор для новой игры со случайным словом из словаря.
     *
     * @param dictionary словарь, из которого выбирается загаданное слово
     * @param matcher    алгоритм сравнения слов
     */
    public WordleGame(Dictionary dictionary, WordMatcher matcher) {
        this(dictionary, matcher, chooseRandomAnswer(dictionary));
    }

    /**
     * Конструктор для тестирования, позволяющий задать конкретное загаданное слово.
     *
     * @param dictionary словарь для проверки допустимости слов
     * @param matcher    алгоритм сравнения слов
     * @param answer     загаданное слово (должно быть в словаре)
     */
    public WordleGame(Dictionary dictionary, WordMatcher matcher, String answer) {
        this.dictionary = dictionary;
        this.matcher = matcher;
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
    private static String chooseRandomAnswer(Dictionary dictionary) {
        List<String> words = dictionary.getAllWords();
        return words.get(new Random().nextInt(words.size()));
    }

    /**
     * Обрабатывает ход игрока.
     *
     * @param word слово, введённое игроком
     * @return строка-подсказка из символов +, ^, -
     * @throws InvalidWordFormatException      если слово имеет неверную длину (не 5 букв)
     * @throws WordNotFoundInDictionaryException если слово отсутствует в словаре
     * @throws IllegalStateException           если игра уже завершена
     */
    public String makeMove(String word) throws InvalidWordFormatException, WordNotFoundInDictionaryException {
        if (isFinished()) {
            throw new IllegalStateException("Игра уже завершена.");
        }
        // Валидация: если словарь не умеет валидировать, делаем сами
        if (!(dictionary instanceof WordleDictionary)) {
            if (word.length() != 5) throw new InvalidWordFormatException("Слово должно быть из 5 букв");
            if (!dictionary.contains(word)) throw new WordNotFoundInDictionaryException(word);
        } else {
            ((WordleDictionary) dictionary).validateWord(word);
        }

        String hint = matcher.match(word, answer);
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
        return candidates.get(0);
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
        for (String candidate : dictionary.getAllWords()) {
            for (int i = 0; i < guesses.size(); i++) {
                String expectedHint = hints.get(i);
                String actualHint = matcher.match(guesses.get(i), candidate);
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
    public int getStepsUsed() { return guesses.size(); }

    /**
     * Возвращает количество использованных подсказок в текущей игре.
     *
     * @return количество подсказок
     */
    public int getHintsUsed() { return hintsUsed; }

    /**
     * Возвращает количество оставшихся попыток.
     *
     * @return количество оставшихся попыток
     */
    public int getStepsLeft() { return stepsLeft; }

    /**
     * Возвращает загаданное слово.
     *
     * @return загаданное слово
     */
    public String getAnswer() { return answer; }

    /**
     * Возвращает неизменяемую копию списка всех догадок.
     *
     * @return список догадок
     */
    public List<String> getGuesses() { return List.copyOf(guesses); }

    /**
     * Возвращает неизменяемую копию списка всех подсказок.
     *
     * @return список подсказок
     */
    public List<String> getHints() { return List.copyOf(hints); }
}