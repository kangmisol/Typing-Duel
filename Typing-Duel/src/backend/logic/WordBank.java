package backend.logic;

import backend.storage.FileDataStore;
import backend.storage.FileDataStore.WordEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * WordBank.java
 * <p>
 * Loads and serves words from the JSON word bank ({@code data/words.json}).
 * Provides methods to get random words filtered by difficulty level.
 * </p>
 *
 * <p>This class has <strong>no GUI imports</strong>.</p>
 *
 * <p>Design Documentation Ã‚Â§5.3; FR 3.5 (word selection).</p>
 *
 * <p>Team 49 Ã¢â‚¬â€œ CS2212B Winter 2026</p>
 * Contributors: Mrida Hingmire
 */
public class WordBank {

    /** All words loaded from the JSON file. */
    private final List<WordEntry> allWords;

    /** Random number generator for word selection. */
    private final Random random;

    /**
     * Creates a WordBank by loading all words from the given data store.
     *
     * @param dataStore the persistence layer to load words from
     */
    public WordBank(FileDataStore dataStore) {
        this.allWords = dataStore.loadWordsJSON();
        this.random   = new Random();
    }

    /**
     * Creates a WordBank from a pre-built word list (useful for testing).
     *
     * @param words the word entries to use
     */
    public WordBank(List<WordEntry> words) {
        this.allWords = words != null ? new ArrayList<>(words) : new ArrayList<>();
        this.random   = new Random();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns a random word text matching the given difficulty string
     * (e.g. "EASY", "MEDIUM", "HARD").
     *
     * @param difficulty the difficulty name (case-insensitive)
     * @return a random word string, or {@code "typing"} as a fallback
     */
    public String getRandomWord(String difficulty) {
        List<WordEntry> filtered = getWordsByDifficulty(difficulty);
        if (filtered.isEmpty()) return "typing";
        return filtered.get(random.nextInt(filtered.size())).getText();
    }

    /**
     * Returns a random {@link WordEntry} matching the given difficulty.
     *
     * @param difficulty the difficulty name (case-insensitive)
     * @return a random word entry, or {@code null} if none match
     */
    public WordEntry getRandomWordEntry(String difficulty) {
        List<WordEntry> filtered = getWordsByDifficulty(difficulty);
        if (filtered.isEmpty()) return null;
        return filtered.get(random.nextInt(filtered.size()));
    }

    /**
     * Returns all words matching the given difficulty.
     *
     * @param difficulty the difficulty to filter by (e.g. "EASY")
     * @return filtered list (may be empty, never {@code null})
     */
    public List<WordEntry> getWordsByDifficulty(String difficulty) {
        return allWords.stream()
                .filter(w -> w.getDifficulty() != null
                        && w.getDifficulty().equalsIgnoreCase(difficulty))
                .collect(Collectors.toList());
    }

    /**
     * Returns all words matching the given category.
     *
     * @param category the category to filter by (e.g. "ANIMALS", "TECH")
     * @return filtered list (may be empty, never {@code null})
     */
    public List<WordEntry> getWordsByCategory(String category) {
        return allWords.stream()
                .filter(w -> w.getCategory() != null
                        && w.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /** Returns the total number of words in the bank. */
    public int getTotalWordCount() {
        return allWords.size();
    }

    /** Returns the number of words for a given difficulty. */
    public int getWordCount(String difficulty) {
        return getWordsByDifficulty(difficulty).size();
    }
}
