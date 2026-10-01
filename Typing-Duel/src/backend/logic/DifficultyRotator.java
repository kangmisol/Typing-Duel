package backend.logic;

import backend.model.Difficulty;
import backend.storage.FileDataStore.WordEntry;
import backend.storage.FileDataStore;

import java.io.IOException;
import java.util.*;

/**
 * The {@code DifficultyRotator} class is responsible for managing and rotating
 * words across different difficulty levels in a cyclical order:
 * EASY → MEDIUM → HARD → repeat.
 *
 * <p>This class ensures that:
 * <ul>
 *     <li>Words are grouped by difficulty.</li>
 *     <li>Each difficulty pool is shuffled to provide randomness.</li>
 *     <li>Words are not repeated until the pool is exhausted.</li>
 *     <li>All pools are reloaded automatically when empty.</li>
 * </ul>
 *
 * <p>The class retrieves word data from a {@link FileDataStore} and organizes
 * it into difficulty-based collections for efficient access.
 *
 * @author Mrida Hingmire
 */
public class DifficultyRotator {

    /** Data source used to load word entries from persistent storage. */
    private final FileDataStore dataStore;

    /** Maps each difficulty level to its corresponding list of word entries. */
    private final Map<Difficulty, List<WordEntry>> wordsByDifficulty = new HashMap<>();

    /** The current difficulty level in the rotation. */
    private Difficulty currentDifficulty = Difficulty.EASY;

    /** Ordered array defining the rotation sequence of difficulties. */
    private final Difficulty[] difficulties = {
            Difficulty.EASY,
            Difficulty.MEDIUM,
            Difficulty.HARD
    };

    /** Index tracking the current position in the difficulty rotation. */
    private int difficultyIndex = 0;

    /**
     * Constructs a {@code DifficultyRotator} and initializes all word pools
     * by loading data from the provided {@link FileDataStore}.
     *
     * @param dataStore the data source containing word entries
     * @throws IOException if an error occurs while reading from the data source
     */
    public DifficultyRotator(FileDataStore dataStore) throws IOException {
        this.dataStore = dataStore;
        loadWords();
    }

    /**
     * Retrieves the next word based on the current difficulty level,
     * then advances the rotation to the next difficulty.
     *
     * <p>Behavior:
     * <ul>
     *     <li>If word pools are not initialized, they are loaded.</li>
     *     <li>If the current difficulty pool is empty, all pools are reloaded.</li>
     *     <li>The first word in the current pool is removed and returned.</li>
     *     <li>The difficulty is rotated after retrieval.</li>
     * </ul>
     *
     * @return the next {@link WordEntry} corresponding to the current difficulty
     */
    public WordEntry getNextWord() {
        loadIfNeeded();

        List<WordEntry> currentWords = wordsByDifficulty.get(currentDifficulty);

        // If the current difficulty pool is exhausted, reload them all
        if (currentWords.isEmpty()) {
            loadWords();
            currentWords = wordsByDifficulty.get(currentDifficulty);
        }

        // Consume the first word and rotate to the next difficulty
        WordEntry word = currentWords.remove(0);
        rotateDifficulty();
        return word;
    }

    /**
     * Returns the current difficulty level in the rotation cycle.
     *
     * @return the current {@link Difficulty}
     */
    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Advances the difficulty to the next level in a circular sequence.
     *
     * <p>Example rotation:
     * EASY → MEDIUM → HARD → EASY → ...
     */
    private void rotateDifficulty() {
        difficultyIndex = (difficultyIndex + 1) % difficulties.length;
        currentDifficulty = difficulties[difficultyIndex];
    }

    /**
     * Ensures that word pools are loaded if they have not yet been initialized.
     */
    private void loadIfNeeded() {
        if (wordsByDifficulty.isEmpty()) {
            loadWords();
        }
    }

    /**
     * Loads words from the data store and organizes them into difficulty-based pools.
     *
     * <p>Processing steps:
     * <ul>
     *     <li>Initializes empty lists for each difficulty.</li>
     *     <li>Loads word entries from the JSON data source.</li>
     *     <li>Assigns each word to its corresponding difficulty group.</li>
     *     <li>Ignores invalid or improperly formatted difficulty values.</li>
     *     <li>If a difficulty pool is empty, inserts a default fallback word.</li>
     *     <li>Shuffles each pool to ensure randomness.</li>
     * </ul>
     *
     * <p>This method guarantees that each difficulty always has at least one word.
     */
    private void loadWords() {
        for (Difficulty diff : difficulties) {
            wordsByDifficulty.put(diff, new ArrayList<>());
        }

        for (FileDataStore.WordEntry entry : dataStore.loadWordsJSON()) {
            if (entry != null && entry.getText() != null) {
                try {
                    Difficulty diff = Difficulty.valueOf(entry.getDifficulty().toUpperCase());
                    wordsByDifficulty.get(diff).add(entry);
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid difficulty values
                }
            }
        }

        for (Difficulty diff : difficulties) {
            List<WordEntry> pool = wordsByDifficulty.get(diff);
            if (pool.isEmpty()) {
                // Add fallback word if no words exist for this difficulty
                pool.add(new WordEntry("typing", diff.name(), "GENERAL", 10));
            } else {
                // Shuffle each pool to ensure variety every time they are reloaded
                Collections.shuffle(pool);
            }
        }
    }
}