package backend.model;

/**
 * Stores the lifetime gameplay statistics associated with a {@link Player}.
 *
 * <p>This model tracks aggregate performance data such as average and peak WPM,
 * typing accuracy, total errors, time played, words typed, highest level reached,
 * and overall high score. These values are updated across sessions and may be
 * reset by guardian-facing account management features.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Misol Kang
 */
public class PlayerStats {

    /** The weighted average of words-per-minute across all sessions. */
    private double averageWPM;

    /** The highest words-per-minute achieved in a single session (Personal Best). */
    private double peakWPM;

    /** The overall accuracy percentage (0-100) across all typed characters. */
    private double accuracyPercent;

    /** Total number of incorrect keystrokes made over the account's lifetime. */
    private int totalErrorCount;

    /** Total duration of active gameplay in seconds. */
    private int totalTimePlayed;

    /** Total number of words successfully completed. */
    private int totalWordsTyped;

    /** The highest numerical level index successfully reached by the player. */
    private int highestLevelReached;

    /** The highest score achieved in any single game mode session. */
    private int overallHighScore;

    /**
     * Creates a new statistics object with all tracked values initialized to zero.
     */
    public PlayerStats() {
        this.averageWPM = 0;
        this.peakWPM = 0;
        this.accuracyPercent = 0;
        this.totalErrorCount = 0;
        this.totalTimePlayed = 0;
        this.totalWordsTyped = 0;
        this.highestLevelReached = 0;
        this.overallHighScore = 0;
    }

    /**
     * Returns the player's lifetime average words per minute.
     *
     * @return the weighted average words-per-minute
     */
    public double getAverageWPM() {
        return this.averageWPM;
    }

    /**
     * Returns the highest words per minute achieved in any session.
     *
     * @return the all-time peak words-per-minute
     */
    public double getPeakWPM() {
        return this.peakWPM;
    }

    /**
     * Returns the player's lifetime typing accuracy percentage.
     *
     * @return the lifetime accuracy percentage from {@code 0} to {@code 100}
     */
    public double getAccuracy() {
        return this.accuracyPercent;
    }

    /**
     * Returns the total number of typing errors recorded across all sessions.
     *
     * @return total cumulative errors made
     */
    public int getErrorCount() {
        return this.totalErrorCount;
    }

    /**
     * Returns the cumulative gameplay time.
     *
     * @return total time played in seconds
     */
    public int getTotalTimePlayed() {
        return this.totalTimePlayed;
    }

    /**
     * Returns the total number of words completed across all sessions.
     *
     * @return total number of words successfully typed
     */
    public int getWordsTyped() {
        return this.totalWordsTyped;
    }

    /**
     * Returns the highest level reached by the player.
     *
     * @return the highest level reached
     */
    public int getHighestLevel() {
        return this.highestLevelReached;
    }

    /**
     * Returns the highest score achieved across all game sessions.
     *
     * @return the all-time highest score across all game modes
     */
    public int getHighScore() {
        return this.overallHighScore;
    }

    /**
     * Sets the player's lifetime average words per minute.
     *
     * @param avgWPM the new average WPM value
     */
    public void setAverageWPM(double avgWPM) {
        this.averageWPM = avgWPM;
    }

    /**
     * Sets the player's peak words per minute.
     *
     * @param peakWPM the new peak WPM value
     */
    public void setPeakWPM(double peakWPM) {
        this.peakWPM = peakWPM;
    }

    /**
     * Sets the player's lifetime accuracy percentage.
     *
     * @param accuracy the updated accuracy percentage
     */
    public void setAccuracy(double accuracy) {
        this.accuracyPercent = accuracy;
    }

    /**
     * Sets the player's cumulative error count.
     *
     * @param errorCount the updated total error count
     */
    public void setErrorCount(int errorCount) {
        this.totalErrorCount = errorCount;
    }

    /**
     * Sets the player's cumulative time played.
     *
     * @param timePlayed the updated total time played in seconds
     */
    public void setTotalTimePlayed(int timePlayed) {
        this.totalTimePlayed = timePlayed;
    }

    /**
     * Sets the player's cumulative completed word count.
     *
     * @param wordsTyped the updated total number of words completed
     */
    public void setWordsTyped(int wordsTyped) {
        this.totalWordsTyped = wordsTyped;
    }

    /**
     * Sets the highest level reached by the player.
     *
     * @param level the new highest level value
     */
    public void setHighestLevel(int level) {
        this.highestLevelReached = level;
    }

    /**
     * Sets the player's overall high score.
     *
     * @param score the new overall high score
     */
    public void setHighScore(int score) {
        this.overallHighScore = score;
    }
}
