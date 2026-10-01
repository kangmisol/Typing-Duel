package backend.logic;

import backend.model.Difficulty;

/**
 * EndlessMode.java
 * <p>
 * Encapsulates all backend logic for the Endless game mode (FR 3.1.14 -
 * extra functional requirement). Endless mode reuses the regular gameplay
 * screen with the following modified rules (Design Doc Section 4.2.5):
 * </p>
 *
 * <ul>
 *   <li><strong>Survival scoring.</strong> The player earns points for every
 *       successful attack or defend phase. Points scale with the current stage
 *       so later phases are worth more, rewarding endurance.</li>
 *   <li><strong>Progressive difficulty.</strong> Every
 *       {@value #PHASES_PER_STAGE} phases the stage advances, which moves the
 *       active {@link Difficulty} tier upward (EASY -> MEDIUM -> HARD) and then
 *       increases an intra-HARD speed multiplier. {@link #getCurrentDifficulty()}
 *       returns the tier the {@link WordBank} should use for
 *       word selection on the current phase.</li>
 *   <li><strong>Run ends</strong> when the player runs out of lives or chooses
 *       to exit. {@link #endSession()} marks the session inactive.</li>
 * </ul>
 *
 * <h2>Turn structure</h2>
 * <p>
 * The game loop calls {@link #onPhaseCompleted(int, long, int)} after
 * <em>each</em> successful attack or defend phase (matching the tutorial's
 * description that "every time you successfully attack or defend, you receive
 * score"). The result is the points awarded for that phase.
 * </p>
 *
 * <h2>No GUI imports</h2>
 * <p>This class has no GUI imports. All state is queried by the game loop /
 * controller and passed to the GUI layer.</p>
 *
 * <p>Design Documentation Section 3.2.10, 4.2.5; FR 3.1.1 Section 4 (difficulty scaling),
 * FR 3.1.14 (extra feature - endless mode).</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 * @author Atika Hussain
 */
public class EndlessMode {

    // -------------------------------------------------------------------------
    // Tuning constants
    // -------------------------------------------------------------------------

    /**
     * Number of successfully completed phases (attacks or defends) before the
     * stage increments. Adjust to control how quickly difficulty escalates.
     */
    public static final int PHASES_PER_STAGE = 5;

    /**
     * Stage at which the active difficulty tier advances from EASY to MEDIUM.
     * Stages below this value use {@link Difficulty#EASY}.
     */
    public static final int MEDIUM_STAGE_THRESHOLD = 3;

    /**
     * Stage at which the active difficulty tier advances from MEDIUM to HARD.
     * Stages at or above this value use {@link Difficulty#HARD}.
     */
    public static final int HARD_STAGE_THRESHOLD = 6;

    /**
     * Base score awarded for a successful phase at stage 1.
     * Multiplied by the current stage for later phases.
     */
    public static final int BASE_PHASE_SCORE = 100;

    /**
     * Base damage applied by a successful attack before any power-up modifier.
     */
    public static final int BASE_ATTACK_DAMAGE = 10;

    /**
     * Score multiplier applied when Flurry Rush is consumed on a successful endless attack.
     */
    public static final int FLURRY_RUSH_SCORE_MULTIPLIER = 2;

    // -------------------------------------------------------------------------
    // Runtime state
    // -------------------------------------------------------------------------

    /** Total phases successfully completed this session. */
    private int phasesCompleted;

    /**
     * Current difficulty stage (1-based). Increments every
     * {@value #PHASES_PER_STAGE} completed phases.
     */
    private int currentStage;

    /** Cumulative score earned this endless session. */
    private int totalScore;

    /** Whether the session is still running. */
    private boolean active;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new EndlessMode session, fully reset and ready to start.
     * Call {@link #reset()} to reuse the same instance.
     */
    public EndlessMode() {
        reset();
    }

    // -------------------------------------------------------------------------
    // Core API - called by the game loop after each successful phase
    // -------------------------------------------------------------------------

    /**
     * Records that the player successfully completed a phase (attack or defend)
     * and updates all internal counters. Call this once per successful phase,
     * regardless of whether it was an attack or a defend.
     *
     * <p>The returned points value should be added to the player's displayed
     * score by the game loop. It is also accumulated in {@link #getTotalScore()}
     * for use when building the {@code SessionResult} at run end.</p>
     *
     * @param wordPointValue the {@code pointValue} of the word that was just
     *                       typed, as loaded from {@code words.csv} (Section 5.3)
     * @param elapsedMs      milliseconds taken to type the word
     * @param errorsMade     number of incorrect keystrokes during this phase
     * @return the points awarded for this phase (always Ã¢â€°Â¥ 0)
     */
    public int onPhaseCompleted(int wordPointValue, long elapsedMs, int errorsMade) {
        return onPhaseCompleted(wordPointValue, elapsedMs, errorsMade, false);
    }

    /**
     * Records a successful phase and applies any endless-only score modifiers.
     *
     * @param wordPointValue the {@code pointValue} of the word that was just typed
     * @param elapsedMs      milliseconds taken to type the word
     * @param errorsMade     number of incorrect keystrokes during this phase
     * @param flurryRushUsed whether the completed phase consumed Flurry Rush
     * @return the points awarded for this phase
     */
    public int onPhaseCompleted(int wordPointValue, long elapsedMs, int errorsMade, boolean flurryRushUsed) {
        phasesCompleted++;

        // Recalculate stage AFTER incrementing the phase count
        currentStage = (phasesCompleted / PHASES_PER_STAGE) + 1;

        int points = calculatePhaseScore(wordPointValue, elapsedMs, errorsMade, flurryRushUsed);
        totalScore += points;
        return points;
    }

    // -------------------------------------------------------------------------
    // Opponent damage
    // -------------------------------------------------------------------------

    /**
     * Returns the amount of damage a successful attack phase deals to the
     * opponent.
     *
     * <p>The frontend/controller may still replenish the opponent between
     * waves to preserve the endless loop, but successful attacks in Endless
     * mode should use the normal base damage and then apply any power-up
     * multipliers.</p>
     *
     * @return the base damage of a successful attack
     */
    public int getAttackDamage() {
        // In Endless mode, the opponent is infinite/unkillable (Design Doc 4.2.5)
        return 0;
    }

    // -------------------------------------------------------------------------
    // Difficulty - current tier for WordBank selection
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link Difficulty} tier that should be passed to
     * {@link WordBank#getRandomWord(String)} (or equivalent) when selecting
     * the next word. The tier advances as the stage increases:
     *
     * <ul>
     *   <li>Stages 1-{@value #MEDIUM_STAGE_THRESHOLD}-1 -> {@link Difficulty#EASY}</li>
     *   <li>Stages {@value #MEDIUM_STAGE_THRESHOLD}-{@value #HARD_STAGE_THRESHOLD}-1
     *       -> {@link Difficulty#MEDIUM}</li>
     *   <li>Stages {@value #HARD_STAGE_THRESHOLD}+ -> {@link Difficulty#HARD}</li>
     * </ul>
     *
     * @return the active difficulty tier for this stage
     */
    public Difficulty getCurrentDifficulty() {
        if (currentStage >= HARD_STAGE_THRESHOLD)   return Difficulty.HARD;
        if (currentStage >= MEDIUM_STAGE_THRESHOLD) return Difficulty.MEDIUM;
        return Difficulty.EASY;
    }

    /**
     * Returns a human-readable label for the current difficulty stage,
     * suitable for display in the HUD.
     *
     * @return the label string (e.g. {@code "Stage 4 - Medium"})
     */
    public String getStageLabel() {
        return "Stage " + currentStage + " \u2013 " + getCurrentDifficulty().name();
    }

    // -------------------------------------------------------------------------
    // Score calculation
    // -------------------------------------------------------------------------

    /**
     * Calculates the score awarded for a single successfully completed phase.
     *
     * <p>Formula (matches the design doc's description that score is based on
     * word difficulty and how fast it was typed, Section 4.2.2 Tutorial screen):</p>
     * <pre>
     *   stageMultiplier = currentStage
     *   base            = wordPointValue * stageMultiplier
     *   errorPenalty    = errorsMade * 10
     *   total           = max(0, base - errorPenalty)
     * </pre>
     *
     * <p>A speed bonus is deliberately not included here because the timer
     * mechanics in the turn-based attack/defend structure are handled by the
     * gameplay timer, and the design doc places the
     * time-based scoring formula in {@code ScoreCalculator}, not in the
     * mode-specific class.</p>
     *
     * @param wordPointValue the CSV {@code pointValue} of the completed word
     * @param elapsedMs      milliseconds taken (retained for future use /
     *                       forwarding to ScoreCalculator)
     * @param errorsMade     incorrect keystrokes during this phase
     * @return points to award (always Ã¢â€°Â¥ 0)
     */
    public int calculatePhaseScore(int wordPointValue, long elapsedMs, int errorsMade) {
        return calculatePhaseScore(wordPointValue, elapsedMs, errorsMade, false);
    }

    /**
     * Calculates the score awarded for a successfully completed phase and applies
     * the endless-only Flurry Rush bonus when applicable.
     *
     * @param wordPointValue the CSV {@code pointValue} of the completed word
     * @param elapsedMs      milliseconds taken
     * @param errorsMade     incorrect keystrokes during this phase
     * @param flurryRushUsed whether Flurry Rush was consumed on this phase
     * @return points to award (always >= 0)
     */
    public int calculatePhaseScore(int wordPointValue, long elapsedMs, int errorsMade, boolean flurryRushUsed) {
        int base         = wordPointValue * currentStage;
        int errorPenalty = errorsMade * 10;
        int total        = Math.max(0, base - errorPenalty);
        if (flurryRushUsed) {
            total *= FLURRY_RUSH_SCORE_MULTIPLIER;
        }
        return total;
    }

    // -------------------------------------------------------------------------
    // Session lifecycle
    // -------------------------------------------------------------------------

    /**
     * Marks the session as inactive. Call this when the player's HP reaches 0
     * or they choose to exit (Design Doc Section 4.2.5 - "run ends when your health
     * reaches 0, or when you choose to exit").
     *
     * <p>After calling this, the game loop should call
     * {@code GameSession.buildResult()} to package the final
     * {@code SessionResult} for {@code PlayerStats.updateAfterSession()}.</p>
     */
    public void endSession() {
        this.active = false;
    }

    /**
     * Resets all counters to their starting values, ready for a new run.
     * Called by the constructor and may be called again to restart endless mode
     * without constructing a new instance.
     */
    public void reset() {
        this.phasesCompleted = 0;
        this.currentStage    = 1;
        this.totalScore      = 0;
        this.active          = true;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return total phases (attacks + defends) successfully completed */
    public int getPhasesCompleted() { return phasesCompleted; }

    /** @return current difficulty stage (1-based) */
    public int getCurrentStage()    { return currentStage; }

    /** @return cumulative score earned this session */
    public int getTotalScore()      { return totalScore; }

    /** @return {@code true} if the session is still running */
    public boolean isActive()       { return active; }

    /**
     * Returns a compact summary of the endless session state for logging and debugging.
     *
     * @return a string containing the current stage, difficulty, completed phase count,
     *         cumulative score, and active status
     */
    @Override
    public String toString() {
        return "EndlessMode{stage=" + currentStage
                + ", difficulty=" + getCurrentDifficulty()
                + ", phases=" + phasesCompleted
                + ", score=" + totalScore
                + ", active=" + active + "}";
    }
}
