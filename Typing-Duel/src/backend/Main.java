package backend;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import frontend.MainFrame;

/**
 * Launches the Typing Duel desktop application.
 *
 * <p>This utility class provides the application's entry point, schedules GUI
 * startup on the Swing event-dispatch thread, applies the system look and feel,
 * and creates the main application window.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public final class Main {
    /**
     * Prevents instantiation of this utility class.
     */
    private Main() {
    }


    /**
     * Starts the application and opens the main window on the Swing UI thread.
     *
     * @param args command-line arguments; ignored by this application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applyLookAndFeel();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    /**
     * Applies the host platform's Swing look and feel when available.
     */
    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Falls back to the default look and feel if the system one is unavailable.
        }
    }
}
