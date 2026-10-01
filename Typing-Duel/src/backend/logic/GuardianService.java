package backend.logic;

import backend.model.AccountType;
import backend.model.Player;
import backend.model.PlayerStats;
import backend.storage.FileDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code GuardianService} class provides all functionality related to
 * guardian (parent/teacher) control within the Typing Duel application.
 *
 * <p>This service acts as the centralized authority for all operations requiring
 * elevated privileges, including account management, password resets,
 * statistics resets, and leaderboard restoration.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Guardian authentication via PIN verification</li>
 *   <li>Managing player accounts (create, delete)</li>
 *   <li>Resetting player statistics and progress</li>
 *   <li>Resetting player passwords</li>
 *   <li>Restoring default leaderboard data</li>
 * </ul>
 *
 * <p>All changes are persisted immediately using {@link FileDataStore} to ensure
 * data integrity and prevent loss (NFR 3.2.14).</p>
 *
 * <p><b>Functional Requirements:</b> FR 3.1.7, FR 3.1.11</p>
 * <p>Team 49 – CS2212B Winter 2026</p>
 *
 * @author Atika Hussain
 * @author Mrida Hingmire
 * @author Misol Kang
 */
public class GuardianService {

    /** Persistence layer for loading and saving player data. */
    private final FileDataStore dataStore;

    /**
     * Constructs a GuardianService with a given data store.
     *
     * @param dataStore the data store used for persistence
     * @throws IllegalArgumentException if dataStore is null
     */
    public GuardianService(FileDataStore dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("dataStore must not be null");
        }
        this.dataStore = dataStore;
    }

    /**
     * Verifies whether a guardian PIN is correct.
     *
     * @param guardian the guardian account
     * @param enteredPin the entered PIN
     * @return true if the PIN matches; false otherwise
     */
    public boolean verifyPin(Player guardian, String enteredPin) {
        if (!isGuardian(guardian) || enteredPin == null) return false;

        String expected = guardian.getGuardianCode();
        if (expected == null || expected.isBlank()) {
            expected = guardian.getPassword();
        }
        if (expected == null || expected.isBlank()) return false;

        return expected.equals(enteredPin.trim());
    }

    /**
     * Updates the guardian PIN for a specific account.
     *
     * @param username the guardian username
     * @param newPin the new PIN
     * @return true if successfully updated; false otherwise
     * @throws IllegalArgumentException if newPin is null or blank
     */
    public boolean updateGuardianPin(String username, String newPin) {
        if (newPin == null || newPin.isBlank()) {
            throw new IllegalArgumentException("Guardian code must not be blank.");
        }

        Player guardian = findPlayer(username);
        if (!isGuardian(guardian)) return false;

        guardian.setGuardianCode(newPin.trim());
        return dataStore.savePlayer(guardian);
    }

    /**
     * Checks whether a player is a guardian account.
     *
     * @param player the player to check
     * @return true if the player is a guardian; false otherwise
     */
    public boolean isGuardian(Player player) {
        return player != null && AccountType.PARENT_TEACHER.equals(player.getAccountType());
    }

    /**
     * Retrieves all registered players as an unmodifiable list.
     *
     * @return list of players
     */
    public List<Player> getAllPlayers() {
        List<Player> all = dataStore.loadPlayers();
        return Collections.unmodifiableList(all != null ? all : new ArrayList<>());
    }

    /**
     * Finds a player by username (case-insensitive).
     *
     * @param username the username to search
     * @return the Player if found; null otherwise
     */
    public Player findPlayer(String username) {
        if (username == null) return null;
        for (Player p : dataStore.loadPlayers()) {
            if (p.getUsername().equalsIgnoreCase(username)) return p;
        }
        return null;
    }

    /**
     * Resets only the statistics of a player.
     *
     * @param username the player's username
     * @return true if successful; false if player not found
     */
    public boolean resetPlayerStats(String username) {
        List<Player> players = dataStore.loadPlayers();
        Player target = findInList(players, username);
        if (target == null) return false;

        resetStatsObject(target.getStats());
        target.getScores().clear();

        dataStore.savePlayers(players);
        return true;
    }

    /**
     * Fully resets a player's statistics and level progress.
     *
     * @param username the player's username
     * @return true if successful; false if player not found
     */
    public boolean resetPlayerProgress(String username) {
        List<Player> players = dataStore.loadPlayers();
        Player target = findInList(players, username);
        if (target == null) return false;

        resetStatsObject(target.getStats());
        target.getScores().clear();
        target.setHighestLevel(0);

        dataStore.savePlayers(players);
        return true;
    }

    /**
     * Resets a player's password.
     *
     * @param username the username
     * @param newPassword the new password
     * @return true if updated; false otherwise
     * @throws IllegalArgumentException if password is blank
     */
    public boolean resetPlayerPassword(String username, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be blank.");
        }

        List<Player> players = dataStore.loadPlayers();
        Player target = findInList(players, username);
        if (target == null || isGuardian(target)) return false;

        target.setPassword(newPassword);
        dataStore.savePlayers(players);
        return true;
    }

    /**
     * Resets a password using guardian privileges.
     *
     * @param actingGuardianUsername the guardian performing the action
     * @param targetUsername the account being modified
     * @param newPassword the new password
     * @return true if successful; false otherwise
     */
    public boolean resetPasswordAsGuardian(String actingGuardianUsername,
                                           String targetUsername, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be blank.");
        }

        Player actingGuardian = findPlayer(actingGuardianUsername);
        if (!isGuardian(actingGuardian)) return false;

        Player target = findPlayer(targetUsername);
        if (target == null) return false;

        if (isGuardian(target)) {
            if (!actingGuardian.getUsername().equalsIgnoreCase(target.getUsername())) {
                return false;
            }
            target.setPassword(newPassword);
            return dataStore.savePlayer(target);
        }

        return resetPlayerPassword(targetUsername, newPassword);
    }

    /**
     * Creates a new standard player account.
     *
     * @param username the username
     * @param password the password
     * @return the created Player
     */
    public Player createPlayer(String username, String password) {
        return createAccount(username, password, AccountType.PLAYER);
    }

    /**
     * Creates a guardian account.
     *
     * @param username the username
     * @param password the password
     * @return the created guardian Player
     */
    public Player createGuardian(String username, String password) {
        return createGuardian(username, password, password);
    }

    /**
     * Creates a guardian account with a custom PIN.
     *
     * @param username the username
     * @param password the password
     * @param guardianCode the guardian PIN
     * @return the created guardian Player
     */
    public Player createGuardian(String username, String password, String guardianCode) {
        if (guardianCode == null || guardianCode.isBlank()) {
            throw new IllegalArgumentException("Guardian code must not be blank.");
        }

        Player guardian = createAccount(username, password, AccountType.PARENT_TEACHER);
        guardian.setGuardianCode(guardianCode.trim());

        List<Player> players = dataStore.loadPlayers();
        Player savedGuardian = findInList(players, username);
        if (savedGuardian != null) {
            savedGuardian.setGuardianCode(guardianCode.trim());
            dataStore.savePlayers(players);
        }

        return savedGuardian != null ? savedGuardian : guardian;
    }

    /**
     * Deletes a player account permanently.
     *
     * @param username the username to delete
     * @return true if removed; false otherwise
     */
    public boolean deletePlayer(String username) {
        List<Player> players = dataStore.loadPlayers();
        boolean removed = players.removeIf(
                p -> p.getUsername().equalsIgnoreCase(username));

        if (removed) dataStore.savePlayers(players);
        return removed;
    }

    /**
     * Restores the default leaderboard.
     *
     * @return true if successful
     */
    public boolean restoreDefaultLeaderboard() {
        return dataStore.restoreDefaultHighScores();
    }

    /**
     * Creates a new account with a specified type.
     *
     * @param username username
     * @param password password
     * @param accountType type of account
     * @return created Player
     */
    private Player createAccount(String username, String password, AccountType accountType) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }

        List<Player> players = dataStore.loadPlayers();

        if (findInList(players, username) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Player newPlayer = new Player(username.trim(), password, accountType);
        players.add(newPlayer);
        dataStore.savePlayers(players);
        return newPlayer;
    }

    /**
     * Resets all values in a PlayerStats object.
     *
     * @param stats the stats object
     */
    private void resetStatsObject(PlayerStats stats) {
        if (stats == null) return;
        stats.setAverageWPM(0.0);
        stats.setPeakWPM(0.0);
        stats.setAccuracy(0.0);
        stats.setErrorCount(0);
        stats.setTotalTimePlayed(0);
        stats.setWordsTyped(0);
        stats.setHighestLevel(0);
        stats.setHighScore(0);
    }

    /**
     * Finds a player in a list.
     *
     * @param players list of players
     * @param username username to search
     * @return Player if found, null otherwise
     */
    private Player findInList(List<Player> players, String username) {
        if (players == null || username == null) return null;
        for (Player p : players) {
            if (p.getUsername().equalsIgnoreCase(username)) return p;
        }
        return null;
    }
}