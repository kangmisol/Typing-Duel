package backend.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import backend.model.AppConfig;
import backend.model.HighScoreTable;
import backend.model.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central file-backed persistence service for Typing Duel data.
 *
 * <p>This class encapsulates all filesystem access for player profiles, high
 * scores, word banks, level data, and application configuration. Backend
 * services use it as the single entry point for persistent storage so the rest
 * of the application does not manipulate files directly.</p>
 *
 * <h2>Storage Layout</h2>
 * <pre>
 * /data
 *   /players
 *     {username}.json   one file per player account
 *   words.csv           word bank data in CSV format
 *   words.json          word bank data in JSON format
 *   highscores.json     global leaderboard
 *   levels.json         level configuration array
 *   config.json         application settings
 * </pre>
 *
 * <p>Google Gson is used for JSON serialization and deserialization, while CSV
 * files are handled with standard Java I/O utilities.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public class FileDataStore {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FileDataStore.class.getName());

    /** Sub-directory that holds individual player JSON files. */
    private static final String PLAYERS_DIR    = "players";

    /** Filename of the global leaderboard. */
    private static final String HIGHSCORES_FILE = "highscores.json";

    /** Filename of the default leaderboard template (for guardian restore). */
    private static final String HIGHSCORES_DEFAULT_FILE = "highscores_default.json";

    /** Filename of the word bank CSV (legacy). */
    private static final String WORDS_FILE      = "words.csv";

    /** Filename of the word bank JSON. */
    private static final String WORDS_JSON_FILE = "words.json";

    /** Filename of the level configuration array. */
    private static final String LEVELS_FILE     = "levels.json";

    /** Filename of the application config. */
    private static final String CONFIG_FILE     = "config.json";

    /** CSV column indices for the word bank (Design Doc Ã‚Â§5.3). */
    private static final int CSV_COL_TEXT       = 0;
    private static final int CSV_COL_DIFFICULTY = 1;
    private static final int CSV_COL_CATEGORY   = 2;
    private static final int CSV_COL_POINTS     = 3;
    private static final int CSV_EXPECTED_COLS  = 4;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Absolute path to the root data directory. */
    private final Path dataDir;

    /** Gson instance configured for pretty-printing and null serialization. */
    private final Gson gson;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a FileDataStore rooted at the given directory, creating any
     * missing sub-directories as needed (NFR 5.14).
     *
     * @param dataDirectory path to the data directory (e.g. {@code "./data"})
     * @throws IOException if the directories cannot be created
     */
    public FileDataStore(String dataDirectory) throws IOException {
        this.dataDir = Paths.get(dataDirectory).toAbsolutePath().normalize();
        this.gson    = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        // Ensure required directories exist (NFR 5.14: stay inside dataDir)
        Files.createDirectories(dataDir.resolve(PLAYERS_DIR));
    }

    /**
     * Protected no-arg constructor for testing stubs.
     * Subclasses can override load/save methods without touching the filesystem.
     */
    protected FileDataStore() {
        this.dataDir = null;
        this.gson    = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    // =========================================================================
    // Player account I/O
    // =========================================================================

    /**
     * Saves (or overwrites) the player's JSON file.
     *
     * <p>If the write fails, the previous file is left intact (FR 3.8.1 exception:
     * "Save Failed. Please Try Again." behaviour is triggered by the caller).</p>
     *
     * @param player the Player object to persist
     * @return {@code true} on success, {@code false} on any I/O error
     */
    public boolean savePlayer(Player player) {
        if (player == null || player.getUsername() == null) {
            LOG.warning("savePlayer: received null player or username");
            return false;
        }

        Path file = playerFilePath(player.getUsername());
        Path tmp  = file.resolveSibling(player.getUsername() + ".tmp");

        try {
            // Write to a temp file first, then atomically move it (prevents corruption)
            String json = gson.toJson(player);
            Files.writeString(tmp, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            LOG.fine("savePlayer: saved " + file);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "savePlayer: failed to write " + file, e);
            silentDelete(tmp);
            return false;
        }
    }

    /**
     * Loads a player from their JSON file.
     *
     * @param username the username to load (case-sensitive, matches filename)
     * @return the deserialized {@link Player}, or {@code null} if not found or corrupted
     */
    public Player loadPlayer(String username) {
        if (username == null) return null;

        Path file = playerFilePath(username);
        if (!Files.exists(file)) {
            LOG.fine("loadPlayer: file not found for " + username);
            return null;
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Player player = gson.fromJson(json, Player.class);
            if (player == null) {
                LOG.warning("loadPlayer: Gson returned null for " + username);
            }
            return player;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "loadPlayer: I/O error reading " + file, e);
            return null;
        } catch (Exception e) {
            // Corrupted JSON Ã¢â‚¬â€œ FR 3.8.2: "Save Data Corrupted. Restoring Default Profile."
            LOG.log(Level.WARNING, "loadPlayer: corrupted JSON for " + username, e);
            return null;
        }
    }

    /**
     * Checks whether a player file exists for the given username.
     *
     * @param username the username to check
     * @return {@code true} if the file exists
     */
    public boolean playerExists(String username) {
        return username != null && Files.exists(playerFilePath(username));
    }

    /**
     * Deletes the player file for the given username.
     * Used by Parent/Teacher account deletion (FR 3.7).
     *
     * @param username the username to delete
     * @return {@code true} if deleted, {@code false} if file did not exist or could not be deleted
     */
    public boolean deletePlayer(String username) {
        if (username == null) return false;
        Path file = playerFilePath(username);
        return silentDelete(file);
    }

    /**
     * Returns a list of all usernames for which a player file exists.
     * Used by the Parent/Teacher "Statistics" screen to enumerate players.
     *
     * @return list of usernames (without the .json extension), never {@code null}
     */
    public List<String> listAllUsernames() {
        List<String> names = new ArrayList<>();
        Path playersDir = dataDir.resolve(PLAYERS_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(playersDir, "*.json")) {
            for (Path p : stream) {
                String filename = p.getFileName().toString();
                names.add(filename.substring(0, filename.length() - 5)); // strip ".json"
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "listAllUsernames: cannot list players directory", e);
        }

        Collections.sort(names);
        return names;
    }

    /**
     * Returns a list of all player objects from the players directory.
     *
     * @return mutable list of all players; never {@code null}
     */
    public List<Player> loadPlayers() {
        List<Player> players = new ArrayList<>();
        for (String username : listAllUsernames()) {
            Player p = loadPlayer(username);
            if (p != null) {
                players.add(p);
            }
        }
        return players;
    }

    /**
     * Saves all players in the list, replacing any existing files.
     *
     * @param players the full list of players to persist
     */
    public void savePlayers(List<Player> players) {
        if (players == null) return;
        for (Player p : players) {
            savePlayer(p);
        }
    }

    // =========================================================================
    // High score table I/O
    // =========================================================================

    /**
     * Saves the global high score table to {@code highscores.json}.
     *
     * @param table the {@link HighScoreTable} to persist
     * @return {@code true} on success
     */
    public boolean saveHighScores(HighScoreTable table) {
        return writeJson(dataDir.resolve(HIGHSCORES_FILE), table);
    }

    /**
     * Loads the global high score table from {@code highscores.json}.
     *
     * @return the deserialized table, or a fresh empty table if the file is
     *         missing or corrupted (FR 3.9.3: show placeholder values)
     */
    public HighScoreTable loadHighScores() {
        Path file = dataDir.resolve(HIGHSCORES_FILE);
        if (!Files.exists(file)) return new HighScoreTable();

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            HighScoreTable table = gson.fromJson(json, HighScoreTable.class);
            return table != null ? table : new HighScoreTable();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "loadHighScores: error reading " + file, e);
            return new HighScoreTable();
        }
    }

    /**
     * Loads the default leaderboard from {@code highscores_default.json}.
     * Used by the "Restore Default Leaderboard" guardian feature (FR 3.7).
     *
     * @return the default table, or a fresh empty table if the file is missing
     */
    public HighScoreTable loadDefaultHighScores() {
        Path file = dataDir.resolve(HIGHSCORES_DEFAULT_FILE);
        if (!Files.exists(file)) {
            LOG.warning("loadDefaultHighScores: template not found at " + file);
            return new HighScoreTable();
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            HighScoreTable table = gson.fromJson(json, HighScoreTable.class);
            return table != null ? table : new HighScoreTable();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "loadDefaultHighScores: error reading " + file, e);
            return new HighScoreTable();
        }
    }

    /**
     * Restores the global leaderboard to its default state by loading
     * {@code highscores_default.json} and writing it as {@code highscores.json}.
     *
     * @return {@code true} on success
     */
    public boolean restoreDefaultHighScores() {
        HighScoreTable defaults = loadDefaultHighScores();
        return saveHighScores(defaults);
    }

    // =========================================================================
    // Word bank I/O
    // =========================================================================

    /**
     * Represents a single word entry loaded from {@code words.json}.
     *
     * <p>This lightweight data carrier mirrors the JSON schema used for stored
     * word bank entries.</p>
     *
     * @author Mrida Hingmire
     */
    public static class WordEntry {
        private String text;
        private String difficulty;
        private String category;
        private int pointValue;

        /**
         * Creates an empty word entry for Gson deserialization.
         */
        public WordEntry() {}

        /**
         * Creates a fully populated word entry.
         *
         * @param text the word text presented to the player
         * @param difficulty the difficulty label associated with the word
         * @param category the thematic category assigned to the word
         * @param pointValue the score value granted for typing the word
         */
        public WordEntry(String text, String difficulty, String category, int pointValue) {
            this.text = text;
            this.difficulty = difficulty;
            this.category = category;
            this.pointValue = pointValue;
        }

        /**
         * Returns the displayed text for this word entry.
         *
         * @return the word text
         */
        public String getText()       { return text; }

        /**
         * Returns the difficulty label assigned to this word entry.
         *
         * @return the difficulty label
         */
        public String getDifficulty() { return difficulty; }

        /**
         * Returns the category assigned to this word entry.
         *
         * @return the category name
         */
        public String getCategory()   { return category; }

        /**
         * Returns the score value associated with this word entry.
         *
         * @return the point value awarded for the word
         */
        public int getPointValue()    { return pointValue; }
    }

    /**
     * Loads word data from {@code words.json}.
     *
     * <p>Expected JSON structure:</p>
     * <pre>
     * { "words": [ { "text": "cat", "difficulty": "EASY", ... }, ... ] }
     * </pre>
     *
     * @return list of word entries; never {@code null}
     */
    public List<WordEntry> loadWordsJSON() {
        List<WordEntry> words = new ArrayList<>();
        Path file = dataDir.resolve(WORDS_JSON_FILE);

        if (!Files.exists(file)) {
            LOG.warning("loadWordsJSON: words.json not found at " + file);
            return words;
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            com.google.gson.JsonArray arr = root.getAsJsonArray("words");

            if (arr != null) {
                for (com.google.gson.JsonElement elem : arr) {
                    WordEntry entry = gson.fromJson(elem, WordEntry.class);
                    if (entry != null && entry.getText() != null) {
                        words.add(entry);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "loadWordsJSON: error reading " + file, e);
        }

        LOG.fine("loadWordsJSON: loaded " + words.size() + " words");
        return words;
    }

    // =========================================================================
    // Word bank CSV I/O (legacy)
    // =========================================================================

    /**
     * Loads word data from {@code words.csv}.
     *
     * <p>Each row is returned as a String array with columns:
     * [text, difficulty, category, pointValue] (Design Doc Ã‚Â§5.3).</p>
     *
     * <p>The header row is skipped automatically. Rows with fewer than
     * {@value #CSV_EXPECTED_COLS} columns are skipped with a warning.</p>
     *
     * @return list of String arrays, one per valid word row; never {@code null}
     */
    public List<String[]> loadWordsCSV() {
        List<String[]> rows = new ArrayList<>();
        Path file = dataDir.resolve(WORDS_FILE);

        if (!Files.exists(file)) {
            LOG.warning("loadWordsCSV: words.csv not found at " + file);
            return rows;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {           // skip header
                    firstLine = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < CSV_EXPECTED_COLS) {
                    LOG.warning("loadWordsCSV: skipping malformed row: " + line);
                    continue;
                }
                // Trim whitespace from each column
                for (int i = 0; i < cols.length; i++) cols[i] = cols[i].trim();
                rows.add(cols);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "loadWordsCSV: error reading " + file, e);
        }

        LOG.fine("loadWordsCSV: loaded " + rows.size() + " words");
        return rows;
    }

    /**
     * Writes word data back to {@code words.csv} with the standard header.
     * Useful for tools that need to persist a modified word bank.
     *
     * @param rows list of String arrays, each with at least 4 elements
     * @return {@code true} on success
     */
    public boolean saveWordsCSV(List<String[]> rows) {
        Path file = dataDir.resolve(WORDS_FILE);

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // Header
            writer.write("text,difficulty,category,pointValue");
            writer.newLine();

            for (String[] row : rows) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "saveWordsCSV: error writing " + file, e);
            return false;
        }
    }

    // =========================================================================
    // Level configuration I/O
    // =========================================================================

    /**
     * Loads the level configuration array from {@code levels.json} and returns
     * the raw JSON string. The caller (LevelManager) is responsible for
     * deserializing into the appropriate type.
     *
     * <p>A generic approach is used here so that the storage layer has no
     * dependency on the game-logic layer (clean separation of concerns).</p>
     *
     * @param type Gson TypeToken for the target collection type
     * @param <T>  the target type
     * @return the deserialized object, or {@code null} on error
     */
    public <T> T loadLevels(Type type) {
        return readJson(dataDir.resolve(LEVELS_FILE), type);
    }

    /**
     * Saves the level configuration array to {@code levels.json}.
     *
     * @param levels the level data to persist
     * @return {@code true} on success
     */
    public boolean saveLevels(Object levels) {
        return writeJson(dataDir.resolve(LEVELS_FILE), levels);
    }

    // =========================================================================
    // Application config I/O
    // =========================================================================

    /**
     * Loads {@code config.json} and populates the {@link AppConfig} singleton.
     * If the file is missing, default values are applied (NFR 5.25).
     *
     * @return the loaded (or default) {@link AppConfig}
     */
    public AppConfig loadConfig() {
        Path file = dataDir.resolve(CONFIG_FILE);

        if (!Files.exists(file)) {
            LOG.info("loadConfig: config.json not found Ã¢â‚¬â€œ using defaults");
            AppConfig defaults = AppConfig.loadDefaults();
            AppConfig.setInstance(defaults);
            return defaults;
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            AppConfig cfg = gson.fromJson(json, AppConfig.class);
            if (cfg == null) cfg = AppConfig.loadDefaults();
            AppConfig.setInstance(cfg);
            return cfg;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "loadConfig: error reading " + file + " Ã¢â‚¬â€œ using defaults", e);
            AppConfig defaults = AppConfig.loadDefaults();
            AppConfig.setInstance(defaults);
            return defaults;
        }
    }

    /**
     * Saves the application configuration to {@code config.json}.
     *
     * @param config the {@link AppConfig} to persist
     * @return {@code true} on success
     */
    public boolean saveConfig(AppConfig config) {
        return writeJson(dataDir.resolve(CONFIG_FILE), config);
    }

    // =========================================================================
    // Generic JSON helpers (private)
    // =========================================================================

    /**
     * Reads and deserializes a JSON file to the given type.
     *
     * @param path the file to read
     * @param type the Gson type token
     * @param <T>  the target type
     * @return the deserialized object, or {@code null} on error
     */
    private <T> T readJson(Path path, Type type) {
        if (!Files.exists(path)) return null;
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return gson.fromJson(json, type);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "readJson: error reading " + path, e);
            return null;
        }
    }

    /**
     * Serializes and writes an object to a JSON file.
     *
     * @param path   the destination file
     * @param object the object to serialize
     * @return {@code true} on success
     */
    private boolean writeJson(Path path, Object object) {
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            String json = gson.toJson(object);
            Files.writeString(tmp, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "writeJson: error writing " + path, e);
            silentDelete(tmp);
            return false;
        }
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /**
     * Resolves the path for a player's JSON file.
     *
     * @param username the username (used as file stem)
     * @return absolute {@link Path} within the players sub-directory
     */
    private Path playerFilePath(String username) {
        return dataDir.resolve(PLAYERS_DIR).resolve(username + ".json");
    }

    /**
     * Deletes a file without throwing if the file does not exist.
     *
     * @param path the file to delete
     * @return {@code true} if deleted successfully
     */
    private boolean silentDelete(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.log(Level.FINE, "silentDelete: could not delete " + path, e);
            return false;
        }
    }

    /**
     * Returns the absolute path of the data directory managed by this store.
     * Useful for diagnostics and logging.
     *
     * @return the data directory path
     */
    public Path getDataDir() { return dataDir; }
}
