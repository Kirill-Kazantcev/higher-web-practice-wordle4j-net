package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;
import ru.yandex.practicum.game.Dictionary;
import ru.yandex.practicum.game.GameConstants;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class WordleDictionary implements Dictionary, GameConstants {
    private final List<String> words;

    public WordleDictionary(List<String> words) {
        this.words = words;
    }

    @Override
    public List<String> getAllWords() {
        return List.copyOf(words);
    }

    @Override
    public boolean contains(String word) {
        return words.contains(word);
    }

    @Override
    public int size() {
        return words.size();
    }

    @Override
    public void validate(String word) throws GameException {
        if (word.length() != WORD_LENGTH) {
            throw new InvalidWordFormatException("Слово должно состоять ровно из " + WORD_LENGTH + " букв.");
        }
        if (!contains(word)) {
            throw new WordNotFoundInDictionaryException(word);
        }
    }
}