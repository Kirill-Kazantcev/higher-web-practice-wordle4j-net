package ru.yandex.practicum.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WordleServerStatisticLoaderTest {

    @Test
    void load_nonExistentFile_shouldReturnEmptyMap(@TempDir Path tempDir) {
        Path statsFile = tempDir.resolve("non_existent.txt");
        WordleServerStatisticLoader loader = new WordleServerStatisticLoader(statsFile.toString());
        Map<String, WordleServerStatisticLoader.PlayerStats> stats = loader.load();
        assertTrue(stats.isEmpty());
    }

    @Test
    void saveAndLoad_shouldPreserveData(@TempDir Path tempDir) {
        Path statsFile = tempDir.resolve("stats.txt");
        WordleServerStatisticLoader loader = new WordleServerStatisticLoader(statsFile.toString());

        Map<String, WordleServerStatisticLoader.PlayerStats> statsToSave = Map.of(
                "Кирилл", new WordleServerStatisticLoader.PlayerStats(20, 1, 5, 80),
                "Анна", new WordleServerStatisticLoader.PlayerStats(10, 5, 11, 45)
        );

        loader.save(statsToSave);
        Map<String, WordleServerStatisticLoader.PlayerStats> loadedStats = loader.load();

        assertStatsEqual(statsToSave, loadedStats);
    }

    @Test
    void load_emptyFile_shouldReturnEmptyMap(@TempDir Path tempDir) throws IOException {
        Path statsFile = tempDir.resolve("empty.txt");
        Files.write(statsFile, List.of());

        WordleServerStatisticLoader loader = new WordleServerStatisticLoader(statsFile.toString());
        Map<String, WordleServerStatisticLoader.PlayerStats> stats = loader.load();
        assertTrue(stats.isEmpty());
    }

    @Test
    void playerStats_calculateWinRate_shouldReturnCorrectValue() {
        WordleServerStatisticLoader.PlayerStats ps = new WordleServerStatisticLoader.PlayerStats(20, 5, 10, 80);
        assertEquals(80.0, ps.getWinRate(), 0.01);
    }

    @Test
    void playerStats_noGames_winRateShouldBeZero() {
        WordleServerStatisticLoader.PlayerStats ps = new WordleServerStatisticLoader.PlayerStats(0, 0, 0, 0);
        assertEquals(0.0, ps.getWinRate(), 0.01);
    }

    @Test
    void playerStats_calculateAvgSteps_shouldReturnCorrectValue() {
        WordleServerStatisticLoader.PlayerStats ps = new WordleServerStatisticLoader.PlayerStats(10, 0, 0, 45);
        assertEquals(4.5, ps.getAvgSteps(), 0.01);
    }

    @Test
    void playerStats_noWins_avgStepsShouldBeZero() {
        WordleServerStatisticLoader.PlayerStats ps = new WordleServerStatisticLoader.PlayerStats(0, 10, 5, 0);
        assertEquals(0.0, ps.getAvgSteps(), 0.01);
    }
    private void assertStatsEqual(Map<String, WordleServerStatisticLoader.PlayerStats> expected,
                                  Map<String, WordleServerStatisticLoader.PlayerStats> actual) {
        assertEquals(expected.size(), actual.size(), "Количество записей не совпадает");

        for (String name : expected.keySet()) {
            assertTrue(actual.containsKey(name), "Отсутствует игрок: " + name);

            WordleServerStatisticLoader.PlayerStats expectedStats = expected.get(name);
            WordleServerStatisticLoader.PlayerStats actualStats = actual.get(name);

            assertEquals(expectedStats.wins, actualStats.wins, "wins для " + name);
            assertEquals(expectedStats.losses, actualStats.losses, "losses для " + name);
            assertEquals(expectedStats.hintsUsed, actualStats.hintsUsed, "hintsUsed для " + name);
            assertEquals(expectedStats.totalSteps, actualStats.totalSteps, "totalSteps для " + name);
        }
    }
}