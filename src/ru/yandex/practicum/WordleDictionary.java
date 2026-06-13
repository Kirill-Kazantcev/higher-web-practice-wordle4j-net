package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;
import ru.yandex.practicum.game.Dictionary;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class WordleDictionary implements Dictionary {
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
        if (word.length() != 5) {
            throw new InvalidWordFormatException("Слово должно состоять ровно из 5 букв.");
        }
        if (!contains(word)) {
            throw new WordNotFoundInDictionaryException(word);
        }
    }
}