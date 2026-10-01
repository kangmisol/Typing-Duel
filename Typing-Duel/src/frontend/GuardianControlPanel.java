// package frontend;

// import backend.logic.AuthService;
// import backend.logic.GuardianService;
// import backend.model.Player;
// import java.awt.BorderLayout;
// import java.awt.GridLayout;
// import java.util.List;
// import javax.swing.JComboBox;
// import javax.swing.JOptionPane;
// import javax.swing.JPanel;
// import javax.swing.JPasswordField;
// import javax.swing.JScrollPane;
// import javax.swing.JTextArea;

// public class GuardianControlPanel extends BaseScreenPanel {
//     private final GuardianService guardianService;
//     private final AuthService authService;
//     private final JComboBox<String> playerSelect;
//     private final JTextArea statsArea;

//     public GuardianControlPanel(ScreenManager screenManager, GuardianService guardianService,
//             AuthService authService) {

//         super("Guardian Control Menu");
//         this.guardianService = guardianService;
//         this.authService = authService;

//         JPanel shell = createMockFrame();
//         shell.setLayout(new BorderLayout(20, 20));

//         JPanel controls = new JPanel(new GridLayout(0, 1, 12, 12));
//         controls.setOpaque(false);

//         this.playerSelect = new JComboBox<>();
//         javax.swing.JButton restoreButton = createMenuButton("[ Restore Default Leaderboard ]");
//         javax.swing.JButton updateCodeButton = createMenuButton("[ Update Guardian Code ]");
//         javax.swing.JButton resetPasswordButton = createMenuButton("[ Reset Password ]");
//         javax.swing.JButton resetButton = createMenuButton("[ Reset Player ]");
//         javax.swing.JButton createAccountButton = createMenuButton("[ Create Account ]");
//         javax.swing.JButton backButton = createMenuButton("[ Back to Main Menu ]");

//         controls.add(playerSelect);
//         controls.add(restoreButton);
//         controls.add(updateCodeButton);
//         controls.add(resetPasswordButton);
//         controls.add(resetButton);
//         controls.add(createAccountButton);
//         controls.add(backButton);

//         this.statsArea = new JTextArea();

//         statsArea.setEditable(false);
//         statsArea.setForeground(new java.awt.Color(245, 235, 210));
//         statsArea.setBackground(new java.awt.Color(28, 28, 42));
//         statsArea.setBorder(OUTLINE);

//         restoreButton.addActionListener(event -> {
//             guardianService.restoreDefaultLeaderboard();
//             JOptionPane.showMessageDialog(this, "Leaderboard restored.");
//         });

//         updateCodeButton.addActionListener(event -> {
//             Player currentGuardian = this.authService.getCurrentPlayer();
//             if (!guardianService.isGuardian(currentGuardian)) {
//                 JOptionPane.showMessageDialog(
//                         this,
//                         "Current guardian session not found.",
//                         "Guardian Session Required",
//                         JOptionPane.ERROR_MESSAGE);
//                 return;
//             }

//             JPasswordField codeField = new JPasswordField(16);
//             int result = JOptionPane.showConfirmDialog(
//                     this,
//                     codeField,
//                     "Enter new guardian code",
//                     JOptionPane.OK_CANCEL_OPTION,
//                     JOptionPane.PLAIN_MESSAGE);

//             if (result != JOptionPane.OK_OPTION) {
//                 return;
//             }

//             try {
//                 boolean updated = this.guardianService.updateGuardianPin(
//                                 currentGuardian.getUsername(),
//                                 new String(codeField.getPassword()));
//                 if (updated) {
//                     currentGuardian.setGuardianCode(new String(codeField.getPassword()).trim());
//                 }
//                 JOptionPane.showMessageDialog(
//                         this,
//                         updated ? "Guardian code updated."
//                                 : "Unable to save guardian code.",
//                         updated ? "Guardian Code" : "Save Failed",
//                         updated ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
//             } catch (IllegalArgumentException ex) {
//                 JOptionPane.showMessageDialog(
//                         this,
//                         ex.getMessage(),
//                         "Invalid Guardian Code",
//                         JOptionPane.ERROR_MESSAGE);
//             }
//         });

//         resetPasswordButton.addActionListener(event -> {
//             String selectedUser = String.valueOf(playerSelect.getSelectedItem());
//             if (selectedUser == null || selectedUser.isBlank() || "null".equals(selectedUser)) {
//                 JOptionPane.showMessageDialog(
//                         this,
//                         "Select an account to reset the password.",
//                         "No Account Selected",
//                         JOptionPane.ERROR_MESSAGE);
//                 return;
//             }

//             JPasswordField passwordField = new JPasswordField(16);
//             int result = JOptionPane.showConfirmDialog(
//                     this,
//                     passwordField,
//                     "Enter new password for " + selectedUser,
//                     JOptionPane.OK_CANCEL_OPTION,
//                     JOptionPane.PLAIN_MESSAGE);

//             if (result != JOptionPane.OK_OPTION) {
//                 return;
//             }

//             try {
//                 Player currentGuardian = this.authService.getCurrentPlayer();
//                 boolean updated = currentGuardian != null
//                         && this.guardianService.resetPasswordAsGuardian(
//                                 currentGuardian.getUsername(),
//                                 selectedUser,
//                                 new String(passwordField.getPassword()));
//                 JOptionPane.showMessageDialog(
//                         this,
//                         updated ? "Password reset for " + selectedUser + "."
//                                 : "Unable to reset password for that account.",
//                         updated ? "Password Reset" : "Reset Failed",
//                         updated ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
//             } catch (IllegalArgumentException ex) {
//                 JOptionPane.showMessageDialog(
//                         this,
//                         ex.getMessage(),
//                         "Invalid Password",
//                         JOptionPane.ERROR_MESSAGE);
//             }
//         });


//         resetButton.addActionListener(event -> {
//             String selectedUser = String.valueOf(playerSelect.getSelectedItem());
//             if (selectedUser == null || selectedUser.isBlank() || "null".equals(selectedUser)) {
//                 JOptionPane.showMessageDialog(
//                         this,
//                         "Select a player account to reset.",
//                         "No Account Selected",
//                         JOptionPane.ERROR_MESSAGE);
//                 return;
//             }

//             this.guardianService.resetPlayerProgress(selectedUser);
//             statsArea.setText(loadStatistics(this.guardianService));
//             JOptionPane.showMessageDialog(this, "Player reset.");
//         });

//         createAccountButton.addActionListener(event ->
//                 screenManager.showScreen(ScreenManager.REGISTER));

//         backButton.addActionListener(event -> {
//             this.authService.logout();
//             screenManager.showScreen(ScreenManager.MAIN_MENU);
//         });

//         JScrollPane statsScrollPane = new JScrollPane(statsArea);
//         statsScrollPane.setPreferredSize(new java.awt.Dimension(0, 260));
//         shell.add(controls, BorderLayout.NORTH);
//         shell.add(statsScrollPane, BorderLayout.CENTER);
//         add(shell, BorderLayout.CENTER);
//     }

//     @Override
//     public void setVisible(boolean visible) {
//         super.setVisible(visible);
//         if (visible) {
//             refreshPlayerList();
//             statsArea.setText(loadStatistics(guardianService));
//         }
//     }

//     private void refreshPlayerList() {
//         playerSelect.removeAllItems();
//         for (String username : loadUsernames(guardianService, authService)) {
//             playerSelect.addItem(username);
//         }
//     }

//     private String[] loadUsernames(GuardianService service, AuthService authService) {
//         List<Player> players = service.getAllPlayers();
//         Player currentGuardian = authService.getCurrentPlayer();
//         String currentGuardianName = currentGuardian != null ? currentGuardian.getUsername() : null;
//         return players.stream()
//                 .filter(player -> !service.isGuardian(player)
//                         || (currentGuardianName != null
//                         && player.getUsername().equalsIgnoreCase(currentGuardianName)))
//                 .map(Player::getUsername)
//                 .toArray(String[]::new);
//     }

//     private String loadStatistics(GuardianService service) {
//         StringBuilder builder = new StringBuilder();
//         for (Player p : service.getAllPlayers()) {
//             if (service.isGuardian(p)) {
//                 continue;
//             }
//             builder.append(p.getUsername())
//                 .append(" | High Score: ").append(p.getStats().getHighScore())
//                 .append(" | Level Progress: ").append(p.getHighestLevelUnlocked() + 1)
//                 .append("/3")
//                 .append(" | Highest Level Reached: ").append(p.getStats().getHighestLevel())
//                 .append(" | Avg WPM: ").append(String.format("%.1f", p.getStats().getAverageWPM()))
//                 .append(" | Accuracy: ").append(String.format("%.1f%%", p.getStats().getAccuracy()))
//                 .append(System.lineSeparator());
//         }
//         return builder.toString();
//     }

// }
// package frontend;

// import backend.logic.AuthService;
// import backend.logic.GuardianService;
// import backend.model.Player;
// import java.awt.BorderLayout;
// import java.awt.Color;
// import java.awt.Dimension;
// import java.awt.GridLayout;
// import java.util.List;
// import javax.swing.BorderFactory;
// import javax.swing.JComboBox;
// import javax.swing.JOptionPane;
// import javax.swing.JPanel;
// import javax.swing.JPasswordField;
// import javax.swing.JScrollPane;
// import javax.swing.JTextArea;
// import javax.swing.JButton;
// import java.awt.CardLayout;
// import java.util.ArrayList;

// public class GuardianControlPanel extends BaseScreenPanel {
//     private final GuardianService guardianService;
//     private final AuthService authService;
//     private final ScreenManager screenManager;
//     private final JComboBox<String> playerSelect;
//     private final JTextArea statsArea;
//     private JTextArea leaderboardPreview; // Added missing field
//     private JPanel container;             // For CardLayout navigation
//     private CardLayout cardLayout;

//     public GuardianControlPanel(ScreenManager screenManager, GuardianService guardianService,
//             AuthService authService) {

//         super("Guardian Control Menu");
//         this.guardianService = guardianService;
//         this.authService = authService;
//         this.screenManager = screenManager;

//         JPanel shell = createMockFrame();
//         shell.setLayout(new BorderLayout(20, 20));

//         JPanel controls = new JPanel(new GridLayout(0, 1, 12, 12));
//         controls.setOpaque(false);

//         // Player selection dropdown
//         this.playerSelect = new JComboBox<>();
//         playerSelect.setFont(TEKTUR.deriveFont(16f));
//         playerSelect.setBackground(new Color(40, 40, 55));
//         playerSelect.setForeground(new Color(245, 235, 210));
//         playerSelect.setBorder(BorderFactory.createLineBorder(BTN_BORDER, 2));
//         playerSelect.setPreferredSize(new Dimension(0, 48));

//         // Create buttons without brackets
//         JButton showStatsButton = createMenuButton("Show Player Stats");
//         JButton restoreButton = createMenuButton("Restore Default Leaderboard");
//         JButton updateCodeButton = createMenuButton("Update Guardian Code");
//         JButton resetPasswordButton = createMenuButton("Reset Player Password");
//         JButton resetStatsButton = createMenuButton("Reset Player Stats");
//         JButton resetProgressButton = createMenuButton("Reset Player Progress");
//         JButton createAccountButton = createMenuButton("Create New Account");
//         JButton backButton = createMenuButton("Back to Main Menu");

//         // Add components to controls panel
//         controls.add(playerSelect);
//         controls.add(showStatsButton);
//         controls.add(restoreButton);
//         controls.add(updateCodeButton);
//         controls.add(resetPasswordButton);
//         controls.add(resetStatsButton);
//         controls.add(resetProgressButton);
//         controls.add(createAccountButton);
//         controls.add(backButton);

//         // Stats display area
//         this.statsArea = new JTextArea();
//         statsArea.setEditable(false);
//         statsArea.setForeground(new Color(245, 235, 210));
//         statsArea.setBackground(new Color(28, 28, 42));
//         statsArea.setFont(TEKTUR.deriveFont(13f));
//         statsArea.setBorder(BorderFactory.createCompoundBorder(
//             BorderFactory.createLineBorder(BTN_BORDER, 2),
//             BorderFactory.createEmptyBorder(12, 12, 12, 12)
//         ));

//         // Button actions
//         showStatsButton.addActionListener(event -> showSelectedPlayerStats());

//         restoreButton.addActionListener(event -> restoreDefaultLeaderboard());

//         updateCodeButton.addActionListener(event -> updateGuardianCode());

//         resetPasswordButton.addActionListener(event -> resetPlayerPassword());

//         resetStatsButton.addActionListener(event -> resetPlayerStats());

//         resetProgressButton.addActionListener(event -> resetPlayerProgress());

//         createAccountButton.addActionListener(event ->
//                 screenManager.showScreen(ScreenManager.REGISTER));

//         backButton.addActionListener(event -> {
//             this.authService.logout();
//             screenManager.showScreen(ScreenManager.MAIN_MENU);
//         });

//         JScrollPane statsScrollPane = new JScrollPane(statsArea);
//         statsScrollPane.setPreferredSize(new Dimension(0, 280));
//         statsScrollPane.setBorder(null);
        
//         shell.add(controls, BorderLayout.NORTH);
//         shell.add(statsScrollPane, BorderLayout.CENTER);
//         add(shell, BorderLayout.CENTER);
//     }

//     @Override
//     public void setVisible(boolean visible) {
//         super.setVisible(visible);
//         if (visible) {
//             refreshPlayerList();
//             if (statsArea != null) statsArea.setText(loadAllPlayerStatistics());
//             if (leaderboardPreview != null) leaderboardPreview.setText(loadAllPlayerStatistics());
//         }
//     }

//     /**
//      * Navigates to the Reset Stats card.
//      */
//     private void resetPlayerStats() {
//         if (cardLayout != null) {
//             cardLayout.show(container, "RESET_STATS");
//         }
//     }

//     /**
//      * Navigates to the Reset Progress card.
//      */
//     private void resetPlayerProgress() {
//         if (cardLayout != null) {
//             cardLayout.show(container, "RESET_PROGRESS");
//         }
//     }

//     /**
//      * Refreshes the dropdown list with current player names.
//      */
//     private void refreshPlayerList() {
//         if (playerSelect == null) return;
//         playerSelect.removeAllItems();
//         for (String username : loadUsernames(guardianService, authService)) {
//             playerSelect.addItem(username);
//         }
//     }

//     /**
//      * Filters usernames to show players (and the current guardian).
//      */
//     private String[] loadUsernames(GuardianService service, AuthService authService) {
//         List<Player> players = service.getAllPlayers();
//         Player currentGuardian = authService.getCurrentPlayer();
//         String currentGuardianName = currentGuardian != null ? currentGuardian.getUsername() : null;
        
//         return players.stream()
//             .filter(player -> !service.isGuardian(player)
//                 || (currentGuardianName != null
//                 && player.getUsername().equalsIgnoreCase(currentGuardianName)))
//             .map(Player::getUsername)
//             .toArray(String[]::new);
//     }

//     /**
//      * Generates a formatted string of all player metrics for the preview areas.
//      */
//     private String loadAllPlayerStatistics() {
//         StringBuilder builder = new StringBuilder();
//         builder.append("═══════════════════════════════════════════════════════════════════════\n");
//         builder.append("                          ALL PLAYER STATISTICS\n");
//         builder.append("═══════════════════════════════════════════════════════════════════════\n\n");
        
//         for (Player p : guardianService.getAllPlayers()) {
//             if (guardianService.isGuardian(p)) continue;
//             builder.append(String.format(
//                 "%-15s | Score: %-6d | Progress: %d/3 | Level: %d | WPM: %5.1f | Acc: %5.1f%%\n", 
//                 p.getUsername(),
//                 p.getStats().getHighScore(),
//                 p.getHighestLevelUnlocked() + 1,
//                 p.getStats().getHighestLevel(),
//                 p.getStats().getAverageWPM(),
//                 p.getStats().getAccuracy()
//             ));
//         }
        
//         builder.append("\n═══════════════════════════════════════════════════════════════════════");
//         return builder.toString();
//     }

//     private void showSelectedPlayerStats() {
//         cardLayout.show(container, "SHOW_STATS");
//     }

//     private void restoreDefaultLeaderboard() {
//         int response = JOptionPane.showConfirmDialog(
//             this,
//             "<html><div style='width:360px; text-align:center;'>" +
//             "Restore Default Leaderboard will <b>reset high scores of all players to zero</b>.<br><br>" +
//             "Do you want to continue?" +
//             "</div></html>",
//             "Confirm Leaderboard Reset",
//             JOptionPane.YES_NO_OPTION,
//             JOptionPane.WARNING_MESSAGE
//         );

//         if (response == JOptionPane.YES_OPTION) {
//             boolean success = guardianService.restoreDefaultLeaderboard();
//             if (success) {
//                 showInfo("Leaderboard Reset", "All leaderboards have been restored to default values.");
//                 if (leaderboardPreview != null) {
//                     leaderboardPreview.setText(loadAllPlayerStatistics()); // Refresh the preview
//                 }
//             } else {
//                 showError("Reset Failed", "Unable to restore default leaderboard.");
//             }
//         } else {
//             cardLayout.show(container, "MENU"); // Go back to menu if not confirmed
//         }
//     }

//     private void updateGuardianCode() {
//         Player currentGuardian = authService.getCurrentPlayer();
//         if (!guardianService.isGuardian(currentGuardian)) {
//             showError("Guardian Session Required", "Current guardian session not found.");
//             return;
//         }

//         JPasswordField codeField = new JPasswordField(16);
//         int result = JOptionPane.showConfirmDialog(
//             this,
//             codeField,
//             "Enter new guardian code",
//             JOptionPane.OK_CANCEL_OPTION,
//             JOptionPane.PLAIN_MESSAGE
//         );

//         if (result != JOptionPane.OK_OPTION) {
//             return;
//         }

//         try {
//             boolean updated = guardianService.updateGuardianPin(
//                 currentGuardian.getUsername(),
//                 new String(codeField.getPassword())
//             );
            
//             if (updated) {
//                 currentGuardian.setGuardianCode(new String(codeField.getPassword()).trim());
//                 showInfo("Guardian Code", "Guardian code updated successfully.");
//             } else {
//                 showError("Save Failed", "Unable to save guardian code.");
//             }
//         } catch (IllegalArgumentException ex) {
//             showError("Invalid Guardian Code", ex.getMessage());
//         }
//     }

//     private void resetPlayerPassword() {
//         String selectedUser = String.valueOf(playerSelect.getSelectedItem());
//         if (selectedUser == null || selectedUser.isBlank() || "null".equals(selectedUser)) {
//             showError("No Account Selected", "Please select an account to reset the password.");
//             return;
//         }

//         JPasswordField passwordField = new JPasswordField(16);
//         int result = JOptionPane.showConfirmDialog(
//             this,
//             passwordField,
//             "Enter new password for " + selectedUser,
//             JOptionPane.OK_CANCEL_OPTION,
//             JOptionPane.PLAIN_MESSAGE
//         );

//         if (result != JOptionPane.OK_OPTION) {
//             return;
//         }

//         try {
//             Player currentGuardian = authService.getCurrentPlayer();
//             boolean updated = currentGuardian != null && guardianService.resetPasswordAsGuardian(
//                 currentGuardian.getUsername(),
//                 selectedUser,
//                 new String(passwordField.getPassword())
//             );
            
//             if (updated) {
//                 showInfo("Password Reset", "Password reset for " + selectedUser + ".");
//             } else {
//                 showError("Reset Failed", "Unable to reset password for that account.");
//             }
//         } catch (IllegalArgumentException ex) {
//             showError("Invalid Password", ex.getMessage());
//         }
//     }

//     private void resetPlayerStats() {
//         // This method now just navigates to the reset stats page
//         cardLayout.show(container, "RESET_PLAYER_STATS");
//         // The actual reset logic is in performResetStatsAction
//     }

//     private void showInfo(String title, String message) {
//         JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
//     }

//     private void showError(String title, String message) {
//         JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
//     }
// }
package frontend;

import backend.logic.AuthService;
import backend.logic.GuardianService;
import backend.model.Player;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
/**
 * GuardianControlPanel provides the guardian or teacher management interface.
 * It allows authorized guardians to review player data and perform protected
 * administrative actions such as restoring the leaderboard, resetting players,
 * updating guardian codes, resetting passwords, and creating new accounts.
 */
public class GuardianControlPanel extends BaseScreenPanel {

    private final GuardianService guardianService;
    private final AuthService authService;
    private final JComboBox<String> playerSelect;

    public GuardianControlPanel(ScreenManager screenManager,
                                GuardianService guardianService,
                                AuthService authService) {

        super("Guardian Control Menu");
        this.guardianService = guardianService;
        this.authService = authService;

        JPanel shell = createMockFrame();
        shell.setLayout(new BorderLayout(20, 20));

        JPanel controls = new JPanel(new GridLayout(0, 1, 12, 12));
        controls.setOpaque(false);

        playerSelect = new JComboBox<>();
        playerSelect.setFont(TEKTUR.deriveFont(16f));
        playerSelect.setBackground(new Color(40, 40, 55));
        playerSelect.setForeground(new Color(245, 235, 210));
        playerSelect.setBorder(BorderFactory.createLineBorder(BTN_BORDER, 2));
        playerSelect.setPreferredSize(new Dimension(0, 48));

        JButton showStatsBtn = createMenuButton("Show Player Stats");
        JButton leaderboardBtn = createMenuButton("View Leaderboard");
        JButton resetStatsBtn = createMenuButton("Reset Player Stats");
        JButton restoreLeaderboardBtn = createMenuButton("Restore Leaderboard");
        JButton updateCodeButton = createMenuButton("Update Guardian Code");
        JButton resetPasswordButton = createMenuButton("Reset Player Password");
        JButton createAccountButton = createMenuButton("Create New Account");
        JButton backBtn = createMenuButton("Back to Main Menu");

        controls.add(playerSelect);
        controls.add(showStatsBtn);
        controls.add(leaderboardBtn);
        controls.add(resetStatsBtn);
        controls.add(restoreLeaderboardBtn);
        controls.add(updateCodeButton);
        controls.add(resetPasswordButton);
        controls.add(createAccountButton);
        controls.add(backBtn);

        showStatsBtn.addActionListener(e -> showPlayerStatsPopup());
        leaderboardBtn.addActionListener(e -> showLeaderboardPopup());
        resetStatsBtn.addActionListener(e -> resetPlayerStats());
        restoreLeaderboardBtn.addActionListener(e -> restoreLeaderboard());
        updateCodeButton.addActionListener(e -> updateGuardianCode());
        resetPasswordButton.addActionListener(e -> resetPlayerPassword());
        createAccountButton.addActionListener(e -> screenManager.showScreen(ScreenManager.REGISTER));
        backBtn.addActionListener(e -> {
            authService.logout();
            screenManager.showScreen(ScreenManager.MAIN_MENU);
        });

        shell.add(controls, BorderLayout.CENTER);
        add(shell, BorderLayout.CENTER);
    }

    private void showPlayerStatsPopup() {
        String username = (String) playerSelect.getSelectedItem();
        if (username == null) {
            return;
        }

        Player player = null;
        for (Player candidate : guardianService.getAllPlayers()) {
            if (candidate.getUsername().equals(username)) {
                player = candidate;
                break;
            }
        }
        if (player == null) {
            return;
        }

        JPanel panel = buildStatsPanel(player);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(450, 500));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);

        showStyledContentDialog(
                "PLAYER STATS",
                "Viewing statistics for " + username,
                scroll,
                "[ Close ]");
    }

    private void showLeaderboardPopup() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(TEKTUR.deriveFont(18f));
        area.setForeground(new Color(245, 235, 210));
        area.setBackground(new Color(28, 28, 42));
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        area.setText(buildLeaderboardText());

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 500));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(28, 28, 42));

        showStyledContentDialog(
                "LEADERBOARD",
                "Current player rankings",
                scroll,
                "[ Close ]");
    }

    private void resetPlayerStats() {
        String username = (String) playerSelect.getSelectedItem();
        if (username == null) {
            return;
        }

        Boolean confirmed = showStyledDecisionDialog(
                "RESET PLAYER STATS",
                "This action cannot be undone.",
                "<html><div style='text-align:center; width:300px;'>This will reset all stats for <b>"
                        + username + "</b> to zero.<br/><br/>Do you want to continue?</div></html>",
                "[ Yes, Reset ]",
                "[ Cancel ]");

        if (Boolean.TRUE.equals(confirmed)) {
            guardianService.resetPlayerStats(username);
            showStyledMessageDialog(
                    "PLAYER STATS RESET",
                    "Success",
                    "All statistics for " + username + " have been reset.",
                    "[ OK ]");
        }
    }

    private void restoreLeaderboard() {
        Boolean confirmed = showStyledDecisionDialog(
                "RESTORE LEADERBOARD",
                "This action affects all players.",
                "<html><div style='text-align:center; width:320px;'>This will reset highscores for <b>all players</b> back to their default state.<br/><br/>Do you want to continue?</div></html>",
                "[ Yes, Restore ]",
                "[ Cancel ]");

        if (Boolean.TRUE.equals(confirmed)) {
            guardianService.restoreDefaultLeaderboard();
            showStyledMessageDialog(
                    "LEADERBOARD RESTORED",
                    "Success",
                    "The default leaderboard has been restored.",
                    "[ OK ]");
        }
    }

    private JPanel buildStatsPanel(Player player) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        var stats = player.getStats();

        addRow(panel, gbc, "High Score:", String.valueOf(stats.getHighScore()));
        addRow(panel, gbc, "Average WPM:", String.format("%.1f", stats.getAverageWPM()));
        addRow(panel, gbc, "Peak WPM:", String.format("%.1f", stats.getPeakWPM()));
        addRow(panel, gbc, "Accuracy:", String.format("%.1f%%", stats.getAccuracy()));
        addRow(panel, gbc, "Words Typed:", String.valueOf(stats.getWordsTyped()));
        addRow(panel, gbc, "Errors:", String.valueOf(stats.getErrorCount()));
        addRow(panel, gbc, "Highest Level:", String.valueOf(stats.getHighestLevel()));

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        JLabel keyLabel = new JLabel(label);
        keyLabel.setFont(TEKTUR.deriveFont(Font.BOLD, 18f));
        keyLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(TEKTUR.deriveFont(18f));
        valueLabel.setForeground(BTN_BORDER);

        gbc.gridx = 0;
        panel.add(keyLabel, gbc);

        gbc.gridx = 1;
        panel.add(valueLabel, gbc);

        gbc.gridy++;
    }

    private void resetPlayerPassword() {
        String selectedUser = String.valueOf(playerSelect.getSelectedItem());

        if (selectedUser == null || selectedUser.isBlank() || "null".equals(selectedUser)) {
            showStyledMessageDialog(
                    "NO ACCOUNT SELECTED",
                    "Reset Password",
                    "Please select an account to reset the password.",
                    "[ OK ]");
            return;
        }

        String newPassword = showStyledPasswordPrompt(
                "RESET PLAYER PASSWORD",
                "Enter a new password for " + selectedUser + ".",
                "New Password:",
                "[ Save Password ]",
                "[ Cancel ]");

        if (newPassword == null) {
            return;
        }

        try {
            Player currentGuardian = authService.getCurrentPlayer();
            boolean updated = currentGuardian != null
                    && guardianService.resetPasswordAsGuardian(
                            currentGuardian.getUsername(),
                            selectedUser,
                            newPassword);

            if (updated) {
                showStyledMessageDialog(
                        "PASSWORD RESET",
                        "Success",
                        "Password reset for " + selectedUser + ".",
                        "[ OK ]");
            } else {
                showStyledMessageDialog(
                        "RESET FAILED",
                        "Unable to continue",
                        "Unable to reset password for that account.",
                        "[ OK ]");
            }
        } catch (IllegalArgumentException ex) {
            showStyledMessageDialog(
                    "INVALID PASSWORD",
                    "Please try again",
                    ex.getMessage(),
                    "[ OK ]");
        }
    }

    private String buildLeaderboardText() {
        StringBuilder builder = new StringBuilder();
        List<Player> players = guardianService.getAllPlayers();

        for (Player player : players) {
            if (guardianService.isGuardian(player)) {
                continue;
            }
            builder.append(player.getUsername())
                    .append(" - ")
                    .append(player.getStats().getHighScore())
                    .append("\n");
        }

        return builder.toString();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            refreshPlayers();
        }
    }

    private void refreshPlayers() {
        // Rebuild the dropdown from the latest stored players each time the panel opens.
        playerSelect.removeAllItems();
        for (Player player : guardianService.getAllPlayers()) {
            if (!guardianService.isGuardian(player)) {
                playerSelect.addItem(player.getUsername());
            }
        }
    }

    private void updateGuardianCode() {
        Player currentGuardian = authService.getCurrentPlayer();
        if (!guardianService.isGuardian(currentGuardian)) {
            showStyledMessageDialog(
                    "GUARDIAN SESSION REQUIRED",
                    "Unable to continue",
                    "Current guardian session not found.",
                    "[ OK ]");
            return;
        }

        String newCode = showStyledPasswordPrompt(
                "UPDATE GUARDIAN CODE",
                "Enter a new guardian code.",
                "Guardian Code:",
                "[ Save Code ]",
                "[ Cancel ]");

        if (newCode == null) {
            return;
        }

        try {
            boolean updated = guardianService.updateGuardianPin(
                    currentGuardian.getUsername(),
                    newCode);

            if (updated) {
                currentGuardian.setGuardianCode(newCode.trim());
                showStyledMessageDialog(
                        "GUARDIAN CODE UPDATED",
                        "Success",
                        "Guardian code updated successfully.",
                        "[ OK ]");
            } else {
                showStyledMessageDialog(
                        "SAVE FAILED",
                        "Unable to continue",
                        "Unable to save guardian code.",
                        "[ OK ]");
            }
        } catch (IllegalArgumentException ex) {
            showStyledMessageDialog(
                    "INVALID GUARDIAN CODE",
                    "Please try again",
                    ex.getMessage(),
                    "[ OK ]");
        }
    }

    private void showStyledMessageDialog(
            String titleText,
            String subtitleText,
            String bodyText,
            String buttonText) {
        JDialog dialog = createStyledDialog(titleText);

        JButton actionButton = createMenuButton(buttonText);
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.addActionListener(event -> dialog.dispose());

        JPanel content = buildDialogContent(titleText, subtitleText, bodyText, actionButton, null);

        dialog.setContentPane(content);
        dialog.setMinimumSize(new Dimension(560, 340));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showStyledContentDialog(
            String titleText,
            String subtitleText,
            JComponent centerContent,
            String buttonText) {
        JDialog dialog = createStyledDialog(titleText);

        JButton actionButton = createMenuButton(buttonText);
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.addActionListener(event -> dialog.dispose());

        JPanel content = buildDialogContent(titleText, subtitleText, null, actionButton, null);
        centerContent.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(centerContent, BorderLayout.CENTER);

        dialog.setContentPane(content);
        dialog.setMinimumSize(new Dimension(620, 620));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String showStyledPasswordPrompt(
            String titleText,
            String subtitleText,
            String fieldLabelText,
            String confirmText,
            String cancelText) {
        final String[] result = {null};
        JDialog dialog = createStyledDialog(titleText);

        JLabel fieldLabel = createBodyLabel(fieldLabelText, 16, SwingConstants.CENTER);
        fieldLabel.setForeground(new Color(245, 235, 210));
        fieldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passwordField = createStyledPasswordField(16);
        passwordField.setMaximumSize(new Dimension(320, 42));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton confirmButton = createMenuButton(confirmText);
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmButton.addActionListener(event -> {
            String value = new String(passwordField.getPassword()).trim();
            if (!value.isEmpty()) {
                result[0] = value;
                dialog.dispose();
            }
        });

        JButton cancelButton = createMenuButton(cancelText);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(event -> dialog.dispose());

        JPanel content = buildDialogContent(titleText, subtitleText, null, confirmButton, cancelButton);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalStrut(8));
        center.add(fieldLabel);
        center.add(Box.createVerticalStrut(8));
        center.add(passwordField);
        center.add(Box.createVerticalStrut(20));
        content.add(center, BorderLayout.CENTER);

        dialog.setContentPane(content);
        dialog.setMinimumSize(new Dimension(580, 380));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(passwordField::requestFocusInWindow);
        dialog.setVisible(true);
        return result[0];
    }

    private Boolean showStyledDecisionDialog(
            String titleText,
            String subtitleText,
            String bodyHtml,
            String primaryText,
            String secondaryText) {
        final Boolean[] result = {null};
        JDialog dialog = createStyledDialog(titleText);

        JButton primaryButton = createMenuButton(primaryText);
        primaryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        primaryButton.addActionListener(event -> {
            result[0] = Boolean.TRUE;
            dialog.dispose();
        });

        JButton secondaryButton = createMenuButton(secondaryText);
        secondaryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        secondaryButton.addActionListener(event -> {
            result[0] = Boolean.FALSE;
            dialog.dispose();
        });

        JPanel content = buildDialogContent(titleText, subtitleText, bodyHtml, primaryButton, secondaryButton);

        dialog.setContentPane(content);
        dialog.setMinimumSize(new Dimension(580, 380));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    private JDialog createStyledDialog(String titleText) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                titleText,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        return dialog;
    }

    private JPanel buildDialogContent(
            String titleText,
            String subtitleText,
            String bodyHtml,
            JButton primaryButton,
            JButton secondaryButton) {
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(28, 28, 42));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 24, 24);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(30, 42, 28, 42));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setForeground(ACCENT);
        title.setFont(BITCOUNT.deriveFont(34f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText, SwingConstants.CENTER);
        subtitle.setForeground(new Color(220, 210, 190));
        subtitle.setFont(TEKTUR.deriveFont(16f));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        top.add(title);
        top.add(subtitle);
        outer.add(top, BorderLayout.NORTH);

        if (bodyHtml != null && !bodyHtml.isBlank()) {
            JLabel body = new JLabel(bodyHtml, SwingConstants.CENTER);
            body.setForeground(new Color(245, 235, 210));
            body.setFont(TEKTUR.deriveFont(14f));
            body.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
            outer.add(body, BorderLayout.CENTER);
        }

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.add(Box.createVerticalStrut(24));
        actions.add(primaryButton);
        if (secondaryButton != null) {
            actions.add(Box.createVerticalStrut(14));
            actions.add(secondaryButton);
        }
        outer.add(actions, BorderLayout.SOUTH);
        return outer;
    }
}
