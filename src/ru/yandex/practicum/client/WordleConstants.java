package ru.yandex.practicum.client;

import ru.yandex.practicum.util.ClientSettings;

public final class WordleConstants {
    public static final String DICTIONARY_FILE = ClientSettings.getDictionaryPath();
    public static final String LOG_FILE = ClientSettings.getLogFile();
    public static final String SERVER_HOST = ClientSettings.getServerHost();
    public static final int SERVER_PORT = ClientSettings.getServerPort();
    public static final String STATS_POST_PATH = "/result";
    public static final String TOP_GET_PATH = "/top";
    public static final String STATS_GET_PATH = "/stats";

    private WordleConstants() {}
}