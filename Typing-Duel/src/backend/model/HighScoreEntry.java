package backend.model;

/**
 * Represents a single immutable entry in the global high score leaderboard.
 *
 * <p>Each instance stores the player name, recorded score, highest stage reached,
 * and the date the score was achieved. Entries are intended to be serialized as
 * elements of the leaderboard data set and compared by score for ranking.</p>
 *
 * <p>Corresponds to one element of the {@code entries} array in
 * {@code highscores.json}.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public final class HighScoreEntry implements Comparable<HighScoreEntry> {

    private final String username;
    private final int    score;
    private final int    levelReached;
    private final String date;   // ISO-8601 date string e.g. "2026-03-01"

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates an empty entry for Gson deserialization.
     *
     * <p>This constructor is intentionally private because production code should
     * create fully initialized immutable entries through the public constructor.</p>
     */
    @SuppressWarnings("unused")
    private HighScoreEntry() {
        this.username     = null;
        this.score        = 0;
        this.levelReached = 0;
        this.date         = null;
    }

    /**
     * Constructs an immutable leaderboard entry.
     *
     * @param username     the player's username
     * @param score        the score achieved
     * @param levelReached the highest level reached in the session
     * @param date         ISO-8601 date string (e.g. {@code "2026-03-01"})
     */
    public HighScoreEntry(String username, int score, int levelReached, String date) {
        this.username     = username;
        this.score        = score;
        this.levelReached = levelReached;
        this.date         = date;
    }

    // -------------------------------------------------------------------------
    // Comparable Ã¢â‚¬â€œ highest scores sort first
    // -------------------------------------------------------------------------

    /**
     * Compares this entry with another entry for leaderboard ordering.
     *
     * @param other the other entry to compare against
     * @return a negative value if this entry should appear before {@code other},
     *         a positive value if it should appear after, or {@code 0} if the
     *         scores are equal
     */
    @Override
    public int compareTo(HighScoreEntry other) {
        return Integer.compare(other.score, this.score); // descending
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the username associated with this score entry.
     *
     * @return the player's username
     */
    public String getUsername()     { return username; }

    /**
     * Returns the score recorded for this leaderboard entry.
     *
     * @return the score achieved
     */
    public int getScore()           { return score; }

    /**
     * Returns the highest stage or level reached for this score.
     *
     * @return the highest level reached
     */
    public int getLevelReached()    { return levelReached; }

    /**
     * Returns the date associated with this score entry.
     *
     * @return the ISO-8601 date string
     */
    public String getDate()         { return date; }

    /**
     * Returns a string representation of this leaderboard entry for debugging.
     *
     * @return a string containing the username, score, level reached, and date
     */
    @Override
    public String toString() {
        return "HighScoreEntry{username='" + username + "', score=" + score
                + ", levelReached=" + levelReached + ", date='" + date + "'}";
    }
}
