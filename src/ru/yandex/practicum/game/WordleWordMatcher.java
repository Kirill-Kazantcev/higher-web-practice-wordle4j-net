package ru.yandex.practicum.game;

/**
 * Реализация алгоритма сравнения слов для игры Wordle.
 * <p>
 * Сравнивает слово-догадку с загаданным словом и возвращает строку из символов:
 * '+' — буква есть на правильной позиции,
 * '^' — буква есть в слове, но на другой позиции,
 * '-' — буквы нет в загаданном слове.
 * </p>
 */
public class WordleWordMatcher implements WordMatcher {

    /**
     * Сравнивает слово-догадку с секретным словом.
     *
     * @param guess   слово, введённое игроком
     * @param secret загаданное слово
     * @return строка подсказки длиной 5 символов, состоящая из '+', '^', '-'
     */
    @Override
    public String match(String guess, String secret) {
        StringBuilder result = new StringBuilder("-----");
        boolean[] usedInAnswer = new boolean[5];

        // Точные совпадения
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == secret.charAt(i)) {
                result.setCharAt(i, '+');
                usedInAnswer[i] = true;
            }
        }

        // Буквы не на своих местах
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