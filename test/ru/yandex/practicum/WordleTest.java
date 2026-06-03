package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для главного класса игры Wordle.
 */
class WordleTest {

    @Test
    void testConstantsAreDefined() {
        // Проверяем, что константы определены через рефлексию
        try {
            Class<?> clazz = Class.forName("ru.yandex.practicum.Wordle");
            java.lang.reflect.Field dictField = clazz.getDeclaredField("DICTIONARY_FILE");
            dictField.setAccessible(true);
            assertNotNull(dictField.get(null));

            java.lang.reflect.Field logField = clazz.getDeclaredField("LOG_FILE");
            logField.setAccessible(true);
            assertNotNull(logField.get(null));

            java.lang.reflect.Field serverUrlField = clazz.getDeclaredField("SERVER_URL");
            serverUrlField.setAccessible(true);
            assertNotNull(serverUrlField.get(null));
        } catch (Exception e) {
            fail("Константы не найдены: " + e.getMessage());
        }
    }

    @Test
    void testMainMethodExists() {
        try {
            Class<?> clazz = Class.forName("ru.yandex.practicum.Wordle");
            java.lang.reflect.Method mainMethod = clazz.getMethod("main", String[].class);
            assertNotNull(mainMethod);
            assertEquals("public static void",
                    java.lang.reflect.Modifier.toString(mainMethod.getModifiers()) + " " + mainMethod.getReturnType().getName());
        } catch (Exception e) {
            fail("Метод main не найден: " + e.getMessage());
        }
    }
}