package ru.yandex.practicum.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtils {
    private JsonUtils() {}

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String toWinSubmissionJson(String nickname, int steps, boolean usedHints) {
        return String.format("{\"nickname\":\"%s\",\"win\":true,\"steps\":%d,\"hintsUsed\":%d}",
                escape(nickname), steps, usedHints ? 1 : 0);
    }

    public static List<TopPlayerEntry> parseTopResponse(String json) {
        List<TopPlayerEntry> entries = new ArrayList<>();

        int topStart = json.indexOf("\"top\":");
        if (topStart == -1) return entries;
        int arrayStart = json.indexOf('[', topStart);
        int arrayEnd = json.lastIndexOf(']');
        if (arrayStart == -1 || arrayEnd == -1) return entries;

        String content = json.substring(arrayStart + 1, arrayEnd);
        Pattern pattern = Pattern.compile("\"nickname\"\\s*:\\s*\"([^\"]+)\".*?\"wins\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(content);

        int rank = 1;
        while (matcher.find()) {
            entries.add(new TopPlayerEntry(matcher.group(1), Integer.parseInt(matcher.group(2)), rank++));
        }
        return entries;
    }

    public record PlayerStats(String nickname, int wins, int losses, int hintsUsed,
                              double avgSteps, double winRate) {}

    public static PlayerStats parsePlayerStats(String json) {
        try {
            String nickname = extractNickname(json);
            int wins = extractWins(json);
            int losses = extractLosses(json);
            int hintsUsed = extractHintsUsed(json);
            double avgSteps = extractAvgSteps(json);
            double winRate = extractWinRate(json);

            return new PlayerStats(nickname, wins, losses, hintsUsed, avgSteps, winRate);
        } catch (Exception e) {
            return null;
        }
    }

    public record TopPlayerEntry(String nickname, int wins, int rank) {}

    // Упрощённые методы без параметра key
    private static String extractNickname(String json) {
        Pattern p = Pattern.compile("\"nickname\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static int extractWins(String json) {
        Pattern p = Pattern.compile("\"wins\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static int extractLosses(String json) {
        Pattern p = Pattern.compile("\"losses\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static int extractHintsUsed(String json) {
        Pattern p = Pattern.compile("\"hintsUsed\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static double extractAvgSteps(String json) {
        Pattern p = Pattern.compile("\"avgSteps\"\\s*:\\s*([\\d.]+)");
        Matcher m = p.matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }

    private static double extractWinRate(String json) {
        Pattern p = Pattern.compile("\"winRate\"\\s*:\\s*([\\d.]+)");
        Matcher m = p.matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }
}