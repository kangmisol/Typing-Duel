package backend.model;

/**
 * The {@code AppConfig} class represents a centralized configuration manager
 * for the Typing Duel application.
 *
 * <p>This class follows the <b>Singleton design pattern</b>, ensuring that a single,
 * globally accessible configuration instance is used throughout the application.</p>
 *
 * <p>Configuration values are typically loaded from a JSON file located at
 * {@code /data/config.json}. If the file is missing or fails to load, the system
 * falls back to default values to ensure graceful degradation.</p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Store global application settings (e.g., lives, volume, file paths)</li>
 *     <li>Provide default values for safe execution</li>
 *     <li>Support runtime validation of non-functional requirements (NFRs)</li>
 * </ul>
 *
 * <p><b>Design Reference:</b> Section 5.5</p>
 * <p><b>Non-Functional Requirements:</b> NFR 5.1, 5.3, 5.13, 5.16, 5.17, 5.20, 5.24</p>
 *
 * <p>Team 49 – CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public class AppConfig {

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    /** Single shared instance of AppConfig. */
    private static AppConfig instance;

    /**
     * Returns the singleton instance of {@code AppConfig}.
     *
     * <p>If no instance has been loaded yet, a default configuration is created
     * and returned.</p>
     *
     * @return the global AppConfig instance
     */
    public static AppConfig getInstance() {
        if (instance == null) instance = loadDefaults();
        return instance;
    }

    /**
     * Sets the singleton instance after loading configuration from file.
     *
     * <p>This method is typically called by the data layer after deserializing
     * configuration data from JSON.</p>
     *
     * @param loaded the fully initialized AppConfig instance
     */
    public static void setInstance(AppConfig loaded) {
        instance = loaded;
    }

    // -------------------------------------------------------------------------
    // Configuration fields
    // -------------------------------------------------------------------------

    /** Number of lives a player starts with (default: 3). */
    private int defaultLives;

    /** PIN required to access parent/teacher controls. */
    private String parentPin;

    /** Default master volume level (range: 0.0 to 1.0). */
    private double defaultVolume;

    /** Path to the application's data directory. */
    private String dataDirectory;

    // -------------------------------------------------------------------------
    // Non-Functional Requirement (NFR) fields
    // -------------------------------------------------------------------------

    /** Minimum required Java version for execution. */
    private String minJavaVersion;

    /** Maximum allowed project size in megabytes. */
    private int maxProjectSizeMB;

    /** Target frame time in milliseconds (~16 ms for 60 FPS). */
    private int targetFrameTimeMs;

    /** Minimum recommended age rating for content. */
    private int minimumAgeRating;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor required for JSON deserialization.
     */
    public AppConfig() {}

    /**
     * Creates and returns an {@code AppConfig} instance populated with default values.
     *
     * @return a default configuration instance
     */
    public static AppConfig loadDefaults() {
        AppConfig cfg = new AppConfig();
        cfg.defaultLives      = 3;
        cfg.parentPin         = "";
        cfg.defaultVolume     = 0.8;
        cfg.dataDirectory     = "./data";
        cfg.minJavaVersion    = "23";
        cfg.maxProjectSizeMB  = 500;
        cfg.targetFrameTimeMs = 16;
        cfg.minimumAgeRating  = 8;
        return cfg;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * Returns the default number of lives per game.
     *
     * @return the default lives value
     */
    public int getDefaultLives() {
        return defaultLives;
    }

    /**
     * Sets the default number of lives per game.
     *
     * @param defaultLives the new default lives value
     */
    public void setDefaultLives(int defaultLives) {
        this.defaultLives = defaultLives;
    }

    /**
     * Returns the parent/guardian PIN.
     *
     * @return the parent PIN string
     */
    public String getParentPin() {
        return parentPin;
    }

    /**
     * Sets the parent/guardian PIN.
     *
     * @param parentPin the new PIN value
     */
    public void setParentPin(String parentPin) {
        this.parentPin = parentPin;
    }

    /**
     * Returns the default audio volume.
     *
     * @return the default volume (0.0 to 1.0)
     */
    public double getDefaultVolume() {
        return defaultVolume;
    }

    /**
     * Sets the default audio volume.
     *
     * @param defaultVolume the new volume value
     */
    public void setDefaultVolume(double defaultVolume) {
        this.defaultVolume = defaultVolume;
    }

    /**
     * Returns the configured data directory path.
     *
     * @return the data directory path
     */
    public String getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Sets the data directory path.
     *
     * @param dataDirectory the new directory path
     */
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Returns the minimum required Java version.
     *
     * @return the Java version string
     */
    public String getMinJavaVersion() {
        return minJavaVersion;
    }

    /**
     * Sets the minimum required Java version.
     *
     * @param minJavaVersion the new Java version requirement
     */
    public void setMinJavaVersion(String minJavaVersion) {
        this.minJavaVersion = minJavaVersion;
    }

    /**
     * Returns the maximum allowed project size.
     *
     * @return the maximum size in MB
     */
    public int getMaxProjectSizeMB() {
        return maxProjectSizeMB;
    }

    /**
     * Sets the maximum allowed project size.
     *
     * @param maxProjectSizeMB the new maximum size in MB
     */
    public void setMaxProjectSizeMB(int maxProjectSizeMB) {
        this.maxProjectSizeMB = maxProjectSizeMB;
    }

    /**
     * Returns the target frame time.
     *
     * @return the frame time in milliseconds
     */
    public int getTargetFrameTimeMs() {
        return targetFrameTimeMs;
    }

    /**
     * Sets the target frame time.
     *
     * @param targetFrameTimeMs the new frame time value
     */
    public void setTargetFrameTimeMs(int targetFrameTimeMs) {
        this.targetFrameTimeMs = targetFrameTimeMs;
    }

    /**
     * Returns the minimum age rating.
     *
     * @return the age rating value
     */
    public int getMinimumAgeRating() {
        return minimumAgeRating;
    }

    /**
     * Sets the minimum age rating.
     *
     * @param minimumAgeRating the new age rating value
     */
    public void setMinimumAgeRating(int minimumAgeRating) {
        this.minimumAgeRating = minimumAgeRating;
    }

    /**
     * Returns a string representation of the configuration.
     *
     * @return a formatted string containing key configuration values
     */
    @Override
    public String toString() {
        return "AppConfig{dataDirectory='" + dataDirectory +
                "', defaultLives=" + defaultLives + "}";
    }
}
