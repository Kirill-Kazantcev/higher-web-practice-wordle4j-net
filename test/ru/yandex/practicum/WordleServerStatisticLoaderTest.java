package ru.yandex.practicum;

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
    void saveAndLoad_shouldPreserveData(@TempDir Path tempDir) throws IOException {
        Path statsFile = tempDir.resolve("stats.txt");
        WordleServerStatisticLoader loader = new WordleServerStatisticLoader(statsFile.toString());

        Map<String, WordleServerStatisticLoader.PlayerStats> statsToSave = Map.of(
                "Кирилл", new WordleServerStatisticLoader.PlayerStats(20, 1, 5, 80),
                "Анна", new WordleServerStatisticLoader.PlayerStats(10, 5, 11, 45)
        );

        loader.save(statsToSave);
        Map<String, WordleServerStatisticLoader.PlayerStats> loadedStats = loader.load();

        assertEquals(2, loadedStats.size());
        assertTrue(loadedStats.containsKey("Кирилл"));
        assertTrue(loadedStats.containsKey("Анна"));
        assertEquals(20, loadedStats.get("Кирилл").wins);
        assertEquals(1, loadedStats.get("Кирилл").losses);
        assertEquals(5, loadedStats.get("Кирилл").hintsUsed);
        assertEquals(80, loadedStats.get("Кирилл").totalSteps);
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
}