// package frontend;

// import backend.logic.AuthService;
// import backend.model.Difficulty;
// import backend.model.Player;
// import backend.model.PlayerStats;
// import backend.model.Score;
// import backend.storage.FileDataStore;

// import javax.swing.*;
// import java.awt.*;
// import java.nio.file.Path;
// import java.util.*;
// import java.util.List;

// public class HighScorePanel extends BaseScreenPanel {

//     private final AuthService authService;

//     public HighScorePanel(ScreenManager screenManager, AuthService authService) {
//         super("Player Statistics");
//         this.authService = authService;

//         JPanel shell = createMockFrame();
//         shell.setLayout(new BorderLayout(20, 20));

//         // Tab panel — Player Stats + Leaderboards
//         JTabbedPane tabs = new JTabbedPane();
//         tabs.setFont(new Font("SansSerif", Font.BOLD, 18));
//         tabs.setBackground(BACKGROUND);
//         tabs.setForeground(FOREGROUND);
//         tabs.setOpaque(true);

//         // First tab: Current player's detailed statistics
//         tabs.addTab("My Stats", buildPlayerStatsPanel());

//         // Leaderboard tabs
//         tabs.addTab("Easy",   buildLeaderboard(Difficulty.EASY));
//         tabs.addTab("Medium", buildLeaderboard(Difficulty.MEDIUM));
//         tabs.addTab("Hard",   buildLeaderboard(Difficulty.HARD));
//         tabs.addTab("Endless", buildLeaderboard(Difficulty.ENDLESS));

//         JButton backButton = createMenuButton("Back");
//         backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.PLAYER_MENU));

//         shell.add(backButton, BorderLayout.NORTH);
//         shell.add(tabs,       BorderLayout.CENTER);
//         add(shell, BorderLayout.CENTER);
//     }

//     private JPanel buildPlayerStatsPanel() {
//         Player currentPlayer = authService.getCurrentPlayer();

//         JPanel panel = new JPanel(new GridBagLayout());
//         panel.setBackground(BACKGROUND);
//         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.gridx = 0;
//         gbc.anchor = GridBagConstraints.WEST;
//         gbc.fill = GridBagConstraints.HORIZONTAL;
//         gbc.weightx = 1.0;
//         gbc.weighty = 0;
//         gbc.insets = new Insets(8, 12, 8, 12);

//         if (currentPlayer == null) {
//             JLabel guestLabel = new JLabel("You are playing as a Guest.");
//             guestLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
//             guestLabel.setForeground(new Color(210, 205, 190));
//             guestLabel.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 0;
//             panel.add(guestLabel, gbc);

//             JLabel guestHint = new JLabel("Login to track your statistics!");
//             guestHint.setFont(new Font("SansSerif", Font.PLAIN, 16));
//             guestHint.setForeground(new Color(180, 175, 160));
//             guestHint.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 1;
//             panel.add(guestHint, gbc);
//         } else {
//             PlayerStats stats = currentPlayer.getStats();

//             // Player name header
//             JLabel nameHeader = new JLabel("Player: " + currentPlayer.getUsername());
//             nameHeader.setFont(new Font("SansSerif", Font.BOLD, 28));
//             nameHeader.setForeground(ACCENT);
//             nameHeader.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 0;
//             gbc.gridwidth = 2;
//             panel.add(nameHeader, gbc);

//             gbc.gridwidth = 1;
//             gbc.gridy++;

//             // Add separator
//             JSeparator separator1 = new JSeparator();
//             separator1.setForeground(new Color(92, 92, 118));
//             separator1.setBackground(new Color(92, 92, 118));
//             gbc.gridx = 0;
//             gbc.gridwidth = 2;
//             gbc.insets = new Insets(12, 12, 12, 12);
//             panel.add(separator1, gbc);
//             gbc.gridwidth = 1;
//             gbc.gridy++;

//             // Statistics section
//             gbc.insets = new Insets(6, 12, 6, 12);

//             // Format time played
//             int totalSeconds = stats.getTotalTimePlayed();
//             int hours = totalSeconds / 3600;
//             int minutes = (totalSeconds % 3600) / 60;
//             int seconds = totalSeconds % 60;
//             String timePlayedStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

//             // Add all stats
//             addStatRow(panel, gbc, "Overall High Score:", String.valueOf(stats.getHighScore()));
//             addStatRow(panel, gbc, "Average WPM:", String.format("%.1f", stats.getAverageWPM()));
//             addStatRow(panel, gbc, "Peak WPM:", String.format("%.1f", stats.getPeakWPM()));
//             addStatRow(panel, gbc, "Accuracy:", String.format("%.1f%%", stats.getAccuracy()));
//             addStatRow(panel, gbc, "Total Words Typed:", String.valueOf(stats.getWordsTyped()));
//             addStatRow(panel, gbc, "Total Errors:", String.valueOf(stats.getErrorCount()));
//             addStatRow(panel, gbc, "Total Time Played:", timePlayedStr);
//             addStatRow(panel, gbc, "Highest Level Reached:", String.valueOf(stats.getHighestLevel()));

//             // Add separator before difficulty scores
//             gbc.gridy++;
//             JSeparator separator2 = new JSeparator();
//             separator2.setForeground(new Color(92, 92, 118));
//             separator2.setBackground(new Color(92, 92, 118));
//             gbc.gridx = 0;
//             gbc.gridwidth = 2;
//             gbc.insets = new Insets(12, 12, 12, 12);
//             panel.add(separator2, gbc);
//             gbc.gridwidth = 1;
//             gbc.gridy++;

//             // Difficulty-specific scores
//             JLabel scoresHeader = new JLabel("Best Scores by Difficulty");
//             scoresHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
//             scoresHeader.setForeground(ACCENT);
//             gbc.gridx = 0;
//             gbc.gridwidth = 2;
//             gbc.insets = new Insets(6, 12, 12, 12);
//             panel.add(scoresHeader, gbc);
//             gbc.gridwidth = 1;
//             gbc.gridy++;

//             gbc.insets = new Insets(6, 12, 6, 12);
//             addStatRow(panel, gbc, "Easy:", getScoreForDifficulty(currentPlayer, Difficulty.EASY));
//             addStatRow(panel, gbc, "Medium:", getScoreForDifficulty(currentPlayer, Difficulty.MEDIUM));
//             addStatRow(panel, gbc, "Hard:", getScoreForDifficulty(currentPlayer, Difficulty.HARD));
//             addStatRow(panel, gbc, "Endless:", getScoreForDifficulty(currentPlayer, Difficulty.ENDLESS));
//         }

//         // Add vertical glue at the end to push content to top
//         gbc.gridy++;
//         gbc.weighty = 1.0;
//         gbc.fill = GridBagConstraints.BOTH;
//         panel.add(Box.createVerticalGlue(), gbc);

//         JScrollPane scroll = new JScrollPane(panel);
//         scroll.setBorder(null);
//         scroll.getViewport().setBackground(BACKGROUND);
//         scroll.setBackground(BACKGROUND);

//         JPanel wrapper = new JPanel(new BorderLayout());
//         wrapper.setBackground(BACKGROUND);
//         wrapper.add(scroll, BorderLayout.CENTER);
//         return wrapper;
//     }

//     private void addStatRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
//         // Label
//         JLabel labelComp = new JLabel(label);
//         labelComp.setFont(new Font("SansSerif", Font.BOLD, 18));
//         labelComp.setForeground(new Color(245, 235, 210));
//         gbc.gridx = 0;
//         panel.add(labelComp, gbc);

//         // Value
//         JLabel valueComp = new JLabel(value);
//         valueComp.setFont(new Font("SansSerif", Font.PLAIN, 18));
//         valueComp.setForeground(FOREGROUND);
//         valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
//         gbc.gridx = 1;
//         panel.add(valueComp, gbc);

//         gbc.gridy++;
//     }

//     private String getScoreForDifficulty(Player player, Difficulty difficulty) {
//         for (Score score : player.getScores()) {
//             if (score.getDifficulty() == difficulty && score.getValue() > 0) {
//                 return String.valueOf(score.getValue());
//             }
//         }
//         return "Not played";
//     }

//     private JPanel buildLeaderboard(Difficulty difficulty) {
//         // Load all players and collect their score for this difficulty
//         List<Player> allPlayers = loadAllPlayers();
//         List<int[]> entries = new ArrayList<>(); // [score, playerIndex]

//         for (int i = 0; i < allPlayers.size(); i++) {
//             Player p = allPlayers.get(i);
//             for (Score s : p.getScores()) {
//                 if (s.getDifficulty() == difficulty && s.getValue() > 0) {
//                     entries.add(new int[]{s.getValue(), i});
//                 }
//             }
//         }

//         // Sort descending by score
//         entries.sort((a, b) -> Integer.compare(b[0], a[0]));

//         // Build the display panel
//         JPanel panel = new JPanel(new GridBagLayout());
//         panel.setBackground(BACKGROUND);
//         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.gridx = 0;
//         gbc.anchor = GridBagConstraints.WEST;
//         gbc.fill = GridBagConstraints.HORIZONTAL;
//         gbc.weightx = 1.0;
//         gbc.weighty = 0;
//         gbc.insets = new Insets(0, 0, 0, 0);

//         Player currentUser = authService.getCurrentPlayer();

//         if (entries.isEmpty()) {
//             JLabel none = new JLabel("No scores yet for " + difficulty.name() + ".");
//             none.setFont(new Font("SansSerif", Font.ITALIC, 18));
//             none.setForeground(new Color(210, 205, 190));
//             none.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 0;
//             panel.add(none, gbc);
//         } else {
//             for (int rank = 0; rank < entries.size() && rank < 10; rank++) {
//                 int[] entry   = entries.get(rank);
//                 Player player = allPlayers.get(entry[1]);
//                 int score     = entry[0];

//                 boolean isCurrentUser = currentUser != null
//                         && player.getUsername().equals(currentUser.getUsername());

//                 String text = (rank + 1) + ".   " + player.getUsername()
//                         + "   —   " + score + " pts";

//                 JLabel row = new JLabel(text);
//                 row.setFont(new Font("SansSerif", Font.BOLD, 20));
//                 row.setForeground(isCurrentUser ? ACCENT : FOREGROUND);
//                 row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

//                 gbc.gridy = rank * 2;
//                 panel.add(row, gbc);

//                 JSeparator separator = new JSeparator();
//                 separator.setForeground(new Color(92, 92, 118));
//                 separator.setBackground(new Color(92, 92, 118));
//                 gbc.gridy = rank * 2 + 1;
//                 panel.add(separator, gbc);
//             }
//         }

//         // Add vertical glue at the end to push content to top
//         gbc.gridy++;
//         gbc.weighty = 1.0;
//         gbc.fill = GridBagConstraints.BOTH;
//         panel.add(Box.createVerticalGlue(), gbc);

//         JScrollPane scroll = new JScrollPane(panel);
//         scroll.setBorder(null);
//         scroll.getViewport().setBackground(BACKGROUND);
//         scroll.setBackground(BACKGROUND);

//         JPanel wrapper = new JPanel(new BorderLayout());
//         wrapper.setBackground(BACKGROUND);
//         wrapper.add(scroll, BorderLayout.CENTER);
//         return wrapper;
//     }

//     private List<Player> loadAllPlayers() {
//         try {
//             FileDataStore store = new FileDataStore(Path.of("data").toString());
//             return store.loadPlayers();
//         } catch (Exception e) {
//             System.err.println("HighScorePanel: could not load players — " + e.getMessage());
//             return new ArrayList<>();
//         }
//     }
// }
//
// package frontend;

// import backend.logic.AuthService;
// import backend.model.Difficulty;
// import backend.model.Player;
// import backend.model.PlayerStats;
// import backend.model.Score;
// import backend.storage.FileDataStore;

// import javax.swing.*;
// import java.awt.*;
// import java.nio.file.Path;
// import java.util.*;
// import java.util.List;

// public class HighScorePanel extends BaseScreenPanel {

//     private final AuthService authService;
//     private JPanel contentPanel;
//     private CardLayout cardLayout;
//     private JButton currentStatsButton;
//     private JButton leaderboardButton;

//     public HighScorePanel(ScreenManager screenManager, AuthService authService) {
//         super("Player Statistics");
//         this.authService = authService;

//         JPanel shell = createMockFrame();
//         shell.setLayout(new BorderLayout(20, 20));

//         // Top toggle buttons
//         JPanel togglePanel = new JPanel(new GridLayout(1, 2, 20, 0));
//         togglePanel.setOpaque(false);
//         togglePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

//         currentStatsButton = createToggleButton("Current Player Stats");
//         leaderboardButton = createToggleButton("Leaderboard");

//         currentStatsButton.addActionListener(e -> showCard("stats"));
//         leaderboardButton.addActionListener(e -> showCard("leaderboard"));

//         togglePanel.add(currentStatsButton);
//         togglePanel.add(leaderboardButton);

//         // Content area with CardLayout
//         cardLayout = new CardLayout();
//         contentPanel = new JPanel(cardLayout);
//         contentPanel.setOpaque(false);

//         contentPanel.add(buildPlayerStatsCard(), "stats");
//         contentPanel.add(buildLeaderboardCard(), "leaderboard");

//         // Bottom back button
//         JButton backButton = createMenuButton("Back");
//         backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.PLAYER_MENU));

//         shell.add(togglePanel, BorderLayout.NORTH);
//         shell.add(contentPanel, BorderLayout.CENTER);
//         shell.add(backButton, BorderLayout.SOUTH);
        
//         add(shell, BorderLayout.CENTER);

//         // Show stats by default
//         showCard("stats");
//     }

//     private JButton createToggleButton(String text) {
//         JButton button = new JButton(text);
//         button.setFocusPainted(false);
//         button.setFont(new Font("SansSerif", Font.BOLD, 16));
//         button.setBackground(new Color(60, 60, 80));
//         button.setForeground(new Color(245, 235, 210));
//         button.setBorder(BorderFactory.createCompoundBorder(
//             BorderFactory.createLineBorder(BTN_BORDER, 2),
//             BorderFactory.createEmptyBorder(12, 20, 12, 20)
//         ));
//         button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
//         button.addMouseListener(new java.awt.event.MouseAdapter() {
//             @Override
//             public void mouseEntered(java.awt.event.MouseEvent e) {
//                 button.setForeground(BTN_HOVER);
//             }
//             @Override
//             public void mouseExited(java.awt.event.MouseEvent e) {
//                 button.setForeground(new Color(245, 235, 210));
//             }
//         });
        
//         return button;
//     }

//     private void showCard(String cardName) {
//         cardLayout.show(contentPanel, cardName);
        
//         // Update button styles to show which is active
//         if (cardName.equals("stats")) {
//             currentStatsButton.setBackground(new Color(80, 80, 100));
//             leaderboardButton.setBackground(new Color(60, 60, 80));
//         } else {
//             currentStatsButton.setBackground(new Color(60, 60, 80));
//             leaderboardButton.setBackground(new Color(80, 80, 100));
//         }
//     }

//     private JPanel buildPlayerStatsCard() {
//         Player currentPlayer = authService.getCurrentPlayer();
        
//         JPanel panel = new JPanel(new GridBagLayout());
//         panel.setBackground(BACKGROUND);
//         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.gridx = 0;
//         gbc.anchor = GridBagConstraints.WEST;
//         gbc.fill = GridBagConstraints.HORIZONTAL;
//         gbc.weightx = 1.0;
//         gbc.weighty = 0;
//         gbc.insets = new Insets(8, 12, 8, 12);

//         if (currentPlayer == null) {
//             JLabel guestLabel = new JLabel("You are playing as a Guest.");
//             guestLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
//             guestLabel.setForeground(new Color(210, 205, 190));
//             guestLabel.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 0;
//             panel.add(guestLabel, gbc);
            
//             JLabel guestHint = new JLabel("Login to track your statistics!");
//             guestHint.setFont(new Font("SansSerif", Font.PLAIN, 16));
//             guestHint.setForeground(new Color(180, 175, 160));
//             guestHint.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 1;
//             panel.add(guestHint, gbc);
//         } else {
//             PlayerStats stats = currentPlayer.getStats();
            
//             // Player name header
//             JLabel nameHeader = new JLabel("Player: " + currentPlayer.getUsername());
//             nameHeader.setFont(new Font("SansSerif", Font.BOLD, 28));
//             nameHeader.setForeground(ACCENT);
//             nameHeader.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 0;
//             gbc.gridwidth = 2;
//             panel.add(nameHeader, gbc);
            
//             gbc.gridwidth = 1;
//             gbc.gridy++;
            
//             // Add separator
//             JSeparator separator1 = new JSeparator();
//             separator1.setForeground(new Color(92, 92, 118));
//             separator1.setBackground(new Color(92, 92, 118));
//             gbc.gridx = 0;
//             gbc.gridwidth = 2;
//             gbc.insets = new Insets(12, 12, 12, 12);
//             panel.add(separator1, gbc);
//             gbc.gridwidth = 1;
//             gbc.gridy++;
            
//             // Statistics section
//             gbc.insets = new Insets(6, 12, 6, 12);
            
//             // Format time played
//             int totalSeconds = stats.getTotalTimePlayed();
//             int hours = totalSeconds / 3600;
//             int minutes = (totalSeconds % 3600) / 60;
//             int seconds = totalSeconds % 60;
//             String timePlayedStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            
//             // Add all stats
//             addStatRow(panel, gbc, "Overall High Score:", String.valueOf(stats.getHighScore()));
//             addStatRow(panel, gbc, "Average WPM:", String.format("%.1f", stats.getAverageWPM()));
//             addStatRow(panel, gbc, "Peak WPM:", String.format("%.1f", stats.getPeakWPM()));
//             addStatRow(panel, gbc, "Accuracy:", String.format("%.1f%%", stats.getAccuracy()));
//             addStatRow(panel, gbc, "Total Words Typed:", String.valueOf(stats.getWordsTyped()));
//             addStatRow(panel, gbc, "Total Errors:", String.valueOf(stats.getErrorCount()));
//             addStatRow(panel, gbc, "Total Time Played:", timePlayedStr);
//             addStatRow(panel, gbc, "Highest Level Reached:", String.valueOf(stats.getHighestLevel()));
            
//             // Add separator before difficulty scores
//             gbc.gridy++;
//             JSeparator separator2 = new JSeparator();
//             separator2.setForeground(new Color(92, 92, 118));
//             separator2.setBackground(new Color(92, 92, 118));
//             gbc.gridx = 0;
//             gbc.gridwidth = 2;
//             gbc.insets = new Insets(12, 12, 12, 12);
//             panel.add(separator2, gbc);
//             gbc.gridwidth = 1;
//             gbc.gridy++;
            
//             // Difficulty-specific scores
//             JLabel scoresHeader = new JLabel("Best Scores by Difficulty");
//             scoresHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
//             scoresHeader.setForeground(ACCENT);
//             gbc.gridx = 0;
//             gbc.gridwidth = 2;
//             gbc.insets = new Insets(6, 12, 12, 12);
//             panel.add(scoresHeader, gbc);
//             gbc.gridwidth = 1;
//             gbc.gridy++;
            
//             gbc.insets = new Insets(6, 12, 6, 12);
//             addStatRow(panel, gbc, "Easy:", getScoreForDifficulty(currentPlayer, Difficulty.EASY));
//             addStatRow(panel, gbc, "Medium:", getScoreForDifficulty(currentPlayer, Difficulty.MEDIUM));
//             addStatRow(panel, gbc, "Hard:", getScoreForDifficulty(currentPlayer, Difficulty.HARD));
//             addStatRow(panel, gbc, "Endless:", getScoreForDifficulty(currentPlayer, Difficulty.ENDLESS));
//         }

//         // Add vertical glue at the end to push content to top
//         gbc.gridy++;
//         gbc.weighty = 1.0;
//         gbc.fill = GridBagConstraints.BOTH;
//         panel.add(Box.createVerticalGlue(), gbc);

//         JScrollPane scroll = new JScrollPane(panel);
//         scroll.setBorder(null);
//         scroll.getViewport().setBackground(BACKGROUND);
//         scroll.setBackground(BACKGROUND);

//         JPanel wrapper = new JPanel(new BorderLayout());
//         wrapper.setBackground(BACKGROUND);
//         wrapper.add(scroll, BorderLayout.CENTER);
//         return wrapper;
//     }

//     private JPanel buildLeaderboardCard() {
//         JTabbedPane tabs = new JTabbedPane();
//         tabs.setFont(new Font("SansSerif", Font.BOLD, 16));
//         tabs.setBackground(BACKGROUND);
//         tabs.setForeground(FOREGROUND);
//         tabs.setOpaque(true);
        
//         tabs.addTab("Easy",   buildLeaderboard(Difficulty.EASY));
//         tabs.addTab("Medium", buildLeaderboard(Difficulty.MEDIUM));
//         tabs.addTab("Hard",   buildLeaderboard(Difficulty.HARD));
//         tabs.addTab("Endless", buildLeaderboard(Difficulty.ENDLESS));

//         JPanel wrapper = new JPanel(new BorderLayout());
//         wrapper.setBackground(BACKGROUND);
//         wrapper.add(tabs, BorderLayout.CENTER);
//         return wrapper;
//     }
    
//     private void addStatRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
//         // Label
//         JLabel labelComp = new JLabel(label);
//         labelComp.setFont(new Font("SansSerif", Font.BOLD, 18));
//         labelComp.setForeground(new Color(245, 235, 210));
//         gbc.gridx = 0;
//         panel.add(labelComp, gbc);
        
//         // Value
//         JLabel valueComp = new JLabel(value);
//         valueComp.setFont(new Font("SansSerif", Font.PLAIN, 18));
//         valueComp.setForeground(FOREGROUND);
//         valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
//         gbc.gridx = 1;
//         panel.add(valueComp, gbc);
        
//         gbc.gridy++;
//     }
    
//     private String getScoreForDifficulty(Player player, Difficulty difficulty) {
//         for (Score score : player.getScores()) {
//             if (score.getDifficulty() == difficulty && score.getValue() > 0) {
//                 return String.valueOf(score.getValue());
//             }
//         }
//         return "Not played";
//     }

//     private JPanel buildLeaderboard(Difficulty difficulty) {
//         List<Player> allPlayers = loadAllPlayers();
//         List<int[]> entries = new ArrayList<>();

//         for (int i = 0; i < allPlayers.size(); i++) {
//             Player p = allPlayers.get(i);
//             for (Score s : p.getScores()) {
//                 if (s.getDifficulty() == difficulty && s.getValue() > 0) {
//                     entries.add(new int[]{s.getValue(), i});
//                 }
//             }
//         }

//         entries.sort((a, b) -> Integer.compare(b[0], a[0]));

//         JPanel panel = new JPanel(new GridBagLayout());
//         panel.setBackground(BACKGROUND);
//         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.gridx = 0;
//         gbc.anchor = GridBagConstraints.WEST;
//         gbc.fill = GridBagConstraints.HORIZONTAL;
//         gbc.weightx = 1.0;
//         gbc.weighty = 0;
//         gbc.insets = new Insets(0, 0, 0, 0);

//         Player currentUser = authService.getCurrentPlayer();

//         if (entries.isEmpty()) {
//             JLabel none = new JLabel("No scores yet for " + difficulty.name() + ".");
//             none.setFont(new Font("SansSerif", Font.ITALIC, 18));
//             none.setForeground(new Color(210, 205, 190));
//             none.setHorizontalAlignment(SwingConstants.CENTER);
//             gbc.gridy = 0;
//             panel.add(none, gbc);
//         } else {
//             for (int rank = 0; rank < entries.size() && rank < 10; rank++) {
//                 int[] entry   = entries.get(rank);
//                 Player player = allPlayers.get(entry[1]);
//                 int score     = entry[0];

//                 boolean isCurrentUser = currentUser != null
//                         && player.getUsername().equals(currentUser.getUsername());

//                 String text = (rank + 1) + ".   " + player.getUsername()
//                         + "   —   " + score + " pts";

//                 JLabel row = new JLabel(text);
//                 row.setFont(new Font("SansSerif", Font.BOLD, 20));
//                 row.setForeground(isCurrentUser ? ACCENT : FOREGROUND);
//                 row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

//                 gbc.gridy = rank * 2;
//                 panel.add(row, gbc);

//                 JSeparator separator = new JSeparator();
//                 separator.setForeground(new Color(92, 92, 118));
//                 separator.setBackground(new Color(92, 92, 118));
//                 gbc.gridy = rank * 2 + 1;
//                 panel.add(separator, gbc);
//             }
//         }

//         gbc.gridy++;
//         gbc.weighty = 1.0;
//         gbc.fill = GridBagConstraints.BOTH;
//         panel.add(Box.createVerticalGlue(), gbc);

//         JScrollPane scroll = new JScrollPane(panel);
//         scroll.setBorder(null);
//         scroll.getViewport().setBackground(BACKGROUND);
//         scroll.setBackground(BACKGROUND);

//         JPanel wrapper = new JPanel(new BorderLayout());
//         wrapper.setBackground(BACKGROUND);
//         wrapper.add(scroll, BorderLayout.CENTER);
//         return wrapper;
//     }

//     private List<Player> loadAllPlayers() {
//         try {
//             FileDataStore store = new FileDataStore(Path.of("data").toString());
//             return store.loadPlayers();
//         } catch (Exception e) {
//             System.err.println("HighScorePanel: could not load players — " + e.getMessage());
//             return new ArrayList<>();
//         }
//     }
// }
package frontend;

import backend.logic.AuthService;
import backend.model.Difficulty;
import backend.model.Player;
import backend.model.PlayerStats;
import backend.model.Score;
import backend.storage.FileDataStore;
import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * HighScorePanel displays leaderboard and player performance information.
 * It allows users to browse personal statistics and compare scores across
 * the available difficulty levels using a styled multi-view layout.
 */
public class HighScorePanel extends BaseScreenPanel {

    private final AuthService authService;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton currentStatsButton;
    private JButton leaderboardButton;
    private final List<JButton> difficultyButtons = new ArrayList<>();
    private final Color DATA_AREA_BG = new Color(28, 28, 42);
    private final Border THEMED_BORDER = BorderFactory.createLineBorder(BTN_BORDER, 2);

    public HighScorePanel(ScreenManager screenManager, AuthService authService) {
        super("STATS & LEADERBOARDS");
        this.authService = authService;

        JPanel shell = createMockFrame();
        shell.setLayout(new BorderLayout(20, 20));

        // Tab panel Ã¢â‚¬â€ one tab per difficulty
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BaseScreenPanel.TEKTUR.deriveFont(java.awt.Font.BOLD, 14f));
        tabs.setBackground(BACKGROUND);
        tabs.setForeground(FOREGROUND);
        tabs.setOpaque(true);
        
        tabs.addTab("Easy",   buildLeaderboard(Difficulty.EASY));
        tabs.addTab("Medium", buildLeaderboard(Difficulty.MEDIUM));
        tabs.addTab("Hard",   buildLeaderboard(Difficulty.HARD));
        tabs.addTab("Endless", buildLeaderboard(Difficulty.ENDLESS));
        // Top toggle buttons - SAME DESIGN AS BACK BUTTON
        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        togglePanel.setOpaque(false);
        togglePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        currentStatsButton = createMenuButton("Current Player Stats");
        leaderboardButton = createMenuButton("Leaderboard");

        currentStatsButton.addActionListener(e -> showCard("stats"));
        leaderboardButton.addActionListener(e -> showCard("leaderboard"));

        togglePanel.add(currentStatsButton);
        togglePanel.add(leaderboardButton);

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildPlayerStatsCard(), "stats");
        contentPanel.add(buildLeaderboardCard(), "leaderboard");

        // Bottom back button
        JButton backButton = createMenuButton("Back");
        backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.PLAYER_MENU));

        shell.add(togglePanel, BorderLayout.NORTH);
        shell.add(contentPanel, BorderLayout.CENTER);
        shell.add(backButton, BorderLayout.SOUTH);
        
        add(shell, BorderLayout.CENTER);

        // Show stats by default
        showCard("stats");
    }

    private void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
        
        // Update button styles to show which is active
        if (cardName.equals("stats")) {
            currentStatsButton.putClientProperty("active", true);
            leaderboardButton.putClientProperty("active", false);
        } else {
            currentStatsButton.putClientProperty("active", false);
            leaderboardButton.putClientProperty("active", true);
        }
        repaint();
    }

    private JPanel buildPlayerStatsCard() {
        Player currentPlayer = authService.getCurrentPlayer();
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(DATA_AREA_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(THEMED_BORDER, BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.insets = new Insets(8, 12, 8, 12);

        if (currentPlayer == null) {
            JLabel guestLabel = new JLabel("You are playing as a Guest.");
            guestLabel.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.ITALIC, 20f));
            guestLabel.setForeground(new Color(210, 205, 190));
            guestLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 0;
            panel.add(guestLabel, gbc);
            
            JLabel guestHint = new JLabel("Login to track your statistics!");
            guestHint.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.PLAIN, 16f));
            guestHint.setForeground(new Color(180, 175, 160));
            guestHint.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 1;
            panel.add(guestHint, gbc);
        } else {
            PlayerStats stats = currentPlayer.getStats();
            
            // Player name header - SHOWS THE LOGGED-IN USERNAME
            JLabel nameHeader = new JLabel("Player: " + currentPlayer.getUsername());
            nameHeader.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 28f));
            nameHeader.setForeground(BTN_BORDER);
            nameHeader.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(nameHeader, gbc);
            
            gbc.gridwidth = 1;
            gbc.gridy++;
            
            // Add separator
            JSeparator separator1 = new JSeparator();
            separator1.setForeground(new Color(92, 92, 118));
            separator1.setBackground(new Color(92, 92, 118));
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(12, 12, 12, 12);
            panel.add(separator1, gbc);
            gbc.gridwidth = 1;
            gbc.gridy++;
            
            // Statistics section
            gbc.insets = new Insets(6, 12, 6, 12);
            
            // Format time played
            int totalSeconds = stats.getTotalTimePlayed();
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            String timePlayedStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            
            // Add all stats
            addStatRow(panel, gbc, "Overall High Score:", String.valueOf(stats.getHighScore()));
            addStatRow(panel, gbc, "Average WPM:", String.format("%.1f", stats.getAverageWPM()));
            addStatRow(panel, gbc, "Peak WPM:", String.format("%.1f", stats.getPeakWPM()));
            addStatRow(panel, gbc, "Accuracy:", String.format("%.1f%%", stats.getAccuracy()));
            addStatRow(panel, gbc, "Total Words Typed:", String.valueOf(stats.getWordsTyped()));
            addStatRow(panel, gbc, "Total Errors:", String.valueOf(stats.getErrorCount()));
            addStatRow(panel, gbc, "Total Time Played:", timePlayedStr);
            addStatRow(panel, gbc, "Highest Level Reached:", String.valueOf(stats.getHighestLevel()));
            
            // Add separator before difficulty scores
            gbc.gridy++;
            JSeparator separator2 = new JSeparator();
            separator2.setForeground(new Color(92, 92, 118));
            separator2.setBackground(new Color(92, 92, 118));
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(12, 12, 12, 12);
            panel.add(separator2, gbc);
            gbc.gridwidth = 1;
            gbc.gridy++;
            
            // Difficulty-specific scores
            JLabel scoresHeader = new JLabel("Best Scores by Difficulty");
            scoresHeader.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 20f));
            scoresHeader.setForeground(BTN_BORDER);
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(6, 12, 12, 12);
            panel.add(scoresHeader, gbc);
            gbc.gridwidth = 1;
            gbc.gridy++;
            
            gbc.insets = new Insets(6, 12, 6, 12);
            addStatRow(panel, gbc, "Easy:", getScoreForDifficulty(currentPlayer, Difficulty.EASY));
            addStatRow(panel, gbc, "Medium:", getScoreForDifficulty(currentPlayer, Difficulty.MEDIUM));
            addStatRow(panel, gbc, "Hard:", getScoreForDifficulty(currentPlayer, Difficulty.HARD));
            addStatRow(panel, gbc, "Endless:", getScoreForDifficulty(currentPlayer, Difficulty.ENDLESS));
        }

        // Add vertical glue at the end to push content to top
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        // JScrollPane with hidden bars
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(DATA_AREA_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildLeaderboardCard() {
        CardLayout internalLayout = new CardLayout();
        JPanel cardPanel = new JPanel(internalLayout);
        cardPanel.setOpaque(false);

        // Difficulty navigation - Styled like Back buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        difficultyButtons.clear();
        Difficulty[] difficulties = {Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.ENDLESS};
        for (Difficulty d : difficulties) {
            JButton btn = createMenuButton(d.name());
            btn.setPreferredSize(new Dimension(140, 45));
            btn.addActionListener(e -> {
                internalLayout.show(cardPanel, d.name());
                updateDifficultyButtonStates(btn);
            });
            buttonPanel.add(btn);
            difficultyButtons.add(btn);
            cardPanel.add(buildLeaderboard(d), d.name());
        }
        if (!difficultyButtons.isEmpty()) updateDifficultyButtonStates(difficultyButtons.get(0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(buttonPanel, BorderLayout.NORTH);
        wrapper.add(cardPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateDifficultyButtonStates(JButton active) {
        for (JButton btn : difficultyButtons) {
            btn.putClientProperty("active", btn == active);
        }
        repaint();
    }
    
    private void addStatRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        // Label
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 18f));
        labelComp.setForeground(new Color(245, 235, 210));
        gbc.gridx = 0;
        panel.add(labelComp, gbc);
        
        // Value
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.PLAIN, 18f));
        valueComp.setForeground(FOREGROUND);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 1;
        panel.add(valueComp, gbc);
        
        gbc.gridy++;
    }
    
    private String getScoreForDifficulty(Player player, Difficulty difficulty) {
        for (Score score : player.getScores()) {
            if (score.getDifficulty() == difficulty && score.getValue() > 0) {
                return String.valueOf(score.getValue());
            }
        }
        return "Not played";
    }

    private JPanel buildLeaderboard(Difficulty difficulty) {
        List<Player> allPlayers = loadAllPlayers();
        List<int[]> entries = new ArrayList<>();

        for (int i = 0; i < allPlayers.size(); i++) {
            Player p = allPlayers.get(i);
            for (Score s : p.getScores()) {
                if (s.getDifficulty() == difficulty && s.getValue() > 0) {
                    entries.add(new int[]{s.getValue(), i});
                }
            }
        }

        entries.sort((a, b) -> Integer.compare(b[0], a[0]));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);

        Player currentUser = authService.getCurrentPlayer();

        if (entries.isEmpty()) {
            JLabel none = new JLabel("No scores yet for " + difficulty.name() + ".");
            none.setFont(BaseScreenPanel.TEKTUR.deriveFont(java.awt.Font.ITALIC, 12f));
            none.setForeground(new Color(210, 205, 190));
            none.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 0;
            panel.add(none, gbc);
        } else {
            for (int rank = 0; rank < entries.size() && rank < 10; rank++) {
                int[] entry   = entries.get(rank);
                Player player = allPlayers.get(entry[1]);
                int score     = entry[0];

                boolean isCurrentUser = currentUser != null
                        && player.getUsername().equals(currentUser.getUsername());

                String text = (rank + 1) + ".   " + player.getUsername()
                        + " - " + score + " pts";

                JLabel row = new JLabel(text);
                row.setFont(BaseScreenPanel.TEKTUR.deriveFont(java.awt.Font.BOLD, 20f));
                row.setForeground(isCurrentUser ? BTN_BORDER : FOREGROUND);
                row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

                gbc.gridy = rank * 2;
                panel.add(row, gbc);

                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(92, 92, 118));
                separator.setBackground(new Color(92, 92, 118));
                gbc.gridy = rank * 2 + 1;
                panel.add(separator, gbc);
            }
        }

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        // Hidden scrollbars
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(DATA_AREA_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private List<Player> loadAllPlayers() {
        try {
            FileDataStore store = new FileDataStore(Path.of("data").toString());
            return store.loadPlayers();
        } catch (Exception e) {
            System.err.println("HighScorePanel: could not load players Ã¢â‚¬â€ " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
