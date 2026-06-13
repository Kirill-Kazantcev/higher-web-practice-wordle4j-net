package ru.yandex.practicum.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.game.GameException;
import ru.yandex.practicum.exception.game.InvalidWordLengthException;
import ru.yandex.practicum.exception.game.WordNotFoundInDictionaryException;
import ru.yandex.practicum.exception.system.GameStateException;
import ru.yandex.practicum.game.WordleWordMatcher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("FieldCanBeLocal")
class WordleGameTest {
    private WordleDictionary dictionary;
    private WordleWordMatcher matcher;
    private WordleGame game;

    @BeforeEach
    void setUp() {
        dictionary = new WordleDictionary(List.of("аббат", "вагон", "ухарь", "сосна"));
        matcher = new WordleWordMatcher();
        game = new WordleGame(dictionary, matcher, "аббат");
    }

    @Test
    void makeMove_validWord_shouldReturnHintAndDecreaseSteps() throws Exception {
        String hint = game.makeMove("вагон");
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertEquals(5, game.getStepsLeft());
        assertEquals(1, game.getStepsUsed());
    }

    @Test
    void makeMove_winningWord_shouldReturnFullMatch() throws Exception {
        String hint = game.makeMove("аббат");
        assertEquals("+++++", hint);
        assertTrue(game.isWin());
        assertTrue(game.isFinished());
    }

    @Test
    void makeMove_invalidWordLength_shouldThrowInvalidWordLengthException() {
        assertThrows(InvalidWordLengthException.class, () -> game.makeMove("привет"));
        assertThrows(InvalidWordLengthException.class, () -> game.makeMove("кот"));
    }

    @Test
    void makeMove_wordNotInDictionary_shouldThrowWordNotFoundInDictionaryException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeMove("пенёк"));
    }

    // ИСПРАВЛЕНО: ожидаем GameStateException вместо IllegalStateException
    @Test
    void makeMove_afterGameFinished_shouldThrowGameStateException() throws Exception {
        game.makeMove("аббат");
        assertThrows(GameStateException.class, () -> game.makeMove("вагон"));
    }

    @Test
    void gameLose_shouldFinishWithoutWin() throws Exception {
        for (int i = 0; i < WordleGame.MAX_STEPS; i++) {
            game.makeMove("вагон");
        }
        assertFalse(game.isWin());
        assertTrue(game.isFinished());
        assertEquals(0, game.getStepsLeft());
    }

    @Test
    void getHintWord_shouldReturnPossibleWord() throws GameException {
        String hintWord = game.getHintWord();
        assertNotNull(hintWord);
        assertTrue(dictionary.getAllWords().contains(hintWord));
        assertEquals(1, game.getHintsUsed());
    }

    @Test
    void getHintWord_afterMove_shouldReturnDifferentWord() throws Exception {
        game.makeMove("вагон");
        String hintWord = game.getHintWord();
        assertNotNull(hintWord);
        assertTrue(game.getPossibleWords().contains("аббат"));
    }

    @Test
    void getPossibleWords_shouldReturnAllWordsInitially() {
        List<String> possible = game.getPossibleWords();
        assertEquals(4, possible.size());
    }

    @Test
    void getPossibleWords_afterMove_shouldFilterCorrectly() throws Exception {
        game.makeMove("вагон");
        List<String> possible = game.getPossibleWords();
        assertTrue(possible.contains("аббат"));
        assertFalse(possible.contains("вагон"));
    }

    @Test
    void getStepsUsed_shouldReturnCorrectCount() throws Exception {
        assertEquals(0, game.getStepsUsed());
        game.makeMove("вагон");
        assertEquals(1, game.getStepsUsed());
        game.makeMove("ухарь");
        assertEquals(2, game.getStepsUsed());
    }

    @Test
    void getHintsUsed_shouldReturnCorrectCount() throws GameException {
        assertEquals(0, game.getHintsUsed());
        game.getHintWord();
        assertEquals(1, game.getHintsUsed());
        game.getHintWord();
        assertEquals(2, game.getHintsUsed());
    }

    @Test
    void isFinished_beforeAnyMove_shouldReturnFalse() {
        assertFalse(game.isFinished());
    }

    @Test
    void getStepsLeft_shouldStartAtSix() {
        assertEquals(6, game.getStepsLeft());
    }

    @Test
    void getAnswer_shouldReturnCorrectWord() {
        assertEquals("аббат", game.getAnswer());
    }
}