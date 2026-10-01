package backend.logic;

import backend.model.HighScoreEntry;
import backend.model.HighScoreTable;
import backend.storage.FileDataStore;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages persistence and retrieval for the global high score table.
 *
 * <p>This class wraps {@link FileDataStore} to load and save
 * {@link HighScoreTable} data from persistent storage. It exposes convenience
 * methods for recording new scores and retrieving leaderboard entries for a
 * specific stage or mode.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public class ScoreManager {

    private final FileDataStore dataStore;

    /**
     * Creates a score manager backed by a file data store in the given directory.
     *
     * @param dataDirectory the directory containing score persistence files
     * @throws IOException if the backing data store cannot be initialized
     */
    public ScoreManager(String dataDirectory) throws IOException {
        this(new FileDataStore(dataDirectory));
    }

    /**
     * Creates a score manager that uses the provided data store.
     *
     * @param dataStore the data store used to load and save high scores
     */
    public ScoreManager(FileDataStore dataStore) {
        this.dataStore = Objects.requireNonNull(dataStore, "dataStore");
    }

    /**
     * Returns the top scores for a stage using the default leaderboard size.
     *
     * @param stageName the stage or mode name to filter by
     * @return a list of leaderboard entries ordered from highest to lowest score
     */
    public List<HighScoreEntry> getTopScores(String stageName) {
        return getTopScores(stageName, HighScoreTable.MAX_ENTRIES);
    }

    /**
     * Returns up to a specified number of top scores for a stage.
     *
     * @param stageName the stage or mode name to filter by
     * @param limit the maximum number of entries to return
     * @return a sorted list of matching leaderboard entries, or an empty list if
     *         no entries match or the limit is not positive
     */
    public List<HighScoreEntry> getTopScores(String stageName, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        int stageIndex = stageIndexFromName(stageName);
        HighScoreTable table = dataStore.loadHighScores();
        if (table == null || table.getEntries().isEmpty()) {
            return Collections.emptyList();
        }

        List<HighScoreEntry> filtered = new ArrayList<>();
        for (HighScoreEntry entry : table.getEntries()) {
            if (entry != null && entry.getLevelReached() == stageIndex) {
                filtered.add(entry);
            }
        }

        Collections.sort(filtered);
        return filtered.size() <= limit ? filtered : filtered.subList(0, limit);
    }

    /**
     * Saves a score for the named stage or mode.
     *
     * @param username the username associated with the score
     * @param score the score to record
     * @param stageName the stage or mode name reached by the player
     * @return {@code true} if the score was added and persisted successfully;
     *         {@code false} otherwise
     */
    public boolean saveScore(String username, int score, String stageName) {
        return saveScore(username, score, stageIndexFromName(stageName));
    }

    /**
     * Saves a score for the given numeric stage index.
     *
     * @param username the username associated with the score
     * @param score the score to record
     * @param levelReached the numeric stage or mode index reached by the player
     * @return {@code true} if the score was added and persisted successfully;
     *         {@code false} if validation failed or the table was unchanged
     */
    public boolean saveScore(String username, int score, int levelReached) {
        if (username == null || username.isBlank() || score < 0) {
            return false;
        }

        HighScoreTable table = dataStore.loadHighScores();
        HighScoreEntry entry = new HighScoreEntry(username, score, levelReached, LocalDate.now().toString());
        boolean changed = table.addEntry(entry);

        if (changed) {
            return dataStore.saveHighScores(table);
        }

        return false;
    }

    /**
     * Converts a stage label into the leaderboard stage index used by score entries.
     *
     * @param stageName the stage or mode name to convert
     * @return the parsed stage index, or {@code 0} if the name is {@code null},
     *         invalid, or refers to endless mode
     */
    private int stageIndexFromName(String stageName) {
        if (stageName == null) {
            return 0;
        }

        String normalized = stageName.trim().toLowerCase();
        if (normalized.equals("endless")) {
            return 0;
        }

        if (normalized.startsWith("stage ")) {
            try {
                return Integer.parseInt(normalized.substring(6).trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }
}
