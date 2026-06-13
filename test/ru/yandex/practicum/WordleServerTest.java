package ru.yandex.practicum;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.util.ConfigLoader;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для HTTP-сервера статистики WordleServer.
 * Проверяют структуру класса и наличие основных методов.
 */
class WordleServerTest {

    @Test
    void testConstructorExists() {
        try {
            ConfigLoader config = new ConfigLoader("application.properties");
            int port = config.getInt("server.port");

            WordleServer server = new WordleServer(port, "test_stats.txt");
            assertNotNull(server);
        } catch (Exception e) {
            fail("Конструктор WordleServer(int, String) не работает: " + e.getMessage());
        }
    }

    @Test
    void testMainMethodExists() {
        try {
            Class<?> clazz = Class.forName("ru.yandex.practicum.WordleServer");
            Method mainMethod = clazz.getMethod("main", String[].class);
            assertNotNull(mainMethod);
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
    void testEscapeJsonMethodExists() {
        try {
            Method escapeMethod = WordleServer.class.getDeclaredMethod("escapeJson", String.class);
            escapeMethod.setAccessible(true);

            ConfigLoader config = new ConfigLoader("application.properties");
            int port = config.getInt("server.port");
            WordleServer server = new WordleServer(port, "test_stats.txt");

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
        } catch (NoSuchMethodException e) {
            fail("Метод parseJson не найден: " + e.getMessage());
        }
    }

    @Test
    void testParseQueryMethodExists() {
        try {
            Method parseQueryMethod = WordleServer.class.getDeclaredMethod("parseQuery", String.class);
            parseQueryMethod.setAccessible(true);
            assertNotNull(parseQueryMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод parseQuery не найден: " + e.getMessage());
        }
    }

    @Test
    void testHandlePostResultMethodExists() {
        try {
            Method handleMethod = WordleServer.class.getDeclaredMethod("handlePostResult",
                    HttpExchange.class);
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
                    HttpExchange.class);
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
                    HttpExchange.class);
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
                    HttpExchange.class);
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
                    HttpExchange.class, int.class, String.class);
            sendResponseMethod.setAccessible(true);
            assertNotNull(sendResponseMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод sendResponse не найден: " + e.getMessage());
        }
    }
}