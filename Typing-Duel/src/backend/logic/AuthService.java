package backend.logic;

import backend.model.AccountType;
import backend.model.Player;
import backend.storage.FileDataStore;

/**
 * The {@code AuthService} class provides authentication and session management
 * functionality for the Typing Duel application.
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>User login and credential validation</li>
 *     <li>New account registration</li>
 *     <li>Password recovery using security questions</li>
 *     <li>Managing the currently logged-in user session</li>
 * </ul>
 *
 * <h2>Functional Requirements Satisfied:</h2>
 * <ul>
 *     <li><b>FR 3.1.6:</b> Login Screen and Multiple User Support</li>
 *     <li><b>FR 3.1.14:</b> Forgot Password system with security questions</li>
 * </ul>
 *
 * <p>This class interacts with {@link FileDataStore} to persist and retrieve
 * player data.</p>
 *
 * <p>Team 49 – CS2212B Winter 2026</p>
 *
 * @author Misol Kang
 */
public class AuthService {

    /** Predefined supported security question: birth city. */
    private static final String QUESTION_BIRTH_CITY = "What city were you born in?";
    /** Predefined supported security question: mother's maiden name. */
    private static final String QUESTION_MAIDEN_NAME = "What is your mother's maiden name?";
    /** Predefined supported security question: first pet. */
    private static final String QUESTION_FIRST_PET = "What was the name of your first pet?";
    /** Predefined supported security question: childhood nickname. */
    private static final String QUESTION_CHILDHOOD_NICKNAME = "What was your childhood nickname?";

    /** Persistence layer used for loading and saving player data. */
    private final FileDataStore dataStore;

    /** The currently authenticated player; null if no user is logged in. */
    private Player currentPlayer;

    /**
     * Constructs an {@code AuthService} with the specified data store.
     *
     * @param dataStore the data store used for player persistence operations
     */
    public AuthService(FileDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Authenticates a user by validating their username and password.
     * If authentication succeeds, the current session is updated.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     * @return {@code true} if authentication is successful; {@code false} otherwise
     */
    public boolean login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        Player player = dataStore.loadPlayer(username);

        if (player != null && player.getPassword().equals(password)) {
            this.currentPlayer = player;
            return true;
        }
        return false;
    }

    /**
     * Registers a new user account with associated security questions.
     *
     * <p>If a guardian code is provided, the account is created as a
     * {@link AccountType#PARENT_TEACHER}; otherwise, it is a standard player account.</p>
     *
     * @param username the unique username for the new account
     * @param password the password for the new account
     * @param questions an array of security questions
     * @param answers an array of corresponding answers to the security questions
     * @param guardianCode an optional code indicating a guardian account
     * @return {@code true} if registration is successful; {@code false} if the username
     * already exists or saving fails
     */
    public boolean register(String username, String password, String[] questions, String[] answers, String guardianCode) {
        if (dataStore.playerExists(username)) {
            return false;
        }

        boolean isGuardianAccount = guardianCode != null && !guardianCode.isBlank();
        AccountType accountType = isGuardianAccount ? AccountType.PARENT_TEACHER : AccountType.PLAYER;

        Player newPlayer = new Player(username, password, accountType);
        newPlayer.setSecurityQs(questions);
        newPlayer.setSecurityAs(answers);

        if (isGuardianAccount) {
            newPlayer.setGuardianCode(guardianCode.trim());
        }

        boolean saved = dataStore.savePlayer(newPlayer);
        if (saved) {
            this.currentPlayer = newPlayer;
        }
        return saved;
    }

    /**
     * Verifies whether the provided security answers match the stored answers
     * for a given user.
     *
     * <p>Comparison is case-insensitive and ignores leading/trailing whitespace.</p>
     *
     * @param username the username of the account being verified
     * @param providedAnswers the answers supplied by the user
     * @return {@code true} if all answers match; {@code false} otherwise
     */
    public boolean verifySecurityAnswers(String username, String[] providedAnswers) {
        Player player = dataStore.loadPlayer(username);
        if (player == null || player.getSecurityAs() == null) return false;

        if (providedAnswers == null || providedAnswers.length == 0) {
            return false;
        }

        String[] storedAnswers = player.getSecurityAs();

        if (providedAnswers.length == 1) {
            int preferredIndex = getPreferredRecoveryQuestionIndex(player.getSecurityQs());
            if (preferredIndex < 0 || preferredIndex >= storedAnswers.length) {
                return false;
            }
            return storedAnswers[preferredIndex].trim()
                    .equalsIgnoreCase(providedAnswers[0].trim());
        }

        if (providedAnswers.length != storedAnswers.length) return false;

        for (int i = 0; i < storedAnswers.length; i++) {
            if (!storedAnswers[i].trim().equalsIgnoreCase(providedAnswers[i].trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the password for a given user after successful verification.
     *
     * @param username the username of the account
     * @param newPassword the new password to set
     * @return {@code true} if the password was updated successfully; {@code false} otherwise
     */
    public boolean recoverPassword(String username, String newPassword) {
        Player player = dataStore.loadPlayer(username);
        if (player == null) return false;

        player.setPassword(newPassword);
        return dataStore.savePlayer(player);
    }

    /**
     * Retrieves the preferred security question for a user.
     *
     * @param username the username to look up
     * @return an array containing a single security question, or {@code null} if unavailable
     */
    public String[] getQuestionsForUser(String username) {
        Player player = dataStore.loadPlayer(username);
        if (player == null || player.getSecurityQs() == null) {
            return null;
        }

        int preferredIndex = getPreferredRecoveryQuestionIndex(player.getSecurityQs());
        if (preferredIndex < 0 || preferredIndex >= player.getSecurityQs().length) {
            return null;
        }

        String normalized = normalizeSecurityQuestion(player.getSecurityQs()[preferredIndex]);
        return new String[]{normalized};
    }

    /**
     * Returns the currently authenticated player.
     *
     * @return the current {@link Player}, or {@code null} if no user is logged in
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Logs out the current user by clearing the session.
     */
    public void logout() {
        this.currentPlayer = null;
    }

    /**
     * Determines the preferred index of a valid recovery question.
     *
     * @param questions array of security questions
     * @return the index of a preferred question, or -1 if none is valid
     */
    private int getPreferredRecoveryQuestionIndex(String[] questions) {
        if (questions == null || questions.length == 0) {
            return -1;
        }

        for (int i = 0; i < questions.length; i++) {
            String normalized = normalizeSecurityQuestion(questions[i]);
            if (isCurrentSupportedQuestion(normalized)) {
                return i;
            }
        }

        for (int i = 0; i < questions.length; i++) {
            if (questions[i] != null && !questions[i].isBlank()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks whether a question is one of the supported predefined questions.
     *
     * @param question the question to check
     * @return {@code true} if supported; {@code false} otherwise
     */
    private boolean isCurrentSupportedQuestion(String question) {
        return QUESTION_BIRTH_CITY.equals(question)
                || QUESTION_MAIDEN_NAME.equals(question)
                || QUESTION_FIRST_PET.equals(question)
                || QUESTION_CHILDHOOD_NICKNAME.equals(question);
    }

    /**
     * Normalizes a security question string into a standard format.
     *
     * @param question the raw question string
     * @return the normalized question string, or {@code null} if invalid
     */
    private String normalizeSecurityQuestion(String question) {
        if (question == null || question.isBlank()) {
            return null;
        }

        String normalized = question.trim().toLowerCase();
        return switch (normalized) {
            case "what city were you born in?", "birth city?" -> QUESTION_BIRTH_CITY;
            case "what is your mother's maiden name?", "mother's maiden name?" -> QUESTION_MAIDEN_NAME;
            case "what was the name of your first pet?", "first pet?" -> QUESTION_FIRST_PET;
            case "what was your childhood nickname?", "childhood nickname?" -> QUESTION_CHILDHOOD_NICKNAME;
            default -> question.trim();
        };
    }
}
