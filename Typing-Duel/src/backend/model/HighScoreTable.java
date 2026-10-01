package backend.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and maintains the global high score leaderboard.
 *
 * <p>This class keeps leaderboard entries sorted in descending score order,
 * limits the table to {@value #MAX_ENTRIES} records, and provides the core
 * operations used to qualify, insert, reset, and inspect leaderboard data.</p>
 *
 * <p>Design Documentation Section 5.2; FR 3.9.3.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 * @author Atika Hussain
 */
public class HighScoreTable {

    /** Maximum number of entries kept on the leaderboard (FR 3.9.3). */
    public static final int MAX_ENTRIES = 10;

    /** Ordered list of high score entries (highest score first). */
    private List<HighScoreEntry> entries;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates an empty leaderboard table.
     *
     * <p>This constructor is also used by Gson during deserialization before the
     * entry list is populated from persisted data.</p>
     */
    public HighScoreTable() {
        this.entries = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Business logic
    // -------------------------------------------------------------------------

    /**
     * Attempts to add a new entry to the leaderboard. The entry is inserted
     * only if it qualifies (see {@link #qualifies(int)}). After insertion the
     * list is re-sorted and trimmed to {@value #MAX_ENTRIES}.
     *
     * @param entry the new entry to consider
     * @return {@code true} if the entry was added; {@code false} if the entry
     *         was {@code null} or did not qualify
     */
    public boolean addEntry(HighScoreEntry entry) {
        if (entry == null) return false;
        if (!qualifies(entry.getScore())) return false;

        entries.add(entry);
        Collections.sort(entries);

        // Trim to max entries
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        return true;
    }

    /**
     * Checks cheaply whether a given score would make it onto the leaderboard,
     * without constructing a full {@link HighScoreEntry}.
     *
     * @param score the score to test
     * @return {@code true} if the score would be added to the table;
     *         {@code false} otherwise
     */
    public boolean qualifies(int score) {
        if (entries.size() < MAX_ENTRIES) return true;
        return score > entries.get(entries.size() - 1).getScore();
    }

    /**
     * Resets the leaderboard to a list of default placeholder entries.
     * Called by the Parent/Teacher "Clear Global High Scores" action (FR 3.7).
     *
     * @param defaults ordered list of default entries to install after clearing
     */
    public void reset(List<HighScoreEntry> defaults) {
        entries.clear();
        if (defaults != null) entries.addAll(defaults);
        Collections.sort(entries);
    }

    /**
     * Resets the leaderboard to empty (no default placeholders).
     */
    public void resetToEmpty() {
        entries.clear();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable view of the current leaderboard entries.
     *
     * @return read-only list, highest score first
     */
    public List<HighScoreEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Replaces the entries list (used by Gson during deserialization).
     *
     * @param entries the deserialized entry list
     */
    public void setEntries(List<HighScoreEntry> entries) {
        this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        Collections.sort(this.entries);
    }

    /**
     * Returns the number of entries currently stored in the leaderboard.
     *
     * @return the current leaderboard size
     */
    public int size() { return entries.size(); }

    /**
     * Returns a compact string representation of the leaderboard for debugging.
     *
     * @return a string containing the current entry count and leaderboard capacity
     */
    @Override
    public String toString() {
        return "HighScoreTable{entries=" + entries.size() + "/" + MAX_ENTRIES + "}";
    }
}
