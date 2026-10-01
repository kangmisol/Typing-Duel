package backend.logic;

/**
 * The {@code TypingEngine} class is the core backend component responsible for
 * handling real-time typing logic in the Typing Duel application.
 *
 * <p>It compares user input against a target word character-by-character,
 * tracking correctness, errors, timing, and overall performance metrics.</p>
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *     <li>Validate typed characters against a target word</li>
 *     <li>Track typing progress and position</li>
 *     <li>Record errors and correct inputs</li>
 *     <li>Measure timing for words and sessions</li>
 *     <li>Compute performance metrics such as WPM and accuracy</li>
 * </ul>
 *
 * <h2>Design Notes:</h2>
 * <ul>
 *     <li>Implements a backend-only logic layer (no GUI dependencies)</li>
 *     <li>Uses the Observer pattern via {@link TypingListener}</li>
 *     <li>Supports real-time feedback through callbacks</li>
 * </ul>
 *
 * <p><b>Functional Requirement:</b> FR 3.5 (Typing Gameplay)</p>
 * <p>Team 49 – CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public class TypingEngine {

    // -------------------------------------------------------------------------
    // Listener interface (Observer pattern)
    // -------------------------------------------------------------------------

    /**
     * Listener interface for receiving typing-related events.
     *
     * <p>Implemented by the UI layer to react to user input in real time.</p>
     */
    public interface TypingListener {

        /**
         * Invoked when the user types the correct character.
         *
         * @param position the index of the correctly typed character (0-based)
         * @param expected the expected character
         */
        void onCorrectChar(int position, char expected);

        /**
         * Invoked when the user types an incorrect character.
         *
         * @param position the index of the expected character (0-based)
         * @param expected the expected character
         * @param actual   the character typed by the user
         */
        void onIncorrectChar(int position, char expected, char actual);

        /**
         * Invoked when the user completes typing the entire word.
         *
         * @param word       the completed word
         * @param elapsedMs  time taken in milliseconds
         * @param errorCount number of errors made while typing the word
         */
        void onWordCompleted(String word, long elapsedMs, int errorCount);
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** The current target word to be typed. */
    private String targetWord;

    /** Index of the next expected character. */
    private int currentIndex;

    /** Number of errors made on the current word. */
    private int errorCount;

    /** Total correct characters typed during the session. */
    private int totalCorrectChars;

    /** Total incorrect keystrokes during the session. */
    private int totalErrors;

    /** Number of words successfully completed. */
    private int wordsCompleted;

    /** Timestamp when the current word was loaded. */
    private long wordStartTimeMs;

    /** Timestamp when the session started. */
    private long sessionStartTimeMs;

    /** Optional listener for handling typing events. */
    private TypingListener listener;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code TypingEngine} with all counters initialized to zero.
     */
    public TypingEngine() {
        this.currentIndex = 0;
        this.errorCount = 0;
        this.totalCorrectChars = 0;
        this.totalErrors = 0;
        this.wordsCompleted = 0;
        this.sessionStartTimeMs = 0;
    }

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    /**
     * Registers a {@link TypingListener} to receive typing event callbacks.
     *
     * @param listener the listener instance; may be {@code null}
     */
    public void setListener(TypingListener listener) {
        this.listener = listener;
    }

    // -------------------------------------------------------------------------
    // Core API
    // -------------------------------------------------------------------------

    /**
     * Loads a new target word and resets per-word tracking state.
     *
     * <p>This method should be called before processing input for a new word.</p>
     *
     * @param word the word to be typed; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code word} is null or empty
     */
    public void loadWord(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Target word must not be null or empty");
        }

        this.targetWord = word;
        this.currentIndex = 0;
        this.errorCount = 0;
        this.wordStartTimeMs = System.currentTimeMillis();

        if (sessionStartTimeMs == 0) {
            sessionStartTimeMs = wordStartTimeMs;
        }
    }

    /**
     * Processes a character typed by the player.
     *
     * <p>Compares the input character with the expected character at the current index.
     * Updates counters and notifies the listener accordingly.</p>
     *
     * @param typed the character entered by the user
     * @return {@code true} if the character is correct; {@code false} otherwise
     */
    public boolean processChar(char typed) {
        if (targetWord == null || currentIndex >= targetWord.length()) {
            return false;
        }

        char expected = targetWord.charAt(currentIndex);

        if (Character.toLowerCase(typed) == Character.toLowerCase(expected)) {
            totalCorrectChars++;

            if (listener != null) {
                listener.onCorrectChar(currentIndex, expected);
            }

            currentIndex++;

            if (currentIndex >= targetWord.length()) {
                wordsCompleted++;
                long elapsed = System.currentTimeMillis() - wordStartTimeMs;

                if (listener != null) {
                    listener.onWordCompleted(targetWord, elapsed, errorCount);
                }
            }
            return true;

        } else {
            errorCount++;
            totalErrors++;

            if (listener != null) {
                listener.onIncorrectChar(currentIndex, expected, typed);
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /**
     * Checks whether the current word has been fully typed.
     *
     * @return {@code true} if the word is complete; {@code false} otherwise
     */
    public boolean isWordComplete() {
        return targetWord != null && currentIndex >= targetWord.length();
    }

    /** @return the current target word */
    public String getTargetWord() { return targetWord; }

    /** @return the current typing index */
    public int getCurrentIndex() { return currentIndex; }

    /** @return number of errors for the current word */
    public int getErrorCount() { return errorCount; }

    /** @return length of the target word */
    public int getWordLength() { return targetWord != null ? targetWord.length() : 0; }

    /** @return total correct characters typed */
    public int getTotalCorrectChars() { return totalCorrectChars; }

    /** @return total errors made */
    public int getTotalErrors() { return totalErrors; }

    /** @return number of completed words */
    public int getWordsCompleted() { return wordsCompleted; }

    // -------------------------------------------------------------------------
    // Statistics
    // -------------------------------------------------------------------------

    /**
     * Calculates the current Words Per Minute (WPM).
     *
     * <p>Uses the standard definition of 5 characters per word.</p>
     *
     * @return the calculated WPM, or {@code 0} if insufficient data
     */
    public double calculateWPM() {
        if (sessionStartTimeMs == 0 || totalCorrectChars == 0) return 0;

        long elapsed = System.currentTimeMillis() - sessionStartTimeMs;
        if (elapsed <= 0) return 0;

        double minutes = elapsed / 60000.0;
        return (totalCorrectChars / 5.0) / minutes;
    }

    /**
     * Calculates the player's typing accuracy.
     *
     * @return accuracy percentage (0–100), or {@code 100} if no input yet
     */
    public double calculateAccuracy() {
        int totalAttempts = totalCorrectChars + totalErrors;
        if (totalAttempts == 0) return 100.0;
        return (totalCorrectChars * 100.0) / totalAttempts;
    }

    /**
     * Returns elapsed time for the current word.
     *
     * @return elapsed time in milliseconds
     */
    public long getWordElapsedMs() {
        if (wordStartTimeMs == 0) return 0;
        return System.currentTimeMillis() - wordStartTimeMs;
    }

    /**
     * Returns total elapsed time for the session.
     *
     * @return elapsed time in milliseconds
     */
    public long getSessionElapsedMs() {
        if (sessionStartTimeMs == 0) return 0;
        return System.currentTimeMillis() - sessionStartTimeMs;
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Resets the entire typing session, clearing all state and counters.
     *
     * <p>This should be called when starting a new game.</p>
     */
    public void resetSession() {
        this.targetWord = null;
        this.currentIndex = 0;
        this.errorCount = 0;
        this.totalCorrectChars = 0;
        this.totalErrors = 0;
        this.wordsCompleted = 0;
        this.wordStartTimeMs = 0;
        this.sessionStartTimeMs = 0;
    }
}
