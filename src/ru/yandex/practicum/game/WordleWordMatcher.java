package ru.yandex.practicum.game;

public class WordleWordMatcher implements WordMatcher {
    @Override
    public String match(String guess, String secret) {
        StringBuilder result = new StringBuilder("-----");
        boolean[] usedInAnswer = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == secret.charAt(i)) {
                result.setCharAt(i, '+');
                usedInAnswer[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result.charAt(i) == '+') continue;
            char gChar = guess.charAt(i);
            for (int j = 0; j < 5; j++) {
                if (!usedInAnswer[j] && gChar == secret.charAt(j)) {
                    result.setCharAt(i, '^');
                    usedInAnswer[j] = true;
                    break;
                }
            }
        }
        return result.toString();
    }
}