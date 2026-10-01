package backend.model;

/**
 * Difficulty.java
 * <p>
 * Defines the operational difficulty tiers for game sessions in Typing Duel.
 * These constants are used by the WordBank to filter word selection and by 
 * the GameTimer to adjust countdown durations (FR 3.1.1, 3.5.1).
 * </p>
 * * <p>In Endless Mode, the active difficulty tier scales based on the 
 * current stage reached by the player (FR 3.1.14).</p>
 *
 * <p>Team 49 â€“ CS2212B Winter 2026</p>
 * @author Misol Kang
 */
public enum Difficulty {
    /** * Short, common words. Used for early game stages and 
     * introductory levels. High timer allowance.
     */
    EASY,

    /** * Medium-length words with moderate complexity. Used for 
     * mid-game progression and intermediate stages.
     */
    MEDIUM,

    /** * Long, complex, or technical words. Used for high-level 
     * gameplay and late-stage Endless Mode. Minimal timer allowance.
     */
    HARD,

    /** * Endless mode difficulty, mixing all word types.
     */
    ENDLESS
}