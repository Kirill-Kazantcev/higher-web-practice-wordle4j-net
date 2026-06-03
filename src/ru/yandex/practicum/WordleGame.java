package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс, управляющий состоянием и логикой игры.
 * <p>
 * Хранит загаданное слово, количество оставшихся попыток,
 * историю введённых слов и полученных подсказок.
 * </p>
 */
public class WordleGame {
    public static final int MAX_STEPS = 6;

    private final WordleDictionary dictionary;
    private final String answer;
    private int stepsLeft;
    private final List<String> guesses = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private boolean hintWasUsed;

    /**
     * Конструктор для новой игры со случайным словом.
     *
     * @param dictionary словарь, из которого выбирается слово.
     */
    public WordleGame(WordleDictionary dictionary) {
        this(dictionary, chooseRandomAnswer(dictionary));
    }

    /**
     * Конструктор для тестирования, позволяющий задать конкретное слово.
     *
     * @param dictionary словарь.
     * @param answer     загаданное слово.
     */
    WordleGame(WordleDictionary dictionary, String answer) {
        this.dictionary = dictionary;
        this.answer = answer;
        this.stepsLeft = MAX_STEPS;
        this.hintWasUsed = false;
    }

    private static String chooseRandomAnswer(WordleDictionary dictionary) {
        List<String> words = dictionary.getWords();
        return words.get(new Random().nextInt(words.size()));
    }

    /**
     * Обрабатывает ход игрока.
     *
     * @param word слово, введённое игроком.
     * @return строка-подсказка для этого слова.
     * @throws InvalidWordFormatException      если слово имеет неверный формат.
     * @throws WordNotFoundInDictionaryException если слово отсутствует в словаре.
     * @throws IllegalStateException           если игра уже завершена.
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
     *
     * @return слово-подсказка.
     * @throws GameException если не найдено ни одного подходящего слова.
     */
    public String getHintWord() throws GameException {
        hintWasUsed = true;
        List<String> candidates = getPossibleWords();
        if (candidates.isEmpty()) {
            throw new GameException("Нет подходящих слов для подсказки.");
        }
        // Для детерминированного поведения и удобства тестирования возвращаем первое слово
        return candidates.getFirst();
    }

    /**
     * Возвращает список слов из словаря, которые соответствуют
     * всем ранее сделанным ходам.
     *
     * @return список возможных слов.
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
     * @return true, если последняя догадка совпадает с ответом.
     */
    public boolean isWin() {
        return !guesses.isEmpty() && guesses.getLast().equals(answer);
    }

    /**
     * Проверяет, завершена ли игра (победой или поражением).
     *
     * @return true, если игра завершена.
     */
    public boolean isFinished() {
        return isWin() || stepsLeft <= 0;
    }

    // Геттеры
    public int getStepsLeft() { return stepsLeft; }
    public String getAnswer() { return answer; }
    public boolean wasHintUsed() { return hintWasUsed; }
    public List<String> getGuesses() { return List.copyOf(guesses); }
    public List<String> getHints() { return List.copyOf(hints); }
}