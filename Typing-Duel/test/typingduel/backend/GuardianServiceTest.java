package typingduel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import typingduel.model.AccountType;
import typingduel.model.AppConfig;
import typingduel.model.Difficulty;
import typingduel.model.Player;
import typingduel.model.Score;
import typingduel.storage.FileDataStore;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GuardianServiceTest.java
 * <p>
 * JUnit 5 unit tests for {@link GuardianService}, covering all guardian
 * reset actions required by FR 3.1.7 and the PIN check required by FR 3.1.11.
 * </p>
 *
 * <p>A lightweight in-memory {@link StubDataStore} is used in place of the
 * real {@link FileDataStore} so no file I/O occurs during testing
 * (NFR 3.2.11).</p>
 *
 * <p>Team 49 – CS2212B Winter 2026</p>
 * Contributors: Mrida Hingmire, Misol Kang, Atika Hussain
 */
class GuardianServiceTest {

    // -------------------------------------------------------------------------
    // In-memory FileDataStore stub
    // -------------------------------------------------------------------------

    /**
     * Minimal stub that holds {@link Player} objects in a list without
     * touching the filesystem. Only {@code loadPlayers} and {@code savePlayers}
     * are overridden; everything else inherited from {@link FileDataStore} is
     * unused in these tests.
     */
    private static class StubDataStore extends FileDataStore {

        private List<Player> store;

        StubDataStore(List<Player> initial) {
            super();
            this.store = new ArrayList<>(initial);
        }

        @Override
        public List<Player> loadPlayers() {
            // Mutable copy so GuardianService can iterate and mutate freely
            return new ArrayList<>(store);
        }

        @Override
        public void savePlayers(List<Player> players) {
            this.store = new ArrayList<>(players);
        }
    }

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private Player          alice;
    private StubDataStore   stubStore;
    private GuardianService service;

    @BeforeEach
    void setUp() {
        // Known PIN for PIN-verification tests
        AppConfig cfg = AppConfig.loadDefaults();
        cfg.setParentPin("1234");
        AppConfig.setInstance(cfg);

        // A player with meaningful non-zero state so resets can be verified
        alice = new Player("alice", "pass123", AccountType.PLAYER);
        alice.getStats().setAverageWPM(72.5);
        alice.getStats().setPeakWPM(95.0);
        alice.getStats().setAccuracy(88.4);
        alice.getStats().setErrorCount(47);
        alice.getStats().setTotalTimePlayed(3600);
        alice.getStats().setWordsTyped(320);
        alice.getStats().setHighestLevel(3);
        alice.getStats().setHighScore(5000);
        alice.setHighestLevel(3);

        // Give her some per-difficulty scores so we can verify they clear
        alice.addOrReplaceScore(Difficulty.EASY,   1200);
        alice.addOrReplaceScore(Difficulty.MEDIUM, 3400);
        alice.addOrReplaceScore(Difficulty.HARD,   5000);

        List<Player> initial = new ArrayList<>();
        initial.add(alice);

        stubStore = new StubDataStore(initial);
        service   = new GuardianService(stubStore);
    }

    // =========================================================================
    // PIN verification (FR 3.1.11)
    // =========================================================================

    @Test
    @DisplayName("verifyPin returns true for the correct PIN")
    void verifyPin_correct() {
        assertTrue(service.verifyPin("1234"));
    }

    @Test
    @DisplayName("verifyPin returns false for a wrong PIN")
    void verifyPin_wrong() {
        assertFalse(service.verifyPin("0000"));
    }

    @Test
    @DisplayName("verifyPin returns false for null input")
    void verifyPin_null() {
        assertFalse(service.verifyPin(null));
    }

    @Test
    @DisplayName("verifyPin trims surrounding whitespace before comparing")
    void verifyPin_trimmed() {
        assertTrue(service.verifyPin("  1234  "));
    }

    // =========================================================================
    // isGuardian (AccountType check)
    // =========================================================================

    @Test
    @DisplayName("isGuardian returns true for PARENT_TEACHER account")
    void isGuardian_parentTeacher() {
        Player guardian = new Player("guard", "pw", AccountType.PARENT_TEACHER);
        assertTrue(service.isGuardian(guardian));
    }

    @Test
    @DisplayName("isGuardian returns false for PLAYER account")
    void isGuardian_player() {
        assertFalse(service.isGuardian(alice));
    }

    @Test
    @DisplayName("isGuardian returns false for null")
    void isGuardian_null() {
        assertFalse(service.isGuardian(null));
    }

    // =========================================================================
    // resetPlayerStats (FR 3.1.7)
    // =========================================================================

    @Test
    @DisplayName("resetPlayerStats zeroes every PlayerStats field")
    void resetStats_clearsAllStatFields() {
        assertTrue(service.resetPlayerStats("alice"));

        Player saved = stubStore.loadPlayers().get(0);
        assertAll("All PlayerStats fields must be zero",
                () -> assertEquals(0.0, saved.getStats().getAverageWPM(),   "averageWPM"),
                () -> assertEquals(0.0, saved.getStats().getPeakWPM(),      "peakWPM"),
                () -> assertEquals(0.0, saved.getStats().getAccuracy(),     "accuracyPercent"),
                () -> assertEquals(0,   saved.getStats().getErrorCount(),   "totalErrorCount"),
                () -> assertEquals(0,   saved.getStats().getTotalTimePlayed(), "totalTimePlayed"),
                () -> assertEquals(0,   saved.getStats().getWordsTyped(),   "totalWordsTyped"),
                () -> assertEquals(0,   saved.getStats().getHighestLevel(), "highestLevelReached"),
                () -> assertEquals(0,   saved.getStats().getHighScore(),    "overallHighScore")
        );
    }

    @Test
    @DisplayName("resetPlayerStats also clears the per-difficulty Score list")
    void resetStats_clearsScoreList() {
        service.resetPlayerStats("alice");
        assertTrue(stubStore.loadPlayers().get(0).getScores().isEmpty(),
                "scores list should be empty after stats reset");
    }

    @Test
    @DisplayName("resetPlayerStats preserves username and password")
    void resetStats_preservesCredentials() {
        service.resetPlayerStats("alice");
        Player saved = stubStore.loadPlayers().get(0);
        assertEquals("alice",   saved.getUsername(), "username unchanged");
        assertEquals("pass123", saved.getPassword(), "password unchanged");
    }

    @Test
    @DisplayName("resetPlayerStats preserves highestLevelUnlocked on Player")
    void resetStats_preservesLevelUnlocked() {
        service.resetPlayerStats("alice");
        // Stats reset does NOT touch Player.highestLevelUnlocked
        assertEquals(3, stubStore.loadPlayers().get(0).getHighestLevelUnlocked(),
                "highestLevelUnlocked should remain 3 after stats-only reset");
    }

    @Test
    @DisplayName("resetPlayerStats returns false for unknown username")
    void resetStats_unknownPlayer() {
        assertFalse(service.resetPlayerStats("nobody"));
    }

    @Test
    @DisplayName("resetPlayerStats is case-insensitive")
    void resetStats_caseInsensitive() {
        assertTrue(service.resetPlayerStats("ALICE"));
        assertEquals(0.0, stubStore.loadPlayers().get(0).getStats().getAverageWPM());
    }

    @Test
    @DisplayName("resetPlayerStats persists the change to the data store")
    void resetStats_persists() {
        service.resetPlayerStats("alice");
        // Reload from store – if save was called the values will be 0
        assertEquals(0, stubStore.loadPlayers().get(0).getStats().getHighScore(),
                "Change must be persisted via savePlayers");
    }

    // =========================================================================
    // resetPlayerProgress (FR 3.1.7 – full reset)
    // =========================================================================

    @Test
    @DisplayName("resetPlayerProgress zeroes stats AND resets highestLevelUnlocked to 0")
    void resetProgress_clearsEverything() {
        assertTrue(service.resetPlayerProgress("alice"));

        Player saved = stubStore.loadPlayers().get(0);
        assertAll("Full reset must zero stats and level",
                () -> assertEquals(0.0, saved.getStats().getAverageWPM(), "averageWPM"),
                () -> assertEquals(0,   saved.getStats().getHighScore(),  "highScore"),
                () -> assertEquals(0,   saved.getHighestLevelUnlocked(),  "highestLevelUnlocked")
        );
    }

    @Test
    @DisplayName("resetPlayerProgress clears the Score list")
    void resetProgress_clearsScores() {
        service.resetPlayerProgress("alice");
        assertTrue(stubStore.loadPlayers().get(0).getScores().isEmpty());
    }

    @Test
    @DisplayName("resetPlayerProgress preserves username, password, and accountType")
    void resetProgress_preservesCredentials() {
        service.resetPlayerProgress("alice");
        Player saved = stubStore.loadPlayers().get(0);
        assertEquals("alice",            saved.getUsername());
        assertEquals("pass123",          saved.getPassword());
        assertEquals(AccountType.PLAYER, saved.getAccountType());
    }

    @Test
    @DisplayName("resetPlayerProgress returns false for unknown player")
    void resetProgress_unknownPlayer() {
        assertFalse(service.resetPlayerProgress("ghost"));
    }

    // =========================================================================
    // resetPlayerPassword (FR 3.1.7)
    // =========================================================================

    @Test
    @DisplayName("resetPlayerPassword updates the stored password")
    void resetPassword_updatesPassword() {
        assertTrue(service.resetPlayerPassword("alice", "newPass99"));
        assertEquals("newPass99", stubStore.loadPlayers().get(0).getPassword());
    }

    @Test
    @DisplayName("resetPlayerPassword returns false for unknown player")
    void resetPassword_unknownPlayer() {
        assertFalse(service.resetPlayerPassword("nobody", "abc"));
    }

    @Test
    @DisplayName("resetPlayerPassword returns false for guardian accounts")
    void resetPassword_guardianAccountDenied() {
        service.createGuardian("guard", "pw", "guard-code");
        assertFalse(service.resetPlayerPassword("guard", "newPass99"));
    }

    @Test
    @DisplayName("resetPlayerPassword throws for blank new password")
    void resetPassword_blank_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.resetPlayerPassword("alice", "   "));
    }

    @Test
    @DisplayName("resetPlayerPassword throws for null new password")
    void resetPassword_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.resetPlayerPassword("alice", null));
    }

    // =========================================================================
    // deletePlayer (FR 3.1.7 – clear specific row)
    // =========================================================================

    @Test
    @DisplayName("deletePlayer removes the player from the persisted list")
    void deletePlayer_removesAccount() {
        assertTrue(service.deletePlayer("alice"));
        assertTrue(stubStore.loadPlayers().isEmpty(),
                "Player list must be empty after deletion");
    }

    @Test
    @DisplayName("deletePlayer returns false when username does not exist")
    void deletePlayer_notFound() {
        assertFalse(service.deletePlayer("nobody"));
        assertEquals(1, stubStore.loadPlayers().size(), "List size unchanged");
    }

    @Test
    @DisplayName("deletePlayer is case-insensitive")
    void deletePlayer_caseInsensitive() {
        assertTrue(service.deletePlayer("ALICE"));
        assertTrue(stubStore.loadPlayers().isEmpty());
    }

    // =========================================================================
    // createPlayer (FR 3.1.7)
    // =========================================================================

    @Test
    @DisplayName("createPlayer adds a new PLAYER account with zeroed stats")
    void createPlayer_addsProfile() {
        Player bob = service.createPlayer("bob", "bobPass");

        assertNotNull(bob);
        assertEquals("bob",              bob.getUsername());
        assertEquals(AccountType.PLAYER, bob.getAccountType());
        assertEquals(0,                  bob.getStats().getHighScore());
        assertEquals(0,                  bob.getHighestLevelUnlocked());
        assertEquals(2, stubStore.loadPlayers().size());
    }

    @Test
    @DisplayName("createGuardian assigns PARENT_TEACHER account type")
    void createGuardian_correctAccountType() {
        Player g = service.createGuardian("supervisor", "pw99");
        assertEquals(AccountType.PARENT_TEACHER, g.getAccountType());
    }

    @Test
    @DisplayName("createPlayer throws for duplicate username (case-insensitive)")
    void createPlayer_duplicate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createPlayer("Alice", "whatever"));
    }

    @Test
    @DisplayName("createPlayer throws for blank username")
    void createPlayer_blankUsername_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createPlayer("  ", "pass"));
    }

    @Test
    @DisplayName("createPlayer throws for blank password")
    void createPlayer_blankPassword_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createPlayer("newUser", ""));
    }

    // =========================================================================
    // Constructor guard
    // =========================================================================

    @Test
    @DisplayName("GuardianService constructor rejects null dataStore")
    void constructor_nullDataStore_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new GuardianService(null));
    }
}
