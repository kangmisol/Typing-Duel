package backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player account in the Typing Duel system.
 *
 * <p>This model stores account credentials, account type, password recovery
 * information, progression state, and score/statistics data associated with a
 * single user. It acts as the central persistent profile object used by the
 * backend authentication, progression, and leaderboard systems.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Misol Kang
 */
public class Player {

    /** The unique identifier for the user. */
    private String username;

    /** The plain-text password for authentication (FR 3.1.6). */
    private String password;

    /** The privilege level of the account (PLAYER or PARENT_TEACHER). */
    private AccountType accountType;

    /** Security questions used for the "Forgot Password" workflow (FR 3.1.14). */
    private String[] securityQuestions;

    /** Corresponding answers for password recovery (FR 3.1.14). */
    private String[] securityAnswers;

    /** Extra verification code for guardian accounts. */
    private String guardianCode;

    /** Detailed lifetime performance metrics for the player (FR 3.1.7). */
    private PlayerStats stats;

    /** The highest level index currently unlocked by the player. */
    private int highestLevelUnlocked;

    /** A list of high scores achieved, categorized by difficulty tier. */
    private List<Score> scores;

    /**
     * Creates a new player profile with default starting state.
     *
     * @param username the player's unique username
     * @param password the player's initial password
     * @param accountType the account role assigned to the player
     */
    public Player(String username, String password, AccountType accountType) {
        this.username = username;
        this.password = password;
        this.stats = new PlayerStats();
        this.accountType = accountType;
        this.highestLevelUnlocked = 0;
        this.scores = new ArrayList<>();
    }

    /**
     * Returns the player's username.
     *
     * @return the user's unique username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Returns the player's password.
     *
     * @return the user's current password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the account type assigned to the player.
     *
     * @return the player's {@link AccountType}
     */
    public AccountType getAccountType() {
        return this.accountType;
    }

    /**
     * Returns the security questions configured for password recovery.
     *
     * @return the array of security questions, or {@code null} if unset
     */
    public String[] getSecurityQs() {
        return this.securityQuestions;
    }

    /**
     * Returns the security answers configured for password recovery.
     *
     * @return the array of security answers, or {@code null} if unset
     */
    public String[] getSecurityAs() {
        return this.securityAnswers;
    }

    /**
     * Returns the guardian verification code for parent or teacher accounts.
     *
     * @return the guardian verification code, or {@code null} if unset
     */
    public String getGuardianCode() {
        return this.guardianCode;
    }

    /**
     * Returns the player's lifetime statistics object.
     *
     * @return the {@link PlayerStats} associated with this player
     */
    public PlayerStats getStats() {
        return this.stats;
    }

    /**
     * Returns the highest unlocked progression level.
     *
     * @return the highest level index currently unlocked
     */
    public int getHighestLevelUnlocked() {
        return this.highestLevelUnlocked;
    }

    /**
     * Returns the list of recorded scores for this player.
     *
     * @return the player's score list
     */
    public List<Score> getScores() {
        return this.scores;
    }

    /**
     * Updates the player's username.
     *
     * @param username the new username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Updates the player's password.
     *
     * @param password the new password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Updates the player's account type.
     *
     * @param accountType the new {@link AccountType} to assign
     */
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    /**
     * Deep-copies the provided questions into the player's profile.
     *
     * @param securityQs array of security questions to store
     */
    public void setSecurityQs(String[] securityQs) {
        if (securityQs == null) return;
        this.securityQuestions = new String[securityQs.length];
        System.arraycopy(securityQs, 0, this.securityQuestions, 0, securityQs.length);
    }

    /**
     * Deep-copies the provided answers into the player's profile.
     *
     * @param securityAs array of security answers to store
     */
    public void setSecurityAs(String[] securityAs) {
        if (securityAs == null) return;
        this.securityAnswers = new String[securityAs.length];
        System.arraycopy(securityAs, 0, this.securityAnswers, 0, securityAs.length);
    }

    /**
     * Sets the guardian verification code for the account.
     *
     * @param guardianCode the guardian verification code to set
     */
    public void setGuardianCode(String guardianCode) {
        this.guardianCode = guardianCode;
    }

    /**
     * Sets the highest unlocked progression level for the player.
     *
     * @param level the highest level index to store
     */
    public void setHighestLevel(int level) {
        this.highestLevelUnlocked = level;
    }

    /**
     * Updates or inserts the player's score for a given difficulty tier.
     *
     * @param difficulty the difficulty tier to update
     * @param value the score value to record
     */
    public void addOrReplaceScore(Difficulty difficulty, int value) {
        for (Score score : scores) {
            if (score.getDifficulty() == difficulty) {
                score.setValue(value);
                return;
            }
        }
        scores.add(new Score(difficulty, value));
    }

    /**
     * Returns whether the player has a positive score recorded for a difficulty.
     *
     * @param difficulty the difficulty tier to check
     * @return {@code true} if a score exists for the difficulty and is greater
     *         than zero; {@code false} otherwise
     */
    public boolean hasScoreFor(Difficulty difficulty) {
        for (Score score : scores) {
            if ((score.getDifficulty() == difficulty) && score.getValue() > 0) {
                return true;
            }
        }
        return false;
    }
}
