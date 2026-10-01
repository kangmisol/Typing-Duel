package backend.model;

/**
 * Stores aggregate gameplay statistics for a player or session summary.
 *
 * <p>This model tracks typing performance metrics such as average and peak WPM,
 * accuracy, error totals, time played, words typed, levels reached, and total
 * score. It acts as a simple data container for transferring summarized
 * statistics between backend services and other parts of the application.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 * @author Atika Hussain
 */
public class Statistics {
    private double averageWpm;
    private double peakWpm;
    private double accuracy;
    private int errorCount;
    private int totalTimePlayedSeconds;
    private int wordsTyped;
    private int levelsReached;
    private int totalScore;

    /**
     * Returns the average typing speed in words per minute.
     *
     * @return the average WPM value
     */
    public double getAverageWpm() {
        return averageWpm;
    }

    /**
     * Sets the average typing speed in words per minute.
     *
     * @param averageWpm the average WPM value to store
     */
    public void setAverageWpm(double averageWpm) {
        this.averageWpm = averageWpm;
    }

    /**
     * Returns the highest typing speed achieved in words per minute.
     *
     * @return the peak WPM value
     */
    public double getPeakWpm() {
        return peakWpm;
    }

    /**
     * Sets the highest typing speed achieved in words per minute.
     *
     * @param peakWpm the peak WPM value to store
     */
    public void setPeakWpm(double peakWpm) {
        this.peakWpm = peakWpm;
    }

    /**
     * Returns the typing accuracy percentage.
     *
     * @return the accuracy percentage
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the typing accuracy percentage.
     *
     * @param accuracy the accuracy percentage to store
     */
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Returns the total number of typing errors recorded.
     *
     * @return the cumulative error count
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Sets the total number of typing errors recorded.
     *
     * @param errorCount the cumulative error count to store
     */
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * Returns the total time played in seconds.
     *
     * @return the cumulative time played in seconds
     */
    public int getTotalTimePlayedSeconds() {
        return totalTimePlayedSeconds;
    }

    /**
     * Sets the total time played in seconds.
     *
     * @param totalTimePlayedSeconds the cumulative time played in seconds to store
     */
    public void setTotalTimePlayedSeconds(int totalTimePlayedSeconds) {
        this.totalTimePlayedSeconds = totalTimePlayedSeconds;
    }

    /**
     * Returns the total number of words typed.
     *
     * @return the cumulative completed word count
     */
    public int getWordsTyped() {
        return wordsTyped;
    }

    /**
     * Sets the total number of words typed.
     *
     * @param wordsTyped the cumulative completed word count to store
     */
    public void setWordsTyped(int wordsTyped) {
        this.wordsTyped = wordsTyped;
    }

    /**
     * Returns the number of levels reached.
     *
     * @return the recorded levels reached value
     */
    public int getLevelsReached() {
        return levelsReached;
    }

    /**
     * Sets the number of levels reached.
     *
     * @param levelsReached the levels reached value to store
     */
    public void setLevelsReached(int levelsReached) {
        this.levelsReached = levelsReached;
    }

    /**
     * Returns the total score represented by this statistics object.
     *
     * @return the total score value
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Sets the total score represented by this statistics object.
     *
     * @param totalScore the total score value to store
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
