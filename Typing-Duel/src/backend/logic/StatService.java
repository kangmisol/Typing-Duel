package backend.logic;

import backend.model.Player;
import backend.model.PlayerStats;
import backend.storage.FileDataStore;

/**
 * The {@code StatService} class is responsible for managing player performance
 * statistics across game sessions.
 *
 * <p>This service aggregates real-time gameplay data from the {@code TypingEngine}
 * and updates the player's long-term statistics stored in {@link PlayerStats}.
 * It also ensures that all updates are persisted to storage immediately.</p>
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *     <li>Aggregate session-based performance metrics into lifetime statistics</li>
 *     <li>Track high scores and progression levels</li>
 *     <li>Maintain weighted averages for WPM and accuracy</li>
 *     <li>Persist updated player data to prevent data loss</li>
 * </ul>
 *
 * <h2>Tracked Metrics:</h2>
 * <ul>
 *     <li><b>WPM:</b> Tracks both peak (personal best) and weighted average speed</li>
 *     <li><b>Accuracy:</b> Lifetime accuracy based on weighted session performance</li>
 *     <li><b>Volume:</b> Total words typed, errors made, and time played</li>
 *     <li><b>Progression:</b> High scores and highest level reached</li>
 * </ul>
 *
 * <p><b>Functional Requirements:</b> FR 3.1.7, FR 3.1.13</p>
 * <p><b>Non-Functional Requirement:</b> NFR 3.2.15</p>
 *
 * <p>Team 49 — CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 * @author Misol Kang
 * @author Atika Hussain
 */
public class StatService {

    /** The storage handler used to persist updated player profiles. */
    private final FileDataStore dataStore;

    /**
     * Constructs a {@code StatService} with the specified data store.
     *
     * @param dataStore the persistence layer used to save player data;
     *                  must not be {@code null}
     */
    public StatService(FileDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Updates a player's lifetime statistics based on the results of a completed session.
     *
     * <p>This method performs the following operations:</p>
     * <ul>
     *     <li>Updates cumulative totals (words typed, errors, time played)</li>
     *     <li>Updates high score and highest level reached</li>
     *     <li>Tracks peak WPM (personal best)</li>
     *     <li>Recalculates weighted averages for WPM and accuracy</li>
     *     <li>Persists updated data to storage</li>
     * </ul>
     *
     * @param player         the player whose statistics are being updated
     * @param sessionEngine  the typing engine containing session performance data
     * @param sessionScore   the score achieved in the session
     * @param sessionTimeSec the duration of the session in seconds
     * @param levelReached   the highest level reached during the session
     */
    public void updatePlayerStats(Player player, TypingEngine sessionEngine,
                                  int sessionScore, int sessionTimeSec, int levelReached) {

        PlayerStats stats = player.getStats();

        // 1. Update cumulative totals
        stats.setWordsTyped(stats.getWordsTyped() + sessionEngine.getWordsCompleted());
        stats.setErrorCount(stats.getErrorCount() + sessionEngine.getTotalErrors());
        stats.setTotalTimePlayed(stats.getTotalTimePlayed() + sessionTimeSec);

        // 2. Update high score and progression
        if (sessionScore > stats.getHighScore()) {
            stats.setHighScore(sessionScore);
        }

        if (levelReached > stats.getHighestLevel()) {
            stats.setHighestLevel(levelReached);
            player.setHighestLevel(levelReached);
        }

        // 3. Update peak WPM (personal best)
        double sessionWPM = sessionEngine.calculateWPM();
        if (sessionWPM > stats.getPeakWPM()) {
            stats.setPeakWPM(sessionWPM);
        }

        // 4. Update weighted average WPM
        updateAverageWPM(stats, sessionWPM, sessionEngine.getWordsCompleted());

        // 5. Update lifetime accuracy
        double sessionAccuracy = sessionEngine.calculateAccuracy();
        updateAverageAccuracy(stats, sessionAccuracy, sessionEngine.getWordsCompleted());

        // Persist updated player data immediately
        dataStore.savePlayer(player);
    }

    /**
     * Updates the player's average Words Per Minute (WPM) using a weighted average.
     *
     * <p>This method ensures that longer sessions have a greater impact on the
     * overall average than shorter sessions.</p>
     *
     * <p>Formula:</p>
     * <pre>
     * ((OldAverage * OldWordCount) + (SessionWPM * SessionWordCount)) / TotalWordCount
     * </pre>
     *
     * @param stats        the player's statistics object to update
     * @param sessionWPM   the WPM achieved during the session
     * @param sessionWords the number of words typed during the session
     */
    private void updateAverageWPM(PlayerStats stats, double sessionWPM, int sessionWords) {
        if (sessionWords == 0) return;

        double currentAvg = stats.getAverageWPM();
        int totalWords = stats.getWordsTyped();

        if (totalWords == sessionWords) {
            stats.setAverageWPM(sessionWPM);
        } else {
            double newAvg = ((currentAvg * (totalWords - sessionWords))
                    + (sessionWPM * sessionWords)) / totalWords;
            stats.setAverageWPM(newAvg);
        }
    }

    /**
     * Updates the player's lifetime typing accuracy using a weighted average.
     *
     * <p>This ensures that sessions with more words contribute more significantly
     * to the overall accuracy metric.</p>
     *
     * @param stats           the player's statistics object to update
     * @param sessionAccuracy the accuracy achieved during the session (0–100)
     * @param sessionWords    the number of words typed during the session
     */
    private void updateAverageAccuracy(PlayerStats stats, double sessionAccuracy, int sessionWords) {
        if (sessionWords == 0) return;

        double currentAcc = stats.getAccuracy();
        int totalWords = stats.getWordsTyped();

        if (totalWords == sessionWords) {
            stats.setAccuracy(sessionAccuracy);
        } else {
            double newAcc = ((currentAcc * (totalWords - sessionWords))
                    + (sessionAccuracy * sessionWords)) / totalWords;
            stats.setAccuracy(newAcc);
        }
    }
}
