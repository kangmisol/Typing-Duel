package backend.utils;

import backend.model.AppConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Performs automated runtime checks for selected non-functional requirements.
 *
 * <p>Call {@link #runAll(String)} during application startup to validate runtime
 * environment assumptions such as Java version, installation size limits,
 * configured frame-time targets, and data-directory scope. Any detected
 * violations are returned to the caller and also logged for diagnostics.</p>
 *
 * <p>This class is stateless and exposes only static methods so it can be used
 * early in application startup before other subsystems are initialized.</p>
 *
 * <table border="1">
 *   <caption>NFRs verified by this class</caption>
 *   <tr><th>NFR ID</th><th>Requirement summary</th><th>Method</th></tr>
 *   <tr><td>5.1 / 5.13</td><td>Java 23+ runtime</td><td>{@link #checkJavaVersion}</td></tr>
 *   <tr><td>5.16</td><td>Project Ã¢â€°Â¤ 500 MB</td><td>{@link #checkProjectSize}</td></tr>
 *   <tr><td>5.14</td><td>No file access outside install dir</td><td>Enforced by FileDataStore; documented here</td></tr>
 *   <tr><td>5.17 / 5.24</td><td>Responsive input (target frame time noted in config)</td><td>{@link #checkFrameTimeConfig}</td></tr>
 *   <tr><td>5.3 / 5.20</td><td>Content age rating / English language</td><td>Manual / content inspection (noted)</td></tr>
 *   <tr><td>5.2</td><td>OO design principles</td><td>Architectural Ã¢â‚¬â€œ verified via code review</td></tr>
 *   <tr><td>5.7</td><td>Source stored in GitLab repo</td><td>Process Ã¢â‚¬â€œ verified via repo inspection</td></tr>
 *   <tr><td>5.11</td><td>JUnit 5 tests present</td><td>Build tool Ã¢â‚¬â€œ verified at compile time</td></tr>
 * </table>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 * @author Misol Kang
 * @author Atika Hussain
 */
public final class NFRChecker {

    private static final Logger LOG = Logger.getLogger(NFRChecker.class.getName());

    /**
     * Prevents instantiation of this utility class.
     */
    private NFRChecker() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Runs all automated NFR checks and returns a list of violation messages.
     * An empty list means all checks passed.
     *
     * @param installRoot the root directory of the application install
     * @return list of human-readable violation strings (empty if all pass)
     */
    public static List<String> runAll(String installRoot) {
        List<String> violations = new ArrayList<>();

        checkJavaVersion(violations);
        checkProjectSize(installRoot, violations);
        checkFrameTimeConfig(violations);
        checkDataDirectoryScope(installRoot, violations);

        if (violations.isEmpty()) {
            LOG.info("NFRChecker: all automated checks passed");
        } else {
            violations.forEach(v -> LOG.warning("NFR VIOLATION: " + v));
        }
        return violations;
    }

    // =========================================================================
    // Individual checks
    // =========================================================================

    /**
     * NFR 5.1 / 5.13 Ã¢â‚¬â€œ Verifies that the runtime Java version is 23 or later.
     * Logs a warning and adds a violation entry if the check fails.
     *
     * @param violations mutable list to append violations to
     */
    public static void checkJavaVersion(List<String> violations) {
        String version = System.getProperty("java.version", "0");
        int major = parseMajorVersion(version);

        String requiredStr = AppConfig.getInstance().getMinJavaVersion();
        int required;
        try {
            required = Integer.parseInt(requiredStr);
        } catch (NumberFormatException e) {
            required = 23; // fallback
        }

        if (major < required) {
            String msg = "NFR 5.1/5.13 Ã¢â‚¬â€œ Java " + required + "+ required; detected Java " + major
                    + " (java.version=" + version + ")";
            violations.add(msg);
        } else {
            LOG.fine("NFRChecker.checkJavaVersion: OK (Java " + major + ")");
        }
    }

    /**
     * NFR 5.16 Ã¢â‚¬â€œ Verifies that the total size of the install directory does
     * not exceed {@code AppConfig.maxProjectSizeMB} (default 500 MB).
     *
     * @param installRoot the root directory to measure
     * @param violations  mutable list to append violations to
     */
    public static void checkProjectSize(String installRoot, List<String> violations) {
        int limitMB = AppConfig.getInstance().getMaxProjectSizeMB();
        long limitBytes = (long) limitMB * 1024 * 1024;

        File root = new File(installRoot);
        if (!root.exists()) {
            LOG.fine("NFRChecker.checkProjectSize: install root not found, skipping");
            return;
        }

        long totalBytes = directorySize(root);
        long totalMB    = totalBytes / (1024 * 1024);

        if (totalBytes > limitBytes) {
            String msg = "NFR 5.16 Ã¢â‚¬â€œ Project size " + totalMB + " MB exceeds limit of " + limitMB + " MB";
            violations.add(msg);
        } else {
            LOG.fine("NFRChecker.checkProjectSize: OK (" + totalMB + " MB / " + limitMB + " MB)");
        }
    }

    /**
     * NFR 5.17 / 5.24 Ã¢â‚¬â€œ Logs the configured target frame time so that the
     * game loop implementer knows the budget. Does not fail; purely advisory.
     *
     * @param violations mutable list (nothing added by this check)
     */
    public static void checkFrameTimeConfig(List<String> violations) {
        int targetMs = AppConfig.getInstance().getTargetFrameTimeMs();
        LOG.info("NFRChecker.checkFrameTimeConfig: target frame time = " + targetMs
                + " ms (~" + (1000 / targetMs) + " fps) Ã¢â‚¬â€œ enforced by game loop");
        // No violation raised; the game loop is responsible for meeting this target.
    }

    /**
     * NFR 5.14 Ã¢â‚¬â€œ Documents that FileDataStore enforces the no-outside-access
     * rule by resolving all paths relative to the data directory. Logs a
     * confirmation; raises a violation only if the data directory escapes the
     * install root.
     *
     * @param installRoot the application install root
     * @param violations  mutable list to append violations to
     */
    public static void checkDataDirectoryScope(String installRoot, List<String> violations) {
        String dataDir = AppConfig.getInstance().getDataDirectory();
        File dataFile  = new File(dataDir).getAbsoluteFile();
        File rootFile  = new File(installRoot).getAbsoluteFile();

        if (!dataFile.toPath().startsWith(rootFile.toPath())) {
            String msg = "NFR 5.14 Ã¢â‚¬â€œ data directory (" + dataFile
                    + ") is outside the install root (" + rootFile + ")";
            violations.add(msg);
        } else {
            LOG.fine("NFRChecker.checkDataDirectoryScope: OK (data dir is inside install root)");
        }
    }

    // =========================================================================
    // NFRs verified through other means (documented only)
    // =========================================================================

    /*
     * The following NFRs are enforced through process, architecture, or tooling
     * rather than runtime checks. They are listed here for traceability:
     *
     * NFR 5.2  Ã¢â‚¬â€œ OO design (classes, interfaces, inheritance, polymorphism)
     *            Ã¢â€ â€™ Verified via code review against class diagrams.
     *
     * NFR 5.3  Ã¢â‚¬â€œ Age-appropriate content (8+), no offensive material
     *            Ã¢â€ â€™ Verified via content inspection checklist.
     *
     * NFR 5.4  Ã¢â‚¬â€œ GUI with clear labels, consistent styling, UX best practices
     *            Ã¢â€ â€™ Verified via usability testing and interface inspection.
     *
     * NFR 5.5  Ã¢â‚¬â€œ Local file persistence across sessions
     *            Ã¢â€ â€™ Verified by running two consecutive sessions and checking data.
     *
     * NFR 5.6  Ã¢â‚¬â€œ Freely available third-party libraries; compile/run instructions
     *            Ã¢â€ â€™ Verified via dependency inspection and README review.
     *
     * NFR 5.7  Ã¢â‚¬â€œ Source in Western GitLab repository
     *            Ã¢â€ â€™ Verified via repository and commit history inspection.
     *
     * NFR 5.8  Ã¢â‚¬â€œ Design artifacts on GitLab Wiki
     *            Ã¢â€ â€™ Verified via Wiki content inspection.
     *
     * NFR 5.9  Ã¢â‚¬â€œ GitLab issue tracking used throughout development
     *            Ã¢â€ â€™ Verified via issue tracker review.
     *
     * NFR 5.10 Ã¢â‚¬â€œ Javadoc for all classes and methods; file-level headers; AI citation
     *            Ã¢â€ â€™ Verified via code inspection and Javadoc generation.
     *
     * NFR 5.11 Ã¢â‚¬â€œ JUnit 5 tests covering non-GUI logic
     *            Ã¢â€ â€™ Verified by running test suite and reviewing coverage report.
     *
     * NFR 5.12 Ã¢â‚¬â€œ Consistent coding style and naming conventions
     *            Ã¢â€ â€™ Verified via code review checklist.
     *
     * NFR 5.15 Ã¢â‚¬â€œ Immediate visual feedback for every user action
     *            Ã¢â€ â€™ Verified via usability testing and interaction walkthrough.
     *
     * NFR 5.18 Ã¢â‚¬â€œ Keyboard + mouse input, logical tab order, sufficient colour contrast
     *            Ã¢â€ â€™ Verified via accessibility inspection and keyboard-only testing.
     *
     * NFR 5.19 Ã¢â‚¬â€œ Modular design, clear separation of responsibilities
     *            Ã¢â€ â€™ Verified via architecture review and code inspection.
     *
     * NFR 5.20 Ã¢â‚¬â€œ All text/docs/comments/communication in clear English
     *            Ã¢â€ â€™ Verified via documentation and code inspection.
     *
     * NFR 5.21 Ã¢â‚¬â€œ Adherence to sound SE principles per course material
     *            Ã¢â€ â€™ Verified via design and code review against course standards.
     *
     * NFR 5.22 Ã¢â‚¬â€œ WPM/response-time measurement with sufficient precision
     *            Ã¢â€ â€™ Verified via timing accuracy testing.
     *
     * NFR 5.23 Ã¢â‚¬â€œ Player scores and stats persist across multiple sessions
     *            Ã¢â€ â€™ Verified by gameplay session testing across multiple runs.
     *
     * NFR 5.25 Ã¢â‚¬â€œ No crash on unexpected/invalid input
     *            Ã¢â€ â€™ Verified via stress testing with invalid and rapid input.
     *
     * NFR 5.26 Ã¢â‚¬â€œ Deterministic, consistent scoring and accuracy calculations
     *            Ã¢â€ â€™ Verified by replaying identical input and comparing results.
     *
     * NFR 5.27 Ã¢â‚¬â€œ Font size and volume adjustable at runtime without restart
     *            Ã¢â€ â€™ Verified via settings adjustment testing.
     *
     * NFR 5.28 Ã¢â‚¬â€œ Readable font sizes and spacing suitable for fast reading/typing
     *            Ã¢â€ â€™ Verified via UI inspection and readability testing.
     */

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Parses the major version number from a Java version string.
     * Handles both legacy format ("1.8.0_xxx") and modern format ("23.0.1").
     *
     * @param version the value of {@code System.getProperty("java.version")}
     * @return the major version integer, or 0 if unparseable
     */
    static int parseMajorVersion(String version) {
        if (version == null || version.isEmpty()) return 0;
        try {
            String[] parts = version.split("[.\\-+]");
            int first = Integer.parseInt(parts[0]);
            // Legacy: "1.x.y" Ã¢â€ â€™ major is x
            if (first == 1 && parts.length > 1) return Integer.parseInt(parts[1]);
            return first;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Recursively computes the total size of all files under a directory.
     *
     * @param dir the root directory to measure
     * @return total size in bytes
     */
    private static long directorySize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files == null) return size;
        for (File f : files) {
            size += f.isDirectory() ? directorySize(f) : f.length();
        }
        return size;
    }
}
