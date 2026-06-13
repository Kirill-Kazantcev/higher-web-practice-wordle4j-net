package ru.yandex.practicum.game;

import java.util.HashMap;
import java.util.Map;

public class WordleWordMatcher implements WordMatcher, GameConstants {
    @Override
    public String match(String guess, String secret) {
        char[] result = new char[WORD_LENGTH];

        Map<Character, Integer> secretFreq = new HashMap<>();
        for (int i = 0; i < WORD_LENGTH; i++) {
            char c = secret.charAt(i);
            secretFreq.put(c, secretFreq.getOrDefault(c, 0) + 1);
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == secret.charAt(i)) {
                result[i] = '+';
                char c = guess.charAt(i);
                secretFreq.put(c, secretFreq.get(c) - 1);
            } else {
                result[i] = '-';
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (result[i] == '+') {
                continue;
            }

            char guessChar = guess.charAt(i);
            Integer count = secretFreq.get(guessChar);

            if (count != null && count > 0) {
                result[i] = '^';
                secretFreq.put(guessChar, count - 1);
            }
        }

        return new String(result);
    }
}