package backend.model;

/**
 * Represents a score record for a specific difficulty tier.
 *
 * <p>These objects are stored within a {@link Player} profile to track the
 * player's recorded score for each supported {@link Difficulty}. A score pairs
 * a difficulty value with the numeric points achieved for that tier.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Misol Kang
 */
public class Score {

    /** The difficulty tier associated with this score (EASY, MEDIUM, or HARD). */
    private Difficulty difficulty;

    /** The numerical point value achieved. */
    private int value;

    /**
     * Creates a new score record for the given difficulty.
     *
     * @param difficulty the {@link Difficulty} tier associated with this score
     * @param value the numeric score value achieved
     */
    public Score(Difficulty difficulty, int value) {
        this.difficulty = difficulty;
        this.value = value;
    }

    /**
     * Returns the difficulty tier associated with this score.
     *
     * @return the difficulty tier of this score record
     */
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    /**
     * Updates the difficulty tier associated with this score.
     *
     * @param difficulty the difficulty tier to associate with this record
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Returns the numeric value of this score.
     *
     * @return the numerical value of the score
     */
    public int getValue() {
        return value;
    }

    /**
     * Updates the numeric value stored in this score record.
     *
     * @param value the new numerical value to set for this record
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns a compact string representation of this score.
     *
     * @return the score formatted as {@code DIFFICULTY:VALUE}
     */
    @Override
    public String toString() {
        return difficulty + ":" + value;
    }
}
