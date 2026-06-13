package ru.yandex.practicum.util;

public class ServerJsonUtils {
    public static String extractJsonValue(String jsonPart, String key) {
        if (jsonPart == null || key == null) return null;

        String pattern = "\"" + key + "\":";
        int idx = jsonPart.indexOf(pattern);
        if (idx == -1) return null;
        idx += pattern.length();

        while (idx < jsonPart.length() && (jsonPart.charAt(idx) == ' ' || jsonPart.charAt(idx) == '\t')) {
            idx++;
        }
        if (idx >= jsonPart.length()) return null;

        if (jsonPart.charAt(idx) == '"') {
            int startQuote = idx + 1;
            int endQuote = jsonPart.indexOf('"', startQuote);
            if (endQuote == -1) return null;
            return jsonPart.substring(startQuote, endQuote);
        } else {
            int endNum = idx;
            while (endNum < jsonPart.length() && (Character.isDigit(jsonPart.charAt(endNum)) ||
                    jsonPart.charAt(endNum) == '.' || jsonPart.charAt(endNum) == '-')) {
                endNum++;
            }
            return jsonPart.substring(idx, endNum);
        }
    }
}