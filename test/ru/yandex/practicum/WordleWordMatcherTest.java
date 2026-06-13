package ru.yandex.practicum;

import ru.yandex.practicum.game.WordleWordMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WordleWordMatcherTest {
    private WordleWordMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new WordleWordMatcher();
    }

    @Test
    void matchFull() {
        assertEquals("+++++", matcher.match("аббат", "аббат"));
    }

    @Test
    void matchPartial() {
        assertEquals("-^---", matcher.match("вагон", "аббат"));
    }

    @Test
    void matchWithRepeatingLetters_correctHandling() {
        assertEquals("++-^-", matcher.match("абзац", "абака"));
    }

    @Test
    void matchAllWrong() {
        assertEquals("-----", matcher.match("гдеёж", "аббат"));
    }

    @Test
    void matchWithDuplicateGuess_correctHandling() {
        assertEquals("+---+", matcher.match("ааааа", "абвга"));
    }

    @Test
    void matchWithComplexRepeatingLetters() {
        assertEquals("^^^^^", matcher.match("аскас", "касса"));
    }

    @Test
    void matchWithAllRepeatingLetters() {
        assertEquals("+++++", matcher.match("ааааа", "ааааа"));
    }

    @Test
    void matchWithNoMatches() {
        assertEquals("-----", matcher.match("ббббб", "ааааа"));
    }

    @Test
    void matchWordleExample1() {
        assertEquals("+++-^", matcher.match("слайд", "сладк"));
    }

    @Test
    void matchWordleExample2() {
        assertEquals("+^+^+", matcher.match("сдалк", "сладк"));
    }

    @Test
    void matchEdgeCase_guessHasMoreLettersThanSecret() {
        assertEquals("+----", matcher.match("ааааа", "абвгд"));
    }
}