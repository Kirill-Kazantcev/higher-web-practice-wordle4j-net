package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WordleTest {
    @Test
    void testMainMethodExists() {
        try {
            Class<?> clazz = Class.forName("ru.yandex.practicum.Wordle");
            clazz.getMethod("main", String[].class);
            assertTrue(true);
        } catch (Exception e) {
            fail("Метод main не найден");
        }
    }
}