package backend.logic;

/**
 * Coordinates attack and defend phase flow for a Typing Duel match.
 *
 * <p>This class tracks the active {@link GamePhase}, manages player and opponent
 * health, applies queued {@link PowerUp} effects, and resolves the combat result
 * of a completed typing phase. It centralizes the mode-independent match rules
 * so controllers can drive gameplay without embedding combat calculations in the
 * GUI layer.</p>
 *
 * <p>In standard play, successful attack phases damage the opponent and failed
 * defend phases damage the player. In endless mode, the same phase structure is
 * preserved, but opponent depletion is skipped so combat can continue across an
 * unlimited number of waves.</p>
 *
 * <p>Design Documentation Section 5.4; FR 3.5.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Misol Kang
 */
public class PhaseManager {

    /**
     * Immutable summary of the outcome of resolving a single attack or defend phase.
     *
     * <p>This object allows callers to inspect the result of phase resolution
     * without recalculating damage from mutable match state.</p>
     *
     * @author Misol Kang
     */
    public static final class PhaseResolution {
        private final GamePhase phase;
        private final boolean successful;
        private final PowerUp powerUpUsed;
        private final int playerDamageTaken;
        private final int opponentDamageTaken;
        private final boolean damageCancelled;

        /**
         * Creates a new resolution summary for a completed phase.
         *
         * @param phase the phase that was resolved
         * @param successful whether the player completed the word successfully
         * @param powerUpUsed the power-up consumed during resolution, or {@link PowerUp#NONE}
         * @param playerDamageTaken damage dealt to the player during this phase
         * @param opponentDamageTaken damage dealt to the opponent during this phase
         * @param damageCancelled whether an active effect cancelled incoming damage
         */
        public PhaseResolution(GamePhase phase, boolean successful, PowerUp powerUpUsed,
                               int playerDamageTaken, int opponentDamageTaken, boolean damageCancelled) {
            this.phase = phase;
            this.successful = successful;
            this.powerUpUsed = powerUpUsed;
            this.playerDamageTaken = playerDamageTaken;
            this.opponentDamageTaken = opponentDamageTaken;
            this.damageCancelled = damageCancelled;
        }

        /**
         * Returns the phase whose effect was resolved.
         *
         * @return the resolved phase
         */
        public GamePhase getPhase() {
            return phase;
        }

        /**
         * Returns whether the player completed the word successfully.
         *
         * @return {@code true} if the phase objective succeeded; {@code false} otherwise
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * Returns the power-up used during the phase.
         *
         * @return the consumed power-up, or {@link PowerUp#NONE} when none was active
         */
        public PowerUp getPowerUpUsed() {
            return powerUpUsed;
        }

        /**
         * Returns the damage dealt to the player.
         *
         * @return player damage applied during this phase
         */
        public int getPlayerDamageTaken() {
            return playerDamageTaken;
        }

        /**
         * Returns the damage dealt to the opponent.
         *
         * @return opponent damage applied during this phase
         */
        public int getOpponentDamageTaken() {
            return opponentDamageTaken;
        }

        /**
         * Returns whether a defensive effect cancelled incoming damage.
         *
         * @return {@code true} if damage was cancelled; {@code false} otherwise
         */
        public boolean isDamageCancelled() {
            return damageCancelled;
        }
    }

    // -------------------------------------------------------------------------
    // State Constants
    // -------------------------------------------------------------------------

    /**
     * Represents the two alternating combat phases in a match.
     */
    public enum GamePhase {
        /** Phase where successful typing damages the opponent. */
        ATTACK,
        /** Phase where failed typing allows the opponent to damage the player. */
        DEFEND
    }

    /**
     * Represents power-up effects that can modify the next phase resolution.
     */
    public enum PowerUp {
        /** No power-up is currently active. */
        NONE,
        /** Attack-phase boost that strengthens a successful offensive action. */
        FLURRY_RUSH,
        /** Defensive shield that cancels the next incoming damage event. */
        BRICK_WALL
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private GamePhase currentPhase;
    private final TypingEngine typingEngine;
    private int playerHp;
    private int opponentHp;
    private PowerUp activePowerUp = PowerUp.NONE;

    /** Maximum HP available to both combatants at the start of a match. */
    private static final int MAX_HP = 100;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a phase manager for a new match.
     *
     * <p>The manager starts in {@link GamePhase#ATTACK} with both combatants at
     * full health and no active power-up.</p>
     *
     * @param engine the typing engine associated with the active game session
     */
    public PhaseManager(TypingEngine engine) {
        this.typingEngine = engine;
        this.currentPhase = GamePhase.ATTACK;
        this.playerHp = MAX_HP;
        this.opponentHp = MAX_HP;
    }

    // -------------------------------------------------------------------------
    // Core Phase Logic
    // -------------------------------------------------------------------------

    /**
     * Swaps the current phase from attack to defend or from defend to attack.
     *
     * @return the newly active phase after toggling
     */
    public GamePhase togglePhase() {
        if (this.currentPhase == GamePhase.ATTACK) {
            this.currentPhase = GamePhase.DEFEND;
        } else {
            this.currentPhase = GamePhase.ATTACK;
        }
        return this.currentPhase;
    }

    /**
     * Resolves the current phase and applies any HP changes caused by the outcome.
     *
     * <p>This convenience method performs the same state updates as
     * {@link #resolvePhaseEffectDetailed(boolean, int, boolean)} but discards the
     * detailed result object when the caller only needs side effects.</p>
     *
     * @param isSuccessful whether the player completed the word before time expired
     * @param wordPointValue the word's base value, used as damage during resolution
     * @param isEndlessMode whether endless-mode combat rules should be applied
     */
    public void resolvePhaseEffect(boolean isSuccessful, int wordPointValue, boolean isEndlessMode) {
        resolvePhaseEffectDetailed(isSuccessful, wordPointValue, isEndlessMode);
    }

    /**
     * Resolves the current phase and returns a detailed summary of the outcome.
     *
     * <p>The active power-up is consumed after this method runs, regardless of
     * whether the player succeeded or failed.</p>
     *
     * @param isSuccessful whether the player completed the word before time expired
     * @param wordPointValue the word's base value, used as damage during resolution
     * @param isEndlessMode whether endless-mode combat rules should be applied
     * @return an immutable summary of the resolved phase outcome
     */
    public PhaseResolution resolvePhaseEffectDetailed(boolean isSuccessful, int wordPointValue, boolean isEndlessMode) {
        PowerUp powerUpUsed = activePowerUp;
        PhaseResolution resolution;

        if (currentPhase == GamePhase.ATTACK) {
            resolution = handleAttackOutcome(isSuccessful, wordPointValue, isEndlessMode, powerUpUsed);
        } else {
            resolution = handleDefendOutcome(isSuccessful, wordPointValue, isEndlessMode, powerUpUsed);
        }

        // Power-ups are consumed after one phase interaction regardless of outcome.
        clearPowerUp();
        return resolution;
    }

    /**
     * Applies attack-phase combat rules for a resolved word.
     *
     * @param success whether the player completed the attack word successfully
     * @param damage the base damage value associated with the word
     * @param isEndless whether endless-mode rules are active
     * @param powerUpUsed the power-up consumed for this phase
     * @return a summary of the attack-phase outcome
     */
    private PhaseResolution handleAttackOutcome(boolean success, int damage, boolean isEndless, PowerUp powerUpUsed) {
        int opponentDamageTaken = 0;

        if (success) {
            int actualDamage = damage;

            if (powerUpUsed == PowerUp.FLURRY_RUSH) {
                actualDamage *= 2;
                if (isEndless) {
                    restorePlayerHp(damage);
                }
            }

            if (!isEndless) {
                opponentHp = Math.max(0, opponentHp - actualDamage);
                opponentDamageTaken = actualDamage;
            }
        }

        return new PhaseResolution(GamePhase.ATTACK, success, powerUpUsed, 0, opponentDamageTaken, false);
    }

    /**
     * Applies defend-phase combat rules for a resolved word.
     *
     * @param success whether the player completed the defend word successfully
     * @param incomingDamage the damage value applied when the defend attempt fails
     * @param isEndless whether endless-mode rules are active
     * @param powerUpUsed the power-up consumed for this phase
     * @return a summary of the defend-phase outcome
     */
    private PhaseResolution handleDefendOutcome(boolean success, int incomingDamage, boolean isEndless, PowerUp powerUpUsed) {
        if (powerUpUsed == PowerUp.BRICK_WALL) {
            return new PhaseResolution(GamePhase.DEFEND, success, powerUpUsed, 0, 0, true);
        }

        int playerDamageTaken = 0;
        if (!success) {
            playerHp = Math.max(0, playerHp - incomingDamage);
            playerDamageTaken = incomingDamage;
        }

        return new PhaseResolution(GamePhase.DEFEND, success, powerUpUsed, playerDamageTaken, 0, false);
    }

    // -------------------------------------------------------------------------
    // Power-up Management
    // -------------------------------------------------------------------------

    /**
     * Queues a power-up to be applied to the next resolved phase.
     *
     * @param powerUp the power-up to activate
     */
    public void activatePowerUp(PowerUp powerUp) {
        this.activePowerUp = powerUp;
    }

    /**
     * Clears any queued power-up so the next phase resolves normally.
     */
    public void clearPowerUp() {
        this.activePowerUp = PowerUp.NONE;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the phase currently active for the match.
     *
     * @return the current attack or defend phase
     */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Returns the power-up that will affect the next phase resolution.
     *
     * @return the active power-up, or {@link PowerUp#NONE} when none is queued
     */
    public PowerUp getActivePowerUp() {
        return activePowerUp;
    }

    /**
     * Returns the player's current HP.
     *
     * @return the player's remaining health
     */
    public int getPlayerHp() {
        return playerHp;
    }

    /**
     * Returns the opponent's current HP.
     *
     * @return the opponent's remaining health
     */
    public int getOpponentHp() {
        return opponentHp;
    }

    /**
     * Restores player HP without exceeding {@link #MAX_HP}.
     *
     * @param amount the amount of health to restore
     */
    private void restorePlayerHp(int amount) {
        if (amount <= 0) {
            return;
        }
        playerHp = Math.min(MAX_HP, playerHp + amount);
    }

    /**
     * Returns whether the current match has ended.
     *
     * @return {@code true} if the player or opponent has zero HP; {@code false} otherwise
     */
    public boolean isGameOver() {
        return playerHp <= 0 || opponentHp <= 0;
    }

    /**
     * Restores the opponent to full HP for a new round or endless continuation.
     */
    public void replenishOpponentHp() {
        this.opponentHp = MAX_HP;
    }

    /**
     * Resets phase, HP, and queued power-ups for a new match.
     */
    public void reset() {
        this.playerHp = MAX_HP;
        this.opponentHp = MAX_HP;
        this.currentPhase = GamePhase.ATTACK;
        this.activePowerUp = PowerUp.NONE;
    }
}
