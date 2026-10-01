package typingduel.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import typingduel.model.Difficulty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EndlessModeTest.java
 * <p>
 * JUnit 5 unit tests for {@link EndlessMode}, covering:
 * </p>
 * <ul>
 *   <li>Stage progression every {@link EndlessMode#PHASES_PER_STAGE} phases</li>
 *   <li>{@link Difficulty} tier advancing correctly through EASY → MEDIUM → HARD</li>
 *   <li>Zero-damage rule on the opponent ({@link EndlessMode#getAttackDamage()})</li>
 *   <li>Phase score calculation using CSV {@code pointValue} × stage multiplier</li>
 *   <li>Error penalty reducing score, score floor at 0</li>
 *   <li>Total score accumulation across multiple phases</li>
 *   <li>Session lifecycle: {@link EndlessMode#endSession()} and
 *       {@link EndlessMode#reset()}</li>
 * </ul>
 *
 * <p>No file I/O or GUI classes are used (NFR 3.2.11).</p>
 *
 * <p>Team 49 – CS2212B Winter 2026</p>
 * Contributors: Mrida Hingmire, Misol Kang, Atika Hussain
 */
class EndlessModeTest {

    private EndlessMode endless;

    // -------------------------------------------------------------------------
    // Helper: complete N phases with neutral values (no errors, 1000 ms)
    // -------------------------------------------------------------------------
    private void completePhases(int count) {
        for (int i = 0; i < count; i++) {
            endless.onPhaseCompleted(10, 1000L, 0);
        }
    }

    @BeforeEach
    void setUp() {
        endless = new EndlessMode();
    }

    // =========================================================================
    // Initial state
    // =========================================================================

    @Test
    @DisplayName("Fresh session starts at stage 1, zero phases, zero score, active")
    void initialState() {
        assertAll(
                () -> assertEquals(1,              endless.getCurrentStage()),
                () -> assertEquals(0,              endless.getPhasesCompleted()),
                () -> assertEquals(0,              endless.getTotalScore()),
                () -> assertTrue(endless.isActive()),
                () -> assertEquals(Difficulty.EASY, endless.getCurrentDifficulty())
        );
    }

    // =========================================================================
    // Stage progression
    // =========================================================================

    @Test
    @DisplayName("Stage increments to 2 after exactly PHASES_PER_STAGE completions")
    void stageIncrements_atBoundary() {
        completePhases(EndlessMode.PHASES_PER_STAGE);
        assertEquals(2, endless.getCurrentStage());
    }

    @Test
    @DisplayName("Stage does not increment one phase before the boundary")
    void stageStable_beforeBoundary() {
        completePhases(EndlessMode.PHASES_PER_STAGE - 1);
        assertEquals(1, endless.getCurrentStage());
    }

    @Test
    @DisplayName("Stage advances correctly across multiple boundaries")
    void stageIncrements_multipleStages() {
        int targetStage = 5;
        completePhases(EndlessMode.PHASES_PER_STAGE * (targetStage - 1));
        assertEquals(targetStage, endless.getCurrentStage());
    }

    @Test
    @DisplayName("phasesCompleted increments by 1 for each completed phase")
    void phasesCompleted_countsCorrectly() {
        endless.onPhaseCompleted(10, 500L, 0);
        endless.onPhaseCompleted(25, 800L, 1);
        assertEquals(2, endless.getPhasesCompleted());
    }

    // =========================================================================
    // Difficulty tier progression (EASY → MEDIUM → HARD)
    // =========================================================================

    @Test
    @DisplayName("Difficulty is EASY before reaching MEDIUM_STAGE_THRESHOLD")
    void difficulty_easy_belowMediumThreshold() {
        completePhases(EndlessMode.PHASES_PER_STAGE * (EndlessMode.MEDIUM_STAGE_THRESHOLD - 2));
        assertEquals(Difficulty.EASY, endless.getCurrentDifficulty(),
                "Should still be EASY before stage " + EndlessMode.MEDIUM_STAGE_THRESHOLD);
    }

    @Test
    @DisplayName("Difficulty advances to MEDIUM at MEDIUM_STAGE_THRESHOLD")
    void difficulty_medium_atThreshold() {
        // Advance to exactly MEDIUM_STAGE_THRESHOLD
        completePhases(EndlessMode.PHASES_PER_STAGE * (EndlessMode.MEDIUM_STAGE_THRESHOLD - 1));
        assertEquals(Difficulty.MEDIUM, endless.getCurrentDifficulty(),
                "Should be MEDIUM at stage " + EndlessMode.MEDIUM_STAGE_THRESHOLD);
    }

    @Test
    @DisplayName("Difficulty advances to HARD at HARD_STAGE_THRESHOLD")
    void difficulty_hard_atThreshold() {
        completePhases(EndlessMode.PHASES_PER_STAGE * (EndlessMode.HARD_STAGE_THRESHOLD - 1));
        assertEquals(Difficulty.HARD, endless.getCurrentDifficulty(),
                "Should be HARD at stage " + EndlessMode.HARD_STAGE_THRESHOLD);
    }

    @Test
    @DisplayName("Difficulty stays HARD beyond HARD_STAGE_THRESHOLD")
    void difficulty_hard_beyondThreshold() {
        completePhases(EndlessMode.PHASES_PER_STAGE * (EndlessMode.HARD_STAGE_THRESHOLD + 10));
        assertEquals(Difficulty.HARD, endless.getCurrentDifficulty(),
                "Should remain HARD indefinitely after threshold");
    }

    // =========================================================================
    // Zero-damage opponent rule (Design Doc §4.2.5)
    // =========================================================================

    @Test
    @DisplayName("getAttackDamage returns 0 at stage 1 (infinite opponent HP)")
    void attackDamage_zero_stage1() {
        assertEquals(0, endless.getAttackDamage());
    }

    @Test
    @DisplayName("getAttackDamage still returns 0 at later stages")
    void attackDamage_zero_laterStage() {
        completePhases(EndlessMode.PHASES_PER_STAGE * 8);
        assertEquals(0, endless.getAttackDamage(),
                "Opponent must remain unkillable regardless of stage");
    }

    // =========================================================================
    // Score calculation
    // =========================================================================

    @Test
    @DisplayName("Phase score = wordPointValue × stage when no errors (stage 1)")
    void score_baseFormula_stage1_noErrors() {
        int pointValue = 25; // e.g. a MEDIUM word from words.csv
        int score = endless.calculatePhaseScore(pointValue, 1000L, 0);
        assertEquals(25 * 1, score, "Stage 1: score = pointValue × 1");
    }

    @Test
    @DisplayName("Phase score multiplies by stage number")
    void score_multipliesWithStage() {
        // Advance to stage 3
        completePhases(EndlessMode.PHASES_PER_STAGE * 2);
        int score = endless.calculatePhaseScore(10, 1000L, 0);
        assertEquals(10 * 3, score, "Stage 3: score = pointValue × 3");
    }

    @Test
    @DisplayName("Each error reduces score by 10")
    void score_errorPenalty() {
        int base     = endless.calculatePhaseScore(20, 1000L, 0);
        int withTwo  = endless.calculatePhaseScore(20, 1000L, 2);
        assertEquals(base - 20, withTwo, "2 errors should reduce score by 20");
    }

    @Test
    @DisplayName("Score is floored at 0 even with many errors")
    void score_neverNegative() {
        int score = endless.calculatePhaseScore(5, 1000L, 999);
        assertTrue(score >= 0, "Score must never be negative");
    }

    @Test
    @DisplayName("onPhaseCompleted accumulates totalScore correctly")
    void score_accumulatesAcrossPhases() {
        int p1 = endless.onPhaseCompleted(10, 500L,  0);
        int p2 = endless.onPhaseCompleted(25, 1000L, 1);
        assertEquals(p1 + p2, endless.getTotalScore(),
                "Total score must be the sum of all returned point values");
    }

    @Test
    @DisplayName("Score increases per phase as stage advances (same word, same speed)")
    void score_higherInLaterStages() {
        int stage1Score = endless.onPhaseCompleted(20, 1000L, 0);

        // Advance to stage 2
        completePhases(EndlessMode.PHASES_PER_STAGE - 1); // already 1 phase done
        int stage2Score = endless.onPhaseCompleted(20, 1000L, 0);

        assertTrue(stage2Score > stage1Score,
                "The same word should award more points in a later stage");
    }

    // =========================================================================
    // Stage label
    // =========================================================================

    @Test
    @DisplayName("getStageLabel contains stage number and difficulty name at stage 1")
    void stageLabel_stage1() {
        String label = endless.getStageLabel();
        assertTrue(label.contains("1"),         "Label should contain stage number");
        assertTrue(label.contains("EASY"),      "Label should contain difficulty");
    }

    @Test
    @DisplayName("getStageLabel updates to HARD after threshold")
    void stageLabel_hard() {
        completePhases(EndlessMode.PHASES_PER_STAGE * (EndlessMode.HARD_STAGE_THRESHOLD - 1));
        assertTrue(endless.getStageLabel().contains("HARD"),
                "Label should reflect HARD difficulty");
    }

    // =========================================================================
    // Session lifecycle
    // =========================================================================

    @Test
    @DisplayName("endSession marks session inactive")
    void endSession_setsInactive() {
        endless.endSession();
        assertFalse(endless.isActive());
    }

    @Test
    @DisplayName("reset restores all fields to initial values")
    void reset_restoresAllFields() {
        // Dirty the state
        completePhases(EndlessMode.PHASES_PER_STAGE * 5);
        endless.endSession();

        endless.reset();

        assertAll("All fields must be restored",
                () -> assertEquals(1,               endless.getCurrentStage()),
                () -> assertEquals(0,               endless.getPhasesCompleted()),
                () -> assertEquals(0,               endless.getTotalScore()),
                () -> assertTrue(endless.isActive()),
                () -> assertEquals(Difficulty.EASY, endless.getCurrentDifficulty())
        );
    }

    @Test
    @DisplayName("Session remains active until endSession is called")
    void sessionActive_untilEndSession() {
        completePhases(50); // many phases
        assertTrue(endless.isActive(), "Session should still be active after many phases");
        endless.endSession();
        assertFalse(endless.isActive());
    }
}
