package backend.logic;

import backend.model.Difficulty;
import backend.model.Player;

/**
 * The {@code ProgressionService} class handles all game progression logic
 * for the Typing Duel application.
 *
 * <p>This includes determining whether difficulty levels and special game modes
 * (such as Endless Mode) are unlocked based on a player's performance history.</p>
 *
 * <h2>Core Responsibilities:</h2>
 * <ul>
 *     <li>Determine if Endless Mode is unlocked</li>
 *     <li>Control access to difficulty levels (EASY, MEDIUM, HARD)</li>
 *     <li>Track and update player progression state</li>
 *     <li>Ensure compatibility with legacy save data</li>
 * </ul>
 *
 * <h2>Endless Mode Unlock Logic:</h2>
 * <p>
 * Endless Mode becomes available when the player has achieved a positive score
 * in all three difficulty levels: EASY, MEDIUM, and HARD.
 * </p>
 *
 * <p><b>Functional Requirement:</b> FR 3.5</p>
 * <p>Team 49 – CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public class ProgressionService {

    /** Index representing EASY difficulty (default unlocked). */
    public static final int EASY_STAGE_INDEX = 0;

    /** Index representing MEDIUM difficulty. */
    public static final int MEDIUM_STAGE_INDEX = 1;

    /** Index representing HARD difficulty. */
    public static final int HARD_STAGE_INDEX = 2;

    /**
     * Determines whether Endless Mode is unlocked for a player.
     *
     * <p>Endless Mode is unlocked when the player has recorded at least one
     * positive score for all three standard difficulty levels.</p>
     *
     * @param player the player to check; may be {@code null}
     * @return {@code true} if Endless Mode is unlocked; {@code false} otherwise
     */
    public boolean isEndlessUnlocked(Player player) {
        return player != null
                && player.hasScoreFor(Difficulty.EASY)
                && player.hasScoreFor(Difficulty.MEDIUM)
                && player.hasScoreFor(Difficulty.HARD);
    }

    /**
     * Determines whether a specific difficulty level is unlocked for a player.
     *
     * <p>Unlock rules:</p>
     * <ul>
     *     <li>EASY is always unlocked</li>
     *     <li>MEDIUM unlocks after completing EASY</li>
     *     <li>HARD unlocks after completing MEDIUM</li>
     *     <li>ENDLESS unlocks via {@link #isEndlessUnlocked(Player)}</li>
     * </ul>
     *
     * @param player the player to check; may be {@code null}
     * @param difficulty the difficulty level being queried
     * @return {@code true} if the difficulty is unlocked; {@code false} otherwise
     */
    public boolean isDifficultyUnlocked(Player player, Difficulty difficulty) {
        if (difficulty == null) {
            return false;
        }

        int highestUnlocked = getHighestUnlockedLevel(player);

        return switch (difficulty) {
            case EASY -> true;
            case MEDIUM -> highestUnlocked >= MEDIUM_STAGE_INDEX;
            case HARD -> highestUnlocked >= HARD_STAGE_INDEX;
            case ENDLESS -> isEndlessUnlocked(player);
        };
    }

    /**
     * Returns the highest difficulty level unlocked by the player.
     *
     * <p>This value is determined using both:</p>
     * <ul>
     *     <li>The stored unlock level in the player profile</li>
     *     <li>Inferred progress from score history (for legacy compatibility)</li>
     * </ul>
     *
     * @param player the player to inspect; may be {@code null}
     * @return {@code 0} for EASY, {@code 1} for MEDIUM, {@code 2} for HARD
     */
    public int getHighestUnlockedLevel(Player player) {
        if (player == null) {
            return EASY_STAGE_INDEX;
        }

        int unlockedFromField = Math.max(EASY_STAGE_INDEX,
                Math.min(HARD_STAGE_INDEX, player.getHighestLevelUnlocked()));

        int unlockedFromScores = inferUnlockedLevelFromScores(player);

        return Math.max(unlockedFromField, unlockedFromScores);
    }

    /**
     * Synchronizes the player's stored unlock level with any higher level
     * inferred from their score history.
     *
     * <p>This ensures that older accounts without explicit progression fields
     * are upgraded automatically.</p>
     *
     * @param player the player to update; may be {@code null}
     */
    public void syncUnlockedProgress(Player player) {
        if (player == null) {
            return;
        }

        player.setHighestLevel(getHighestUnlockedLevel(player));
    }

    /**
     * Unlocks the next difficulty level after a player completes a level.
     *
     * <p>Rules:</p>
     * <ul>
     *     <li>Completing EASY unlocks MEDIUM</li>
     *     <li>Completing MEDIUM unlocks HARD</li>
     *     <li>Completing HARD keeps HARD unlocked (max level)</li>
     *     <li>ENDLESS does not affect progression</li>
     * </ul>
     *
     * @param player the player to update; may be {@code null}
     * @param completedDifficulty the difficulty just completed
     */
    public void unlockNextDifficulty(Player player, Difficulty completedDifficulty) {
        if (player == null || completedDifficulty == null) {
            return;
        }

        int currentlyUnlocked = getHighestUnlockedLevel(player);

        int targetUnlock = switch (completedDifficulty) {
            case EASY -> MEDIUM_STAGE_INDEX;
            case MEDIUM, HARD -> HARD_STAGE_INDEX;
            case ENDLESS -> currentlyUnlocked;
        };

        player.setHighestLevel(Math.max(currentlyUnlocked, targetUnlock));
    }

    /**
     * Infers the highest unlocked difficulty based on score history.
     *
     * <p>This method is used to maintain backward compatibility with older
     * player data that may not explicitly store progression levels.</p>
     *
     * @param player the player whose scores are analyzed
     * @return the inferred highest unlocked difficulty index
     */
    private int inferUnlockedLevelFromScores(Player player) {
        if (player.hasScoreFor(Difficulty.MEDIUM) || player.hasScoreFor(Difficulty.HARD)) {
            return HARD_STAGE_INDEX;
        }
        if (player.hasScoreFor(Difficulty.EASY)) {
            return MEDIUM_STAGE_INDEX;
        }
        return EASY_STAGE_INDEX;
    }
}
