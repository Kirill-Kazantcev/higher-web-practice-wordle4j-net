package ru.yandex.practicum;

import ru.yandex.practicum.game.WordleWordMatcher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WordleWordMatcherTest {
    private final WordleWordMatcher matcher = new WordleWordMatcher();

    @Test
    void matchFull() {
        assertEquals("+++++", matcher.match("аббат", "аббат"));
    }

    @Test
    void matchPartial() {
        assertEquals("-^---", matcher.match("вагон", "аббат"));
    }
}