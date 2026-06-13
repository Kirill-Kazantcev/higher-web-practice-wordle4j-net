package ru.yandex.practicum.client;

import ru.yandex.practicum.exception.game.GameException;
import ru.yandex.practicum.exception.system.GameStateException;
import ru.yandex.practicum.game.Dictionary;
import ru.yandex.practicum.game.GameConstants;
import ru.yandex.practicum.game.WordMatcher;

import java.util.*;

public class WordleGame implements GameConstants {
    public static final int MAX_STEPS = GameConstants.MAX_STEPS;

    private final Dictionary dictionary;
    private final WordMatcher matcher;
    private final String answer;
    private int stepsLeft;
    private final List<String> guesses = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private int hintsUsed;

    public WordleGame(Dictionary dictionary, WordMatcher matcher) {
        this(dictionary, matcher, chooseRandomAnswer(dictionary));
    }

    public WordleGame(Dictionary dictionary, WordMatcher matcher, String answer) {
        this.dictionary = dictionary;
        this.matcher = matcher;
        this.answer = answer;
        this.stepsLeft = MAX_STEPS;
        this.hintsUsed = 0;
    }

    private static String chooseRandomAnswer(Dictionary dictionary) {
        List<String> words = dictionary.getAllWords();
        return words.get(new Random().nextInt(words.size()));
    }

    public String makeMove(String word) throws GameException {
        if (isFinished()) {
            throw new GameStateException("Игра уже завершена.");
        }
        dictionary.validate(word);
        String hint = matcher.match(word, answer);
        guesses.add(word);
        hints.add(hint);
        stepsLeft--;
        return hint;
    }

    public String getHintWord() throws GameException {
        if (isFinished()) {
            throw new GameStateException("Игра уже завершена, нельзя запросить подсказку.");
        }
        hintsUsed++;
        List<String> candidates = getPossibleWords();
        if (candidates.isEmpty()) {
            throw new GameException("Нет подходящих слов для подсказки.");
        }
        return candidates.getFirst();
    }

    public List<String> getPossibleWords() {
        List<String> candidates = new ArrayList<>();
        outer:
        for (String candidate : dictionary.getAllWords()) {
            for (int i = 0; i < guesses.size(); i++) {
                if (!hints.get(i).equals(matcher.match(guesses.get(i), candidate))) {
                    continue outer;
                }
            }
            candidates.add(candidate);
        }
        return candidates;
    }

    public boolean isWin() {
        return !guesses.isEmpty() && guesses.getLast().equals(answer);
    }

    public boolean isFinished() {
        return isWin() || stepsLeft <= 0;
    }

    public int getStepsUsed() {
        return guesses.size();
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public int getStepsLeft() {
        return stepsLeft;
    }

    public String getAnswer() {
        return answer;
    }
}