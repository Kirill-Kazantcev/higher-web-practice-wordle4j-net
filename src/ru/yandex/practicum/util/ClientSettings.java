package ru.yandex.practicum.util;

public class ClientSettings {
    private static ConfigLoader config;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8081;
    private static final String DEFAULT_DICTIONARY = "words_ru.txt";
    private static final String DEFAULT_LOG = "wordle.log";

    static {
        try {
            config = new ConfigLoader("application.properties");
            System.out.println("Конфигурация загружена из application.properties");
        } catch (Exception e) {
            System.err.println("Не удалось загрузить application.properties, используются значения по умолчанию");
            config = null;
        }
    }

    public static String getServerHost() {
        if (config != null) {
            try {
                String host = config.getString("server.host");
                if (host != null && !host.isBlank()) return host;
            } catch (Exception e) {
                System.err.println("Не удалось прочитать server.host, используется localhost");
            }
        }
        return DEFAULT_HOST;
    }

    public static int getServerPort() {
        if (config != null) {
            try {
                return config.getInt("server.port");
            } catch (Exception e) {
                System.err.println("Не удалось прочитать server.port, используется 8081");
            }
        }
        return DEFAULT_PORT;
    }

    public static String getDictionaryPath() {
        if (config != null) {
            try {
                String path = config.getString("dictionary.path");
                if (path != null && !path.isBlank()) return path;
            } catch (Exception e) {
                System.err.println("Не удалось прочитать dictionary.path, используется words_ru.txt");
            }
        }
        return DEFAULT_DICTIONARY;
    }

    public static String getLogFile() {
        if (config != null) {
            try {
                String log = config.getString("log.file");
                if (log != null && !log.isBlank()) return log;
            } catch (Exception e) {
                System.err.println("Не удалось прочитать log.file, используется wordle.log");
            }
        }
        return DEFAULT_LOG;
    }
}