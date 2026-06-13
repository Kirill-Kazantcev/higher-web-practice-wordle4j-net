package ru.yandex.practicum.util;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    @SuppressWarnings("ClassCanBeRecord")
    public static class TopPlayer {
        public final String nickname;
        public final int wins;
        public final double winRate;

        public TopPlayer(String nickname, int wins, double winRate) {
            this.nickname = nickname;
            this.wins = wins;
            this.winRate = winRate;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class PlayerStats {
        public final String nickname;
        public final int wins;
        public final int losses;
        public final int hintsUsed;
        public final double avgSteps;
        public final double winRate;

        public PlayerStats(String nickname, int wins, int losses, int hintsUsed, double avgSteps, double winRate) {
            this.nickname = nickname;
            this.wins = wins;
            this.losses = losses;
            this.hintsUsed = hintsUsed;
            this.avgSteps = avgSteps;
            this.winRate = winRate;
        }
    }

    public static List<TopPlayer> parseTopPlayers(String json) {
        List<TopPlayer> result = new ArrayList<>();
        try {
            int topIdx = json.indexOf("\"top\":");
            if (topIdx == -1) return result;
            int start = json.indexOf('[', topIdx);
            int end = json.lastIndexOf(']');
            if (start == -1 || end == -1 || start >= end) return result;
            String content = json.substring(start + 1, end);
            if (content.trim().isEmpty()) return result;

            int braceCount = 0, lastStart = 0;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') {
                    if (braceCount == 0) lastStart = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        String obj = content.substring(lastStart, i + 1);
                        String nickname = extractJsonValue(obj, "nickname");
                        String winsStr = extractJsonValue(obj, "wins");
                        String winRateStr = extractJsonValue(obj, "winRate");
                        if (nickname != null && winsStr != null) {
                            try {
                                int wins = Integer.parseInt(winsStr);
                                double winRate = winRateStr != null ? Double.parseDouble(winRateStr) : 0.0;
                                result.add(new TopPlayer(nickname, wins, winRate));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static PlayerStats parsePlayerStats(String json) {
        String nickname = extractJsonValue(json, "nickname");
        String winsStr = extractJsonValue(json, "wins");
        if (nickname == null || winsStr == null) return null;
        try {
            int wins = Integer.parseInt(winsStr);

            String lossesStr = extractJsonValue(json, "losses");
            int losses = lossesStr != null ? Integer.parseInt(lossesStr) : 0;

            String hintsStr = extractJsonValue(json, "hintsUsed");
            int hintsUsed = hintsStr != null ? Integer.parseInt(hintsStr) : 0;

            String avgStepsStr = extractJsonValue(json, "avgSteps");
            double avgSteps = avgStepsStr != null ? Double.parseDouble(avgStepsStr) : 0.0;

            String winRateStr = extractJsonValue(json, "winRate");
            double winRate = winRateStr != null ? Double.parseDouble(winRateStr) : 0.0;

            return new PlayerStats(nickname, wins, losses, hintsUsed, avgSteps, winRate);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String extractJsonValue(String jsonPart, String key) {
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