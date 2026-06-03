package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для HTTP-сервера статистики WordleServer.
 * Проверяют структуру класса и наличие основных методов.
 */
class WordleServerTest {
    private WordleServer server;

    @BeforeEach
    void setUp() {
        server = new WordleServer();
    }

    @Test
    void testWordleServerClassExists() {
        assertNotNull(server);
    }

    @Test
    void testMainMethodExists() {
        try {
            Class<?> clazz = Class.forName("ru.yandex.practicum.WordleServer");
            Method mainMethod = clazz.getMethod("main", String[].class);
            assertNotNull(mainMethod);
            assertEquals("public static void",
                    java.lang.reflect.Modifier.toString(mainMethod.getModifiers()) + " " + mainMethod.getReturnType().getName());
        } catch (Exception e) {
            fail("Метод main не найден: " + e.getMessage());
        }
    }

    @Test
    void testStartMethodExists() {
        try {
            Method startMethod = WordleServer.class.getMethod("start");
            assertNotNull(startMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод start не найден: " + e.getMessage());
        }
    }

    @Test
    void testConstantsAreDefined() {
        try {
            java.lang.reflect.Field portField = WordleServer.class.getDeclaredField("PORT");
            portField.setAccessible(true);
            assertEquals(8081, portField.getInt(null));

            java.lang.reflect.Field statsFileField = WordleServer.class.getDeclaredField("STATS_FILE");
            statsFileField.setAccessible(true);
            assertEquals("stats.txt", statsFileField.get(null));
        } catch (Exception e) {
            fail("Константы не найдены: " + e.getMessage());
        }
    }

    @Test
    void testEscapeJsonMethodExists() {
        try {
            Method escapeMethod = WordleServer.class.getDeclaredMethod("escapeJson", String.class);
            escapeMethod.setAccessible(true);
            assertNotNull(escapeMethod);

            // Вызываем нестатический метод на экземпляре сервера
            String result = (String) escapeMethod.invoke(server, "Hello \"World\"");
            assertEquals("Hello \\\"World\\\"", result);
        } catch (Exception e) {
            fail("Метод escapeJson не найден или не работает: " + e.getMessage());
        }
    }

    @Test
    void testParseJsonMethodExists() {
        try {
            Method parseMethod = WordleServer.class.getDeclaredMethod("parseJson", String.class);
            parseMethod.setAccessible(true);
            assertNotNull(parseMethod);

            // Проверяем работу метода
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> result = (java.util.Map<String, String>) parseMethod.invoke(server, "{\"key\":\"value\"}");
            assertNotNull(result);
        } catch (Exception e) {
            fail("Метод parseJson не найден или не работает: " + e.getMessage());
        }
    }

    @Test
    void testParseQueryMethodExists() {
        try {
            Method parseQueryMethod = WordleServer.class.getDeclaredMethod("parseQuery", String.class);
            parseQueryMethod.setAccessible(true);
            assertNotNull(parseQueryMethod);

            // Проверяем работу метода
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> result = (java.util.Map<String, String>) parseQueryMethod.invoke(server, "nickname=Кирилл&wins=20");
            assertNotNull(result);
        } catch (Exception e) {
            fail("Метод parseQuery не найден или не работает: " + e.getMessage());
        }
    }

    @Test
    void testHandlePostResultMethodExists() {
        try {
            Method handleMethod = WordleServer.class.getDeclaredMethod("handlePostResult",
                    com.sun.net.httpserver.HttpExchange.class);
            handleMethod.setAccessible(true);
            assertNotNull(handleMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод handlePostResult не найден: " + e.getMessage());
        }
    }

    @Test
    void testHandleGetTopMethodExists() {
        try {
            Method handleMethod = WordleServer.class.getDeclaredMethod("handleGetTop",
                    com.sun.net.httpserver.HttpExchange.class);
            handleMethod.setAccessible(true);
            assertNotNull(handleMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод handleGetTop не найден: " + e.getMessage());
        }
    }

    @Test
    void testHandleGetStatsMethodExists() {
        try {
            Method handleMethod = WordleServer.class.getDeclaredMethod("handleGetStats",
                    com.sun.net.httpserver.HttpExchange.class);
            handleMethod.setAccessible(true);
            assertNotNull(handleMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод handleGetStats не найден: " + e.getMessage());
        }
    }

    @Test
    void testReadBodyMethodExists() {
        try {
            Method readBodyMethod = WordleServer.class.getDeclaredMethod("readBody",
                    com.sun.net.httpserver.HttpExchange.class);
            readBodyMethod.setAccessible(true);
            assertNotNull(readBodyMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод readBody не найден: " + e.getMessage());
        }
    }

    @Test
    void testSendResponseMethodExists() {
        try {
            Method sendResponseMethod = WordleServer.class.getDeclaredMethod("sendResponse",
                    com.sun.net.httpserver.HttpExchange.class, int.class, String.class);
            sendResponseMethod.setAccessible(true);
            assertNotNull(sendResponseMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод sendResponse не найден: " + e.getMessage());
        }
    }
}