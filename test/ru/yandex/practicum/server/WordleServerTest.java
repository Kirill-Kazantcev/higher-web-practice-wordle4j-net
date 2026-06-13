package ru.yandex.practicum.server;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.util.ConfigLoader;

import java.io.PrintWriter;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class WordleServerTest {

    @Test
    void testConstructorExists() {
        try {
            ConfigLoader config = new ConfigLoader("application.properties");
            int port = config.getInt("server.port");

            PrintWriter testLog = new PrintWriter(System.out);
            WordleServer server = new WordleServer(port, "test_stats.txt", testLog);
            assertNotNull(server);
        } catch (Exception e) {
            fail("Конструктор WordleServer(int, String, PrintWriter) не работает: " + e.getMessage());
        }
    }

    @Test
    void testMainMethodExists() {
        try {
            Class<?> clazz = Class.forName("ru.yandex.practicum.server.WordleServer");
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
    void testStopMethodExists() {
        try {
            Method stopMethod = WordleServer.class.getMethod("stop");
            assertNotNull(stopMethod);
        } catch (NoSuchMethodException e) {
            fail("Метод stop не найден: " + e.getMessage());
        }
    }

    @Test
    void testEscapeJsonMethodExists() {
        try {
            Method escapeMethod = WordleServer.class.getDeclaredMethod("escapeJson", String.class);
            escapeMethod.setAccessible(true);

            ConfigLoader config = new ConfigLoader("application.properties");
            int port = config.getInt("server.port");
            PrintWriter testLog = new PrintWriter(System.out);
            WordleServer server = new WordleServer(port, "test_stats.txt", testLog);

            String result = (String) escapeMethod.invoke(server, "Hello \"World\"");
            assertEquals("Hello \\\"World\\\"", result);
        } catch (Exception e) {
            fail("Метод escapeJson не найден или не работает: " + e.getMessage());
        }
    }

    // В WordleServer нет методов extractJsonString, extractJsonInt, extractJsonBoolean
    // Они заменены на handlePostResult, handleGetTop, handleGetStats
    // Поэтому удаляем эти тесты или проверяем наличие основных методов

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