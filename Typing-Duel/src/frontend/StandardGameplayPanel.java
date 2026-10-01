// package frontend;

// import backend.logic.AuthService;
// import backend.logic.PhaseManager;
// import backend.logic.TypingEngine;
// import backend.logic.WordBank;
// import backend.model.Difficulty;
// import backend.model.Player;
// import backend.model.PlayerStats;
// import backend.storage.FileDataStore;
// import java.awt.BasicStroke;
// import java.awt.BorderLayout;
// import java.awt.Color;
// import java.awt.Component;
// import java.awt.Dimension;
// import java.awt.FlowLayout;
// import java.awt.Font;
// import java.awt.FontMetrics;
// import java.awt.GradientPaint;
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.GridLayout;
// import java.awt.Insets;
// import java.awt.Polygon;
// import java.awt.RenderingHints;
// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import javax.swing.BorderFactory;
// import javax.swing.Box;
// import javax.swing.BoxLayout;
// import javax.swing.ImageIcon;
// import javax.swing.JButton;
// import javax.swing.JComponent;
// import javax.swing.JLabel;
// import javax.swing.JOptionPane;
// import javax.swing.JPanel;
// import javax.swing.JProgressBar;
// import javax.swing.JTextField;
// import javax.swing.SwingConstants;
// import javax.swing.SwingUtilities;
// import javax.swing.Timer;
// import javax.swing.UIManager;
// import javax.swing.event.AncestorEvent;
// import javax.swing.event.AncestorListener;
// import javax.swing.plaf.basic.BasicProgressBarUI;

// public class StandardGameplayPanel extends JPanel {

//     private static final String ATTACK = "ATTACK";
//     private static final String DEFEND = "DEFEND";

//     private static final Color PAGE_BG = new Color(20, 36, 56);
//     private static final Color HEADER_BG = new Color(17, 42, 66);
//     private static final Color TEXT_YELLOW = new Color(255, 240, 40);
//     private static final Color LIGHT_TEXT = new Color(245, 235, 210);
//     private static final Color INPUT_BG = new Color(40, 40, 60);
//     private static final Color INPUT_BORDER = new Color(255, 230, 40);
//     private static final Color PANEL_BG = new Color(28, 28, 42);

//     private JProgressBar playerHealth;
//     private JProgressBar enemyHealth;
//     private JProgressBar timerBar;

//     private JLabel phaseLabel;
//     private JLabel scoreLabel;
//     private JLabel wpmLabel;
//     private JLabel matchTimerLabel;
//     private JLabel modeLabel;
//     private JLabel powerUpLabel;
//     private JLabel targetWordLabel;
//     private JTextField inputField;
//     private JLabel playerSprite;
//     private JLabel enemySprite;

//     private ImageIcon playerAttackChargeIcon;
//     private ImageIcon playerAttackSuccessIcon;
//     private ImageIcon playerDefendChargeIcon;
//     private ImageIcon playerDefendFailIcon;

//     private JPanel centerPanel;
//     private Timer turnTimer;
//     private Timer matchTimer;
//     private long matchStartTime;

//     private String targetWord;
//     private List<String> availableWords;
//     private final StringBuilder currentInput = new StringBuilder();

//     private String currentPhase = ATTACK;
//     private int score = 0;
//     private int successfulInputs = 0;

//     private int matchTimerSeconds = 0;
//     private int totalCharactersTyped = 0;
//     private int totalKeystrokes = 0;
//     private int correctKeystrokes = 0;
//     private int errorCount = 0;
//     private int totalWordErrors = 0;
//     private long sessionStartMs = 0;

//     private boolean paused = false;
//     private boolean gameOver = false;

//     private JButton pauseButton;
//     private ScreenManager screenManager;
//     private final boolean endlessSelected;
//     private final String difficulty;
//     private final AuthService authService;
//     private final FileDataStore dataStore;
//     private final WordBank wordBank;
//     private final TypingEngine typingEngine;
//     private final PhaseManager phaseManager;

//     public StandardGameplayPanel(ScreenManager screenManager, Boolean endlessSelected, String difficulty, AuthService authService) {
//         this.screenManager = screenManager;
//         this.endlessSelected = endlessSelected;
//         this.difficulty = difficulty;
//         this.authService = authService;

//         try {
//             this.dataStore = new FileDataStore("data");
//         } catch (IOException e) {
//             throw new RuntimeException("Unable to initialize data storage", e);
//         }

//         this.wordBank = new WordBank(dataStore);
//         this.typingEngine = new TypingEngine();
//         this.phaseManager = new PhaseManager(typingEngine);

//         setLayout(new BorderLayout(10, 10));
//         setBackground(PAGE_BG);

//         addAncestorListener(new AncestorListener() {
//             @Override
//             public void ancestorAdded(AncestorEvent event) {
//                 initializeGame();
//             }

//             @Override
//             public void ancestorRemoved(AncestorEvent event) {}

//             @Override
//             public void ancestorMoved(AncestorEvent event) {}
//         });

//         initializeGame();
//     }

//     private void initializeGame() {
//         removeAll();

//         currentPhase = ATTACK;
//         score = 0;
//         successfulInputs = 0;
//         paused = false;
//         gameOver = false;
//         currentInput.setLength(0);
//         totalWordErrors = 0;
//         sessionStartMs = System.currentTimeMillis();
//         matchTimerSeconds = 0;
//         totalCharactersTyped = 0;
//         totalKeystrokes = 0;
//         correctKeystrokes = 0;
//         errorCount = 0;

//         phaseManager.reset();
//         currentPhase = phaseManager.getCurrentPhase().name();
//         loadSpriteIcons();

//         setBackground(PAGE_BG);

//         buildTopPanel();
//         buildCenterPanel();
//         loadWords();
//         generateNewWord();
//         setupTimer();
//         setupMatchTimer();
//         setupKeyBindings();

//         matchStartTime = System.currentTimeMillis();
//         updateMatchTimerLabel();
//         updateHpBars();
//         updateStats();

//         revalidate();
//         repaint();

//         SwingUtilities.invokeLater(() -> {
//             if (inputField != null) {
//                 inputField.requestFocusInWindow();
//             } else {
//                 requestFocusInWindow();
//             }
//         });

//         turnTimer.start();
//         if (matchTimer != null) {
//             matchTimer.start();
//         }
//     }

//     private void buildTopPanel() {
//         JPanel topPanel = new JPanel(new BorderLayout(0, 12));
//         topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
//         topPanel.setBackground(HEADER_BG);
//         topPanel.setOpaque(true);

//         JPanel metricsPanel = new JPanel(new GridLayout(1, 5, 24, 0));
//         metricsPanel.setOpaque(false);

//         scoreLabel = createLabel("Score: 0");
//         wpmLabel = createLabel("WPM: 0");
//         matchTimerLabel = createLabel("Time: 00:00");
//         modeLabel = createLabel("Mode: " + difficulty.toUpperCase());
//         powerUpLabel = createLabel("Power-Up: None");

//         metricsPanel.add(scoreLabel);
//         metricsPanel.add(wpmLabel);
//         metricsPanel.add(matchTimerLabel);
//         metricsPanel.add(modeLabel);
//         metricsPanel.add(powerUpLabel);

//         pauseButton = createGameButton("Pause");
//         pauseButton.setPreferredSize(new Dimension(120, 42));
//         pauseButton.setFocusable(false);
//         pauseButton.addActionListener(e -> togglePause());

//         JPanel statusPanel = new JPanel(new BorderLayout());
//         statusPanel.setOpaque(false);
//         statusPanel.add(metricsPanel, BorderLayout.CENTER);
//         statusPanel.add(pauseButton, BorderLayout.EAST);

//         JPanel healthPanel = new JPanel(new GridLayout(1, 2, 56, 0));
//         healthPanel.setOpaque(false);

//         playerHealth = createHPBar("Player HP", true);
//         enemyHealth = createHPBar("Enemy HP", false);

//         healthPanel.add(playerHealth);
//         healthPanel.add(enemyHealth);

//         topPanel.add(statusPanel, BorderLayout.NORTH);
//         topPanel.add(healthPanel, BorderLayout.CENTER);

//         add(topPanel, BorderLayout.NORTH);
//     }

//     private void buildCenterPanel() {
//         centerPanel = new JPanel(new GridBagLayout());
//         centerPanel.setOpaque(false);

//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(10, 10, 10, 10);
//         gbc.anchor = GridBagConstraints.CENTER;
//         gbc.weightx = 1.0;

//         phaseLabel = new JLabel("ATTACK!");
//         phaseLabel.setFont(BaseScreenPanel.BITCOUNT.deriveFont(52f));
//         phaseLabel.setForeground(Color.RED);
//         phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         gbc.gridwidth = 2;
//         gbc.weighty = 0.0;
//         gbc.fill = GridBagConstraints.NONE;
//         centerPanel.add(phaseLabel, gbc);

//         gbc.gridwidth = 1;
//         gbc.gridy = 1;
//         gbc.insets = new Insets(0, 50, 50, 50);
//         gbc.weighty = 0.35;

//         playerSprite = new JLabel();
//         playerSprite.setHorizontalAlignment(SwingConstants.CENTER);
//         playerSprite.setPreferredSize(new Dimension(240, 240));
//         playerSprite.setMinimumSize(new Dimension(240, 240));
//         gbc.gridx = 0;
//         gbc.fill = GridBagConstraints.NONE;
//         centerPanel.add(playerSprite, gbc);

//         enemySprite = new JLabel();
//         enemySprite.setHorizontalAlignment(SwingConstants.CENTER);
//         enemySprite.setPreferredSize(new Dimension(240, 240));
//         enemySprite.setMinimumSize(new Dimension(240, 240));
//         gbc.gridx = 1;
//         centerPanel.add(enemySprite, gbc);

//         gbc.gridx = 0;
//         gbc.gridy = 2;
//         gbc.gridwidth = 2;
//         gbc.insets = new Insets(0, 0, 10, 0);
//         gbc.weighty = 0.0;
//         gbc.fill = GridBagConstraints.HORIZONTAL;

//         targetWordLabel = new JLabel("Word: [Loading...]");
//         targetWordLabel.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 38f));
//         targetWordLabel.setForeground(TEXT_YELLOW);
//         targetWordLabel.setHorizontalAlignment(SwingConstants.CENTER);
//         centerPanel.add(targetWordLabel, gbc);

//         gbc.gridy = 3;
//         gbc.insets = new Insets(10, 160, 18, 160);
//         inputField = new JTextField(15);
//         inputField.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 30f));
//         inputField.setHorizontalAlignment(JTextField.CENTER);
//         inputField.setBackground(INPUT_BG);
//         inputField.setForeground(LIGHT_TEXT);
//         inputField.setCaretColor(TEXT_YELLOW);
//         inputField.setBorder(BorderFactory.createCompoundBorder(
//                 BorderFactory.createLineBorder(INPUT_BORDER, 3),
//                 BorderFactory.createEmptyBorder(12, 18, 12, 18)
//         ));
//         inputField.setPreferredSize(new Dimension(900, 70));
//         inputField.addActionListener(e -> submitCurrentInput());
//         centerPanel.add(inputField, gbc);

//         gbc.gridy = 4;
//         gbc.insets = new Insets(20, 50, 20, 50);
//         gbc.weighty = 0.1;
//         gbc.fill = GridBagConstraints.HORIZONTAL;

//         timerBar = new JProgressBar(0, 100);
//         timerBar.setValue(100);
//         timerBar.setUI(new VerticalTimerUI());
//         timerBar.setPreferredSize(new Dimension(420, 22));
//         timerBar.setForeground(new Color(255, 140, 0));
//         timerBar.setBackground(new Color(70, 60, 60));
//         timerBar.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30), 2));
//         centerPanel.add(timerBar, gbc);

//         updatePhaseSprites();
//         add(centerPanel, BorderLayout.CENTER);
//     }

//     private void loadSpriteIcons() {
//         playerAttackChargeIcon = UiPreferences.loadIcon(UiPreferences.CHAR_ATK_CHARGEUP);
//         playerAttackSuccessIcon = UiPreferences.loadIcon(UiPreferences.CHAR_ATK_SUCCESS);
//         playerDefendChargeIcon = UiPreferences.loadIcon(UiPreferences.CHAR_DFND_CHARGEUP);
//         playerDefendFailIcon = UiPreferences.loadIcon(UiPreferences.CHAR_DFND_FAIL);
//     }

//     private void updatePhaseSprites() {
//         if (playerSprite == null || enemySprite == null) {
//             return;
//         }

//         if (currentPhase.equals(ATTACK)) {
//             playerSprite.setIcon(playerAttackChargeIcon);
//             enemySprite.setIcon(playerDefendChargeIcon);
//         } else {
//             playerSprite.setIcon(playerDefendChargeIcon);
//             enemySprite.setIcon(playerAttackChargeIcon);
//         }
//     }

//     private void updateSprites() {
//         updatePhaseSprites();
//     }

//     private void showSuccessSprite() {
//         if (currentPhase.equals(ATTACK) && playerSprite != null) {
//             playerSprite.setIcon(playerAttackSuccessIcon);
//         } else if (currentPhase.equals(DEFEND) && enemySprite != null) {
//             enemySprite.setIcon(playerDefendFailIcon);
//         }

//         Timer spriteTimer = new Timer(400, e -> updateSprites());
//         spriteTimer.setRepeats(false);
//         spriteTimer.start();
//     }

//     private void showFailSprite() {
//         if (currentPhase.equals(ATTACK) && enemySprite != null) {
//             enemySprite.setIcon(playerAttackSuccessIcon);
//         } else if (currentPhase.equals(DEFEND) && playerSprite != null) {
//             playerSprite.setIcon(playerDefendFailIcon);
//         }

//         Timer spriteTimer = new Timer(400, e -> updateSprites());
//         spriteTimer.setRepeats(false);
//         spriteTimer.start();
//     }

//     private void setupTimer() {
//         if (turnTimer != null) {
//             turnTimer.stop();
//         }

//         turnTimer = new Timer(50, e -> {
//             if (paused || gameOver) {
//                 return;
//             }

//             int val = timerBar.getValue();
//             int depletion = calculateTimerDepletion();

//             if (val > 0) {
//                 timerBar.setValue(Math.max(0, val - depletion));
//             } else {
//                 if (currentPhase.equals(DEFEND)) {
//                     processTurn(false);
//                 } else {
//                     if (!checkGameOver()) {
//                         switchPhase();
//                         resetTimer();
//                     }
//                 }
//             }
//         });
//     }

//     private int calculateTimerDepletion() {
//         int base = 2;
//         if (endlessSelected) {
//             base += 1 + (successfulInputs / 10);
//         }
//         return Math.min(base, 8);
//     }

//     private void setupMatchTimer() {
//         if (matchTimer != null) {
//             matchTimer.stop();
//         }

//         matchTimer = new Timer(1000, e -> {
//             if (paused || gameOver) {
//                 return;
//             }
//             matchTimerSeconds++;
//             updateMatchTimerLabel();
//         });
//     }

//     private void updateMatchTimerLabel() {
//         if (matchTimerLabel == null) {
//             return;
//         }
//         long elapsedMs = System.currentTimeMillis() - matchStartTime;
//         int seconds = (int) (elapsedMs / 1000);
//         int minutes = seconds / 60;
//         int remainder = seconds % 60;
//         matchTimerLabel.setText(String.format("Time: %02d:%02d", minutes, remainder));
//     }

//     private void setupKeyBindings() {
//         SwingUtilities.invokeLater(() -> {
//             if (inputField != null) {
//                 inputField.requestFocusInWindow();
//             } else {
//                 requestFocusInWindow();
//             }
//         });
//     }

//     private void submitCurrentInput() {
//         if (paused || gameOver || turnTimer == null || inputField == null) {
//             return;
//         }

//         String typed = inputField.getText().trim();
//         if (typed.isEmpty()) {
//             return;
//         }

//         totalKeystrokes += typed.length();

//         boolean correct = targetWord != null && typed.equalsIgnoreCase(targetWord);

//         if (correct) {
//             totalCharactersTyped += targetWord.length();
//             correctKeystrokes += targetWord.length();
//         } else {
//             errorCount++;
//             totalWordErrors++;
//         }

//         processTurn(correct);
//         inputField.setText("");
//         generateNewWord();
//     }

//     private void loadWords() {
//         List<String> allWordsForDifficulty = wordBank.getWordsByDifficulty(difficulty)
//                 .stream()
//                 .map(entry -> entry.getText())
//                 .collect(java.util.stream.Collectors.toList());
//         availableWords = new ArrayList<>(allWordsForDifficulty);
//         Collections.shuffle(availableWords);
//     }

//     private String getNextWord() {
//         if (availableWords == null || availableWords.isEmpty()) {
//             loadWords();
//         }
//         if (availableWords.isEmpty()) {
//             return "typing";
//         }
//         return availableWords.remove(0);
//     }

//     private void generateNewWord() {
//         targetWord = getNextWord();
//         typingEngine.loadWord(targetWord);

//         if (targetWordLabel != null) {
//             targetWordLabel.setText("Word: " + targetWord);
//         }

//         if (inputField != null) {
//             inputField.setText("");
//         }
//     }

//     private void processTurn(boolean success) {
//         if (paused || gameOver) {
//             return;
//         }

//         turnTimer.stop();

//         if (currentPhase.equals(ATTACK)) {
//             if (success) {
//                 showSuccessSprite();
//                 score += 100;
//                 successfulInputs++;
//             } else {
//                 showFailSprite();
//             }
//             phaseManager.resolvePhaseEffect(success, 10, endlessSelected);
//         } else {
//             if (!success) {
//                 showFailSprite();
//             } else {
//                 showSuccessSprite();
//                 score += 50;
//                 successfulInputs++;
//             }
//             phaseManager.resolvePhaseEffect(success, 10, endlessSelected);
//         }

//         updateHpBars();
//         updateStats();

//         if (!checkGameOver()) {
//             switchPhase();
//             resetTimer();
//         }
//     }

//     private void updateHpBars() {
//         if (playerHealth != null) {
//             playerHealth.setValue(phaseManager.getPlayerHp());
//         }
//         if (enemyHealth != null) {
//             enemyHealth.setValue(phaseManager.getOpponentHp());
//         }
//     }

//     private void switchPhase() {
//         phaseManager.togglePhase();
//         currentPhase = phaseManager.getCurrentPhase().name();
//         phaseLabel.setText(currentPhase + "!");

//         if (currentPhase.equals(ATTACK)) {
//             phaseLabel.setForeground(Color.RED);
//         } else {
//             phaseLabel.setForeground(new Color(70, 170, 255));
//         }

//         targetWord = getNextWord();
//         if (targetWordLabel != null) {
//             targetWordLabel.setText("Word: " + targetWord);
//         }

//         updatePhaseSprites();
//     }

//     private void resetTimer() {
//         timerBar.setValue(100);
//         timerBar.repaint();
//         turnTimer.start();
//     }

//     private boolean checkGameOver() {
//         boolean gameEnded = endlessSelected ? phaseManager.getPlayerHp() <= 0 : phaseManager.isGameOver();
//         if (gameEnded) {
//             gameOver = true;
//             turnTimer.stop();
//             if (matchTimer != null) {
//                 matchTimer.stop();
//             }
//             recordScoreOnGameOver();
//             showEndScreen(!endlessSelected && phaseManager.getOpponentHp() <= 0);
//             return true;
//         }
//         return false;
//     }

//     private void recordScoreOnGameOver() {
//         updateStatsAndSaveScore();
//     }

//     private void updateStatsAndSaveScore() {
//         Player currentPlayer = authService.getCurrentPlayer();
//         if (currentPlayer == null) {
//             return;
//         }

//         PlayerStats stats = currentPlayer.getStats();

//         long sessionTimeSeconds = (System.currentTimeMillis() - sessionStartMs) / 1000;
//         double minutes = sessionTimeSeconds / 60.0;
//         double sessionWPM = minutes > 0 ? (totalCharactersTyped / 5.0) / minutes : 0;
//         double accuracyPercent = totalKeystrokes > 0
//                 ? ((double) correctKeystrokes / totalKeystrokes) * 100
//                 : 0;

//         stats.setTotalTimePlayed(stats.getTotalTimePlayed() + (int) sessionTimeSeconds);
//         stats.setWordsTyped(stats.getWordsTyped() + (int) (totalCharactersTyped / 5));
//         stats.setErrorCount(stats.getErrorCount() + totalWordErrors);

//         if (stats.getAverageWPM() == 0) {
//             stats.setAverageWPM(sessionWPM);
//         } else {
//             stats.setAverageWPM((stats.getAverageWPM() + sessionWPM) / 2.0);
//         }

//         if (sessionWPM > stats.getPeakWPM()) {
//             stats.setPeakWPM(sessionWPM);
//         }

//         stats.setAccuracy(accuracyPercent);

//         if (this.score > stats.getHighScore()) {
//             stats.setHighScore(this.score);
//         }

//         if (!endlessSelected) {
//             try {
//                 Difficulty diff = Difficulty.valueOf(difficulty);
//                 boolean alreadyHasScore = currentPlayer.hasScoreFor(diff);
//                 int currentBest = getBestScoreFor(currentPlayer, diff);

//                 if (!alreadyHasScore || score > currentBest) {
//                     currentPlayer.addOrReplaceScore(diff, score);
//                 }
//             } catch (IllegalArgumentException e) {
//                 System.err.println("Invalid difficulty: " + difficulty);
//             }
//         }

//         dataStore.savePlayer(currentPlayer);
//     }

//     private int getBestScoreFor(Player player, Difficulty diff) {
//         return player.getScores().stream()
//                 .filter(s -> s.getDifficulty() == diff)
//                 .mapToInt(s -> s.getValue())
//                 .findFirst()
//                 .orElse(0);
//     }

//     private void showEndScreen(boolean win) {
//         centerPanel.removeAll();
//         centerPanel.setLayout(new BorderLayout());
//         centerPanel.revalidate();
//         centerPanel.repaint();

//         String bannerText = win ? "✦ VICTORY ✦" : "✦ DEFEAT ✦";

//         JPanel bannerPanel = new JPanel() {
//             @Override
//             protected void paintComponent(Graphics g) {
//                 Graphics2D g2 = (Graphics2D) g.create();
//                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//                 GradientPaint gp = new GradientPaint(
//                         0, 0, new Color(255, 140, 0),
//                         0, getHeight(), new Color(180, 60, 0)
//                 );
//                 g2.setPaint(gp);
//                 g2.fillRect(0, 0, getWidth(), getHeight());

//                 g2.setColor(new Color(30, 30, 30));
//                 g2.setStroke(new BasicStroke(4));
//                 g2.drawLine(0, 2, getWidth(), 2);
//                 g2.drawLine(0, getHeight() - 3, getWidth(), getHeight() - 3);

//                 g2.setColor(new Color(220, 220, 220));
//                 g2.setFont(BaseScreenPanel.BITCOUNT.deriveFont(36f));
//                 FontMetrics fm = g2.getFontMetrics();
//                 int textX = (getWidth() - fm.stringWidth(bannerText)) / 2;
//                 int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
//                 g2.drawString(bannerText, textX, textY);

//                 g2.dispose();
//             }
//         };
//         bannerPanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 80));
//         bannerPanel.setOpaque(false);

//         JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
//         buttonsPanel.setOpaque(false);

//         JLabel finalScore = new JLabel(
//                 "Final Score: " + score + "  |  Difficulty: " + difficulty,
//                 SwingConstants.CENTER
//         );
//         finalScore.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 18f));
//         finalScore.setForeground(LIGHT_TEXT);

//         JButton restart = createGameButton("Play Again");
//         restart.setPreferredSize(new Dimension(220, 48));
//         restart.addActionListener(e -> initializeGame());

//         JButton exit = createGameButton("Back to Level Select");
//         exit.setPreferredSize(new Dimension(260, 48));
//         exit.addActionListener(e -> screenManager.showScreen(ScreenManager.LEVEL_SELECT));

//         buttonsPanel.add(finalScore);
//         buttonsPanel.add(restart);
//         buttonsPanel.add(exit);

//         JPanel wrapper = new JPanel(new BorderLayout(0, 20));
//         wrapper.setOpaque(false);
//         wrapper.add(bannerPanel, BorderLayout.NORTH);
//         wrapper.add(buttonsPanel, BorderLayout.CENTER);

//         centerPanel.add(wrapper, BorderLayout.CENTER);
//         centerPanel.revalidate();
//         centerPanel.repaint();

//         final int[] startX = {centerPanel.getWidth()};
//         bannerPanel.setBounds(startX[0], 0, centerPanel.getWidth(), 80);

//         Timer slideTimer = new Timer(8, null);
//         slideTimer.addActionListener(e -> {
//             startX[0] -= 18;
//             if (startX[0] <= 0) {
//                 startX[0] = 0;
//                 slideTimer.stop();
//             }
//             bannerPanel.setLocation(startX[0], bannerPanel.getY());
//             centerPanel.repaint();
//         });

//         centerPanel.setLayout(null);
//         int panelW = centerPanel.getWidth() > 0 ? centerPanel.getWidth() : 600;
//         int panelH = centerPanel.getHeight() > 0 ? centerPanel.getHeight() : 400;
//         bannerPanel.setBounds(panelW, panelH / 2 - 40, panelW, 80);
//         wrapper.setBounds(0, 0, panelW, panelH);
//         buttonsPanel.setBounds(panelW / 2 - 260, panelH / 2 + 60, 520, 100);

//         centerPanel.add(bannerPanel);
//         centerPanel.add(buttonsPanel);
//         centerPanel.revalidate();
//         centerPanel.repaint();

//         slideTimer.start();
//     }

//     private void togglePause() {
//         paused = !paused;
//         pauseButton.setText(paused ? "Resume" : "Pause");

//         if (paused) {
//             turnTimer.stop();
//             if (matchTimer != null) {
//                 matchTimer.stop();
//             }

//             showStyledPausePopup();

//             paused = false;
//             pauseButton.setText("Pause");
//             if (!gameOver) {
//                 turnTimer.start();
//                 if (matchTimer != null) {
//                     matchTimer.start();
//                 }
//                 phaseLabel.setText(currentPhase + "!");
//                 if (currentPhase.equals(ATTACK)) {
//                     phaseLabel.setForeground(Color.RED);
//                 } else {
//                     phaseLabel.setForeground(new Color(70, 170, 255));
//                 }
//             }
//         } else {
//             turnTimer.start();
//             if (matchTimer != null) {
//                 matchTimer.start();
//             }
//             phaseLabel.setText(currentPhase + "!");
//         }
//     }

//     private void updateStats() {
//         scoreLabel.setText("Score: " + score);
//         int wpm = (successfulInputs == 0) ? 0 : successfulInputs * 12;
//         wpmLabel.setText("WPM: " + wpm);
//     }

//     private JProgressBar createHPBar(String label, boolean isPlayer) {
//         JProgressBar bar = new JProgressBar(0, 100);
//         bar.setValue(100);
//         bar.setString(label);
//         bar.setStringPainted(true);
//         bar.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 16f));
//         bar.setForeground(isPlayer ? new Color(35, 230, 35) : new Color(255, 40, 40));
//         bar.setBackground(new Color(65, 65, 80));
//         bar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
//         bar.setPreferredSize(new Dimension(420, 24));
//         bar.setUI(new StyledHpBarUI());
//         return bar;
//     }

//     private JLabel createLabel(String text) {
//         JLabel label = new JLabel(text);
//         label.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 18f));
//         label.setForeground(TEXT_YELLOW);
//         return label;
//     }

//     private JButton createGameButton(String text) {
//         JButton button = new JButton(text) {
//             @Override
//             protected void paintComponent(Graphics g) {
//                 Graphics2D g2 = (Graphics2D) g.create();
//                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

//                 int w = getWidth();
//                 int h = getHeight();
//                 int cut = 12;

//                 int[] xPoints = {cut, w - cut, w, w, w - cut, cut, 0, 0};
//                 int[] yPoints = {0, 0, cut, h - cut, h, h, h - cut, cut};
//                 Polygon shape = new Polygon(xPoints, yPoints, 8);

//                 g2.setColor(new Color(10, 10, 10));
//                 g2.fillPolygon(shape);

//                 int[] xInner = {cut + 3, w - cut - 3, w - 3, w - 3, w - cut - 3, cut + 3, 3, 3};
//                 int[] yInner = {3, 3, cut + 3, h - cut - 3, h - 3, h - 3, h - cut - 3, cut + 3};
//                 Polygon innerShape = new Polygon(xInner, yInner, 8);

//                 GradientPaint gp = new GradientPaint(
//                         0, 0, new Color(90, 90, 90),
//                         0, h, new Color(50, 50, 50)
//                 );
//                 g2.setPaint(gp);
//                 g2.fillPolygon(innerShape);

//                 g2.setColor(new Color(130, 130, 130));
//                 g2.drawLine(cut + 4, 5, w - cut - 4, 5);

//                 g2.dispose();
//                 super.paintComponent(g);
//             }

//             @Override
//             protected void paintBorder(Graphics g) {
//             }
//         };

//         button.setFocusPainted(false);
//         button.setContentAreaFilled(false);
//         button.setOpaque(false);
//         button.setForeground(LIGHT_TEXT);
//         button.setFont(BaseScreenPanel.TEKTUR.deriveFont(12f));
//         button.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
//         button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

//         button.addMouseListener(new MouseAdapter() {
//             @Override
//             public void mouseEntered(MouseEvent e) {
//                 button.setForeground(new Color(255, 220, 120));
//             }

//             @Override
//             public void mouseExited(MouseEvent e) {
//                 button.setForeground(LIGHT_TEXT);
//             }
//         });

//         return button;
//     }

//     private void showStyledPausePopup() {
//         Color originalOptionPaneBg = UIManager.getColor("OptionPane.background");
//         Color originalPanelBg = UIManager.getColor("Panel.background");
//         Font originalButtonFont = UIManager.getFont("Button.font");
//         Color originalButtonBg = UIManager.getColor("Button.background");
//         Color originalButtonFg = UIManager.getColor("Button.foreground");

//         try {
//             UIManager.put("OptionPane.background", PANEL_BG);
//             UIManager.put("Panel.background", PANEL_BG);
//             UIManager.put("Button.font", BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 14f));
//             UIManager.put("Button.background", new Color(40, 40, 55));
//             UIManager.put("Button.foreground", LIGHT_TEXT);

//             JPanel panel = new JPanel();
//             panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//             panel.setBackground(PANEL_BG);
//             panel.setBorder(BorderFactory.createCompoundBorder(
//                     BorderFactory.createLineBorder(BaseScreenPanel.BTN_BORDER, 2),
//                     BorderFactory.createEmptyBorder(20, 24, 20, 24)
//             ));

//             JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
//             title.setAlignmentX(Component.CENTER_ALIGNMENT);
//             title.setForeground(BaseScreenPanel.ACCENT);
//             title.setFont(BaseScreenPanel.BITCOUNT.deriveFont(28f));

//             JLabel msg = new JLabel("Press OK to continue", SwingConstants.CENTER);
//             msg.setAlignmentX(Component.CENTER_ALIGNMENT);
//             msg.setForeground(LIGHT_TEXT);
//             msg.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, 16f));

//             panel.add(title);
//             panel.add(Box.createVerticalStrut(12));
//             panel.add(msg);

//             JOptionPane.showMessageDialog(
//                     this,
//                     panel,
//                     "Paused",
//                     JOptionPane.PLAIN_MESSAGE
//             );
//         } finally {
//             UIManager.put("OptionPane.background", originalOptionPaneBg);
//             UIManager.put("Panel.background", originalPanelBg);
//             UIManager.put("Button.font", originalButtonFont);
//             UIManager.put("Button.background", originalButtonBg);
//             UIManager.put("Button.foreground", originalButtonFg);
//         }
//     }

//     static class StyledHpBarUI extends BasicProgressBarUI {
//         @Override
//         protected void paintDeterminate(Graphics g, JComponent c) {
//             Graphics2D g2 = (Graphics2D) g.create();

//             int width = progressBar.getWidth();
//             int height = progressBar.getHeight();

//             g2.setColor(progressBar.getBackground());
//             g2.fillRect(0, 0, width, height);

//             int amountFull = getAmountFull(null, width, height);
//             g2.setColor(progressBar.getForeground());
//             g2.fillRect(0, 0, amountFull, height);

//             g2.setColor(Color.WHITE);
//             g2.drawRect(0, 0, width - 1, height - 1);

//             if (progressBar.isStringPainted()) {
//                 String text = progressBar.getString();
//                 g2.setFont(progressBar.getFont());
//                 FontMetrics fm = g2.getFontMetrics();
//                 int x = (width - fm.stringWidth(text)) / 2;
//                 int y = (height + fm.getAscent() - fm.getDescent()) / 2 - 1;
//                 g2.setColor(Color.WHITE);
//                 g2.drawString(text, x, y);
//             }

//             g2.dispose();
//         }
//     }

//     static class VerticalTimerUI extends BasicProgressBarUI {
//         @Override
//         protected void paintDeterminate(Graphics g, JComponent c) {
//             Graphics2D g2d = (Graphics2D) g.create();
//             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//             int width = c.getWidth();
//             int height = c.getHeight();
//             double percent = progressBar.getPercentComplete();
//             int fillWidth = (int) (width * percent);

//             g2d.setColor(c.getBackground());
//             g2d.fillRect(0, 0, width, height);

//             GradientPaint gp = new GradientPaint(
//                     0, 0, new Color(255, 190, 60),
//                     width, 0, new Color(255, 120, 0)
//             );
//             g2d.setPaint(gp);
//             g2d.fillRect(0, 0, fillWidth, height);

//             g2d.setColor(new Color(25, 25, 25));
//             g2d.drawRect(0, 0, width - 1, height - 1);

//             g2d.dispose();
//         }
//     }
// }

package frontend;

import backend.logic.AuthService;
import backend.logic.EndlessMode;
import backend.logic.PhaseManager;
import backend.logic.ProgressionService;
import backend.logic.TypingEngine;
import backend.logic.WordBank;
import backend.model.Difficulty;
import backend.model.Player;
import backend.model.PlayerStats;
import backend.storage.FileDataStore;
import backend.storage.FileDataStore.WordEntry;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

/**
 * StandardGameplayPanel represents the main gameplay screen of Typing Duel.
 * It manages the match UI, phase-based combat flow, timers, score tracking,
 * typing input, sprite updates, and gameplay audio during a standard round.
 */
public class StandardGameplayPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ATTACK = "ATTACK";
    private static final String DEFEND = "DEFEND";
    private static final int BASE_PHASE_DAMAGE = 10;
    private static final int FLURRY_RUSH_INTERVAL = 3;
    private static final int BRICK_WALL_INTERVAL = 3;
    private static final String NO_POWER_UP_TEXT = "Power-Up: None";
    private static final String ATTACK_SUCCESS_SOUND = "mixkit-metal-hit-woosh-1485.wav";
    private static final String DEFEND_SUCCESS_SOUND = "mixkit-metallic-sword-strike-2160.wav";
    private static final String DEFEND_FAIL_SOUND = "mixkit-impact-of-a-strong-punch-2155.wav";
    private static final String BACKGROUND_MUSIC = "the_mountain-game-game-music-508018.wav";

    // UI colours (from styled version)
    private static final Color PAGE_BG        = new Color(20, 36, 56);
    private static final Color HEADER_BG      = new Color(17, 42, 66);
    private static final Color TEXT_YELLOW    = new Color(255, 240, 40);
    private static final Color LIGHT_TEXT     = new Color(245, 235, 210);
    private static final Color INPUT_BG       = new Color(40, 40, 60);
    private static final Color INPUT_BORDER   = new Color(255, 230, 40);
    private static final Color PANEL_BG       = new Color(28, 28, 42);

    // Input feedback colours (from full-functionality version)
    private static final Color INPUT_DEFAULT_BG    = new Color(40, 40, 55);
    private static final Color INPUT_TEXT_COLOR    = Color.YELLOW;
    private static final Color INPUT_CORRECT_TEXT  = new Color(46, 204, 113);
    private static final Color INPUT_INCORRECT_TEXT = new Color(231, 76, 60);

    // -------------------------------------------------------------------------
    // UI components
    // -------------------------------------------------------------------------

    private JProgressBar playerHealth;
    private JProgressBar enemyHealth;
    private JProgressBar timerBar;

    private JLabel phaseLabel;
    private JLabel scoreLabel;
    private JLabel wpmLabel;
    private JLabel matchTimerLabel;
    private JLabel stageLabel;
    private JLabel powerUpLabel;
    private JLabel targetWordLabel;
    private JTextPane inputField;
    private JLabel playerSprite;
    private JLabel enemySprite;

    private enum CharacterType { KNIGHT, KNAVE, GILDED_WARRIOR }

    private CharacterType playerCharacter;
    private CharacterType enemyCharacter;

    // Maps to store the icons per character
    private final java.util.Map<CharacterType, ImageIcon> attackChargeIcons = new java.util.HashMap<>();
    private final java.util.Map<CharacterType, ImageIcon> attackSuccessIcons = new java.util.HashMap<>();
    private final java.util.Map<CharacterType, ImageIcon> defendChargeIcons = new java.util.HashMap<>();
    private final java.util.Map<CharacterType, ImageIcon> defendFailIcons = new java.util.HashMap<>();

    private ImageIcon playerKnightIcon;
    private ImageIcon playerKnaveIcon;
    private ImageIcon playerGildedWarriorIcon;
    private ImageIcon enemyKnightIcon;
    private ImageIcon enemyKnaveIcon;
    private ImageIcon enemyGildedWarriorIcon;

    private String playerChoice;   // "The Knight", "The Knave", or "The Gilded Warrior"
    private String opponentChoice; // one of the other two

    private ImageIcon playerAttackChargeIcon;
    private ImageIcon playerAttackSuccessIcon;
    private ImageIcon playerDefendChargeIcon;
    private ImageIcon playerDefendFailIcon;

    private JPanel centerPanel;
    private Timer turnTimer;
    private Timer matchTimer;
    private long matchStartTime;
    private long phaseStartMs;

    private JButton pauseButton;
    private JButton settingsButton;
    private Clip backgroundMusicClip;
    private java.awt.Image gameplayBackground;

    // -------------------------------------------------------------------------
    // Game state
    // -------------------------------------------------------------------------

    private String targetWord;
    private String loadedDifficulty;
    private WordEntry currentWordEntry;
    private List<WordEntry> availableWords;
    private final StringBuilder currentInput = new StringBuilder();

    private String currentPhase = ATTACK;
    private int score = 0;
    private int successfulInputs = 0;
    private int successfulAttacks = 0;
    private int successfulDefends = 0;
    private int pendingFlurryRushAwards = 0;
    private int pendingBrickWallAwards = 0;
    private int phaseErrorCount = 0;

    // Stats tracking
    private int matchTimerSeconds = 0;
    private int totalCharactersTyped = 0;
    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private int errorCount = 0;
    private int totalWordErrors = 0;
    private long sessionStartMs = 0;

    private boolean paused = false;
    private boolean gameOver = false;
    private boolean gameInProgress = false;
    private boolean inputFeedbackRefreshQueued = false;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final Runnable volumeRefreshListener = this::refreshAudioVolume;
    private ScreenManager screenManager;
    private final boolean endlessSelected;
    private final String difficulty;
    private final AuthService authService;
    private final FileDataStore dataStore;
    private final WordBank wordBank;
    private final TypingEngine typingEngine;
    private final PhaseManager phaseManager;
    private final EndlessMode endlessMode;
    private final ProgressionService progressionService;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public StandardGameplayPanel(ScreenManager screenManager, Boolean endlessSelected,
                                  String difficulty, AuthService authService) {
        this.screenManager   = screenManager;
        this.endlessSelected = endlessSelected;
        this.difficulty      = difficulty;
        this.authService     = authService;

        try {
            this.dataStore = new FileDataStore("data");
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize data storage", e);
        }

        this.wordBank         = new WordBank(dataStore);
        this.typingEngine     = new TypingEngine();
        this.phaseManager     = new PhaseManager(typingEngine);
        this.endlessMode      = new EndlessMode();
        this.progressionService = new ProgressionService();
        UiPreferences.addVolumeChangeListener(volumeRefreshListener);

        setLayout(new BorderLayout(10, 10));

        addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent event)   { initializeGame(); }
            @Override public void ancestorRemoved(AncestorEvent event) { stopBackgroundMusic(); }
            @Override public void ancestorMoved(AncestorEvent event)   {}
        });
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    private void initializeGame() {
        if (gameInProgress && !gameOver) return; // Break the loop if already playing
        gameInProgress = true;

        removeAll();
        stopBackgroundMusic();

        currentPhase           = ATTACK;
        score                  = 0;
        successfulInputs       = 0;
        successfulAttacks      = 0;
        successfulDefends      = 0;
        pendingFlurryRushAwards = 0;
        pendingBrickWallAwards  = 0;
        paused                 = false;
        gameOver               = false;
        currentInput.setLength(0);
        totalWordErrors        = 0;
        sessionStartMs         = System.currentTimeMillis();
        phaseErrorCount        = 0;
        loadedDifficulty       = null;
        currentWordEntry       = null;
        matchTimerSeconds      = 0;
        totalCharactersTyped   = 0;
        totalKeystrokes        = 0;
        correctKeystrokes      = 0;
        errorCount             = 0;

        phaseManager.reset();
        endlessMode.reset();
        currentPhase = phaseManager.getCurrentPhase().name();
        // Load sprite icons FIRST (still needed)
        loadSpriteIcons();

        // get player selection from ScreenManager
        String selected = screenManager.getSelectedCharacter();

        if (selected != null) {
            switch (selected) {
                case "The Gilded Warrior" -> playerCharacter = CharacterType.GILDED_WARRIOR;
                case "The Knave" -> playerCharacter = CharacterType.KNAVE;
                default -> playerCharacter = CharacterType.KNIGHT;
            }
        } else {
            playerCharacter = CharacterType.KNIGHT;
        }

        // randomize enemy (not the same as player)
        java.util.List<CharacterType> choices = new java.util.ArrayList<>();
        for (CharacterType type : CharacterType.values()) {
            if (type != playerCharacter) {
                choices.add(type);
            }
        }

        enemyCharacter = choices.get(new java.util.Random().nextInt(choices.size()));

        setBackground(PAGE_BG);

        try {
            gameplayBackground = javax.imageio.ImageIO.read(
                    new java.io.File("resources/images/backgrounds/TDgameplayBackgroundNight.png.png"));
        } catch (java.io.IOException e) {
            System.err.println("Could not load background image!");
        }

        buildTopPanel();
        buildCenterPanel();
        generateNewWord();
        setupTimer();
        setupMatchTimer();
        setupKeyBindings();

        matchStartTime = System.currentTimeMillis();
        updateMatchTimerLabel();
        updatePhaseLabel();
        updateStats();

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> {
            if (inputField != null) inputField.requestFocusInWindow();
            else                    requestFocusInWindow();
        });

        startBackgroundMusic();
        startCountdown();
    }

    // -------------------------------------------------------------------------
    // Countdown overlay (3 – 2 – 1 – BEGIN!)
    // -------------------------------------------------------------------------

    /**
     * Displays a 3-2-1-BEGIN! countdown overlay using the frame's glass pane
     * so the existing centerPanel layout is completely undisturbed.
     * Game timers are started only once "BEGIN!" finishes.
     */
    private void startCountdown() {
    String[] labels  = { "3", "2", "1", "BEGIN!" };
    Color[]  colours = { new Color(255, 100, 80), new Color(255, 180, 40), new Color(100, 230, 100), new Color(80, 200, 255) };
    float[]  sizes   = { 160f, 160f, 160f, 90f };
    int[]    delays  = { 800, 800, 800, 1000 }; // Show "BEGIN!" for 1 second

    // 1. CREATE THE OVERLAY
    JPanel glass = new JPanel(new GridBagLayout()) {
        @Override protected void paintComponent(Graphics g) {
            if (!isVisible()) return; // Don't paint if hidden
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    };
    glass.setOpaque(false);

    JLabel lbl = new JLabel(labels[0], SwingConstants.CENTER);
    lbl.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, sizes[0]));
    lbl.setForeground(colours[0]);
    glass.add(lbl, new GridBagConstraints());

    JRootPane root = SwingUtilities.getRootPane(this);
    if (root == null) return;

    Component oldGlass = root.getGlassPane();
    root.setGlassPane(glass);
    glass.setVisible(true);

    final int[] idx = {0};
    Timer t = new Timer(delays[0], null);
    
    t.addActionListener(e -> {
        if (gameOver || idx[0] > labels.length) {
            t.stop();
            return;
        }
        
        idx[0]++;
        if (idx[0] < labels.length) {
            lbl.setText(labels[idx[0]]);
            lbl.setForeground(colours[idx[0]]);
            lbl.setFont(BaseScreenPanel.TEKTUR.deriveFont(Font.BOLD, sizes[idx[0]]));
            t.setDelay(delays[idx[0]]);
        } else {
            // Cleanup countdown
            t.stop();
            glass.setVisible(false);
            glass.removeAll(); // Ensure the "BEGIN!" text is removed from the overlay
            
            // Restore original glass pane
            root.setGlassPane(oldGlass);
            
            // Force complete repaint
            root.revalidate();
            root.repaint();

            // NOW start the game timers (so glare is gone BEFORE game starts)
            matchStartTime = System.currentTimeMillis();
            if (turnTimer != null) turnTimer.start();
            if (matchTimer != null) matchTimer.start();

            if (inputField != null) {
                inputField.requestFocusInWindow();
            }
        }
    });

    t.setRepeats(true);
    t.start();
}
    private void startGameLogic() {
        matchStartTime = System.currentTimeMillis();
        if (turnTimer != null) turnTimer.start();
        if (matchTimer != null) matchTimer.start();
    }

    private void buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        topPanel.setBackground(HEADER_BG);
        topPanel.setOpaque(true);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 5, 24, 0));
        metricsPanel.setOpaque(false);

        scoreLabel     = createLabel("Score: 0");
        wpmLabel       = createLabel("WPM: --");
        matchTimerLabel = createLabel("Time: 00:00");
        stageLabel     = createLabel(endlessSelected ? endlessMode.getStageLabel() : "Mode: " + difficulty);
        powerUpLabel   = createLabel(NO_POWER_UP_TEXT);

        metricsPanel.add(scoreLabel);
        metricsPanel.add(wpmLabel);
        metricsPanel.add(matchTimerLabel);
        metricsPanel.add(stageLabel);
        metricsPanel.add(powerUpLabel);

        pauseButton = createGameButton("Pause");
        pauseButton.setPreferredSize(new Dimension(120, 42));
        pauseButton.setFocusable(false);
        pauseButton.addActionListener(e -> togglePause());

        settingsButton = createGameButton("\u2699 Settings");
        settingsButton.setPreferredSize(new Dimension(140, 42));
        settingsButton.setFocusable(false);
        settingsButton.addActionListener(e -> openSettingsPopup());

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(settingsButton);
        buttonBar.add(pauseButton);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(metricsPanel, BorderLayout.CENTER);
        statusPanel.add(buttonBar, BorderLayout.EAST);

        JPanel healthPanel = new JPanel(new GridLayout(1, 2, 56, 0));
        healthPanel.setOpaque(false);

        playerHealth = createHPBar("Player HP", true);
        enemyHealth  = createHPBar("Enemy HP", false);

        healthPanel.add(playerHealth);
        healthPanel.add(enemyHealth);

        topPanel.add(statusPanel, BorderLayout.NORTH);
        topPanel.add(healthPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
    }

    private void buildCenterPanel() {
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(10, 10, 10, 10);
        gbc.anchor  = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        // ROW 0 – phase label
        phaseLabel = new JLabel("ATTACK!");
        phaseLabel.setFont(new Font("SansSerif", Font.BOLD, 50));
        phaseLabel.setForeground(Color.RED);
        phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.NONE;
        centerPanel.add(phaseLabel, gbc);

        // ROW 1 – sprites
        gbc.gridwidth = 1; gbc.gridy = 2;

        // Push sprites DOWN toward the bottom
        gbc.insets = new Insets(180, 50, 0, 50);

        // Give them more vertical weight so they sit lower
        gbc.weighty = 0.6;
        gbc.anchor = GridBagConstraints.SOUTH;

        playerSprite = new JLabel();
        playerSprite.setHorizontalAlignment(SwingConstants.CENTER);
        playerSprite.setPreferredSize(new Dimension(240, 240));
        playerSprite.setMinimumSize(new Dimension(240, 240));
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE;
        centerPanel.add(playerSprite, gbc);

        enemySprite = new JLabel();
        enemySprite.setHorizontalAlignment(SwingConstants.CENTER);
        enemySprite.setPreferredSize(new Dimension(240, 240));
        enemySprite.setMinimumSize(new Dimension(240, 240));
        gbc.gridx = 1;
        centerPanel.add(enemySprite, gbc);

        // ROW 2 – target word
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;

        targetWordLabel = new JLabel("Word: [Loading...]");
        targetWordLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        targetWordLabel.setForeground(TEXT_YELLOW);
        targetWordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(targetWordLabel, gbc);

        // ROW 3 – input field (JTextPane for coloured feedback)
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 120, 10, 120);

        inputField = new JTextPane();
        inputField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 28));
        inputField.setOpaque(true);
        inputField.setBackground(INPUT_DEFAULT_BG);
        inputField.setForeground(INPUT_TEXT_COLOR);
        inputField.setCaretColor(INPUT_TEXT_COLOR);
        inputField.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 3));
        inputField.setMargin(new Insets(8, 12, 8, 12));
        inputField.setPreferredSize(new Dimension(360, 60));
        inputField.setMinimumSize(new Dimension(360, 60));
        inputField.setMaximumSize(new Dimension(360, 60));
        applyInputParagraphStyle();

        inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submitInput");
        inputField.getActionMap().put("submitInput", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { submitCurrentInput(); }
        });

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { scheduleInputFieldFeedbackUpdate(); }
            @Override public void removeUpdate(DocumentEvent e)  { scheduleInputFieldFeedbackUpdate(); }
            @Override public void changedUpdate(DocumentEvent e) { /* style change – no-op */ }
        });

        inputField.requestFocusInWindow();
        centerPanel.add(inputField, gbc);

        // ROW 4 – timer bar with label
        gbc.gridy = 4;
        gbc.insets = new Insets(14, 60, 18, 60);
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel timerPanel = new JPanel(new BorderLayout(0, 4));
        timerPanel.setOpaque(false);

        JLabel timerLabel = new JLabel("TIME REMAINING", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        timerLabel.setForeground(new Color(200, 180, 120));
        timerPanel.add(timerLabel, BorderLayout.NORTH);

        timerBar = new JProgressBar(0, 100);
        timerBar.setValue(100);
        timerBar.setUI(new VerticalTimerUI());
        timerBar.setPreferredSize(new Dimension(500, 22));
        timerBar.setMinimumSize(new Dimension(200, 22));
        timerBar.setForeground(new Color(255, 140, 0));
        timerBar.setBackground(new Color(50, 40, 40));
        timerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 100, 0), 1),
                BorderFactory.createLineBorder(new Color(20, 15, 15), 1)
        ));
        timerPanel.add(timerBar, BorderLayout.CENTER);

        centerPanel.add(timerPanel, gbc);

        updatePhaseSprites();
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadSpriteIcons() {
        // Preload each character set so phase changes can swap sprites instantly.
        // KNIGHT
        attackChargeIcons.put(CharacterType.KNIGHT, loadSprite("images/sprites/charATKchargeup.png", 240, 240));
        attackSuccessIcons.put(CharacterType.KNIGHT, loadSprite("images/sprites/charATKsuccessFIX.png", 240, 240));
        defendChargeIcons.put(CharacterType.KNIGHT, loadSprite("images/sprites/charDFNDchargeup.png", 240, 240));
        defendFailIcons.put(CharacterType.KNIGHT, loadSprite("images/sprites/charDFNDfail.png", 240, 240));

        // GILDED
        attackChargeIcons.put(CharacterType.GILDED_WARRIOR, loadSprite("images/sprites/charATKchargeup_BlackGold.png", 240, 240));
        attackSuccessIcons.put(CharacterType.GILDED_WARRIOR, loadSprite("images/sprites/charATKsuccessFIX_BlackGold.png", 240, 240));
        defendChargeIcons.put(CharacterType.GILDED_WARRIOR, loadSprite("images/sprites/charDFNDchargeup_BlackGold.png", 240, 240));
        defendFailIcons.put(CharacterType.GILDED_WARRIOR, loadSprite("images/sprites/charDFNDfail_BlackGold.png", 240, 240));

        // KNAVE
        attackChargeIcons.put(CharacterType.KNAVE, loadSprite("images/sprites/charATKchargeup_BlackRed.png", 240, 240));
        attackSuccessIcons.put(CharacterType.KNAVE, loadSprite("images/sprites/charATKsuccessFIX_BlackRed.png", 240, 240));
        defendChargeIcons.put(CharacterType.KNAVE, loadSprite("images/sprites/charDFNDchargeup_BlackRed.png", 240, 240));
        defendFailIcons.put(CharacterType.KNAVE, loadSprite("images/sprites/charDFNDfail_BlackRed.png", 240, 240));
}

    private ImageIcon loadSprite(String path, int w, int h) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } else {
            System.out.println("Missing sprite: " + path);
            return null;
        }
    }

    private void updatePhaseSprites() {
        if (playerSprite == null || enemySprite == null) return;

        ImageIcon playerIcon;
        ImageIcon enemyIcon;

        if (playerSprite == null || enemySprite == null) return;
        if (currentPhase.equals(ATTACK)) {
            playerIcon = attackChargeIcons.get(playerCharacter);
            enemyIcon = defendChargeIcons.get(enemyCharacter);
        } else {
            playerIcon = defendChargeIcons.get(playerCharacter);
            enemyIcon = attackChargeIcons.get(enemyCharacter);
        }

        playerSprite.setIcon(playerIcon);
        enemySprite.setIcon(flipIconHorizontally(enemyIcon));

        playerSprite.revalidate();
        playerSprite.repaint();
        enemySprite.revalidate();
        enemySprite.repaint();
    }

    private ImageIcon getPlayerCharacterIcon(String choice) {
        return switch (choice) {
            case "Knight" -> playerKnightIcon;
            case "Knave" -> playerKnaveIcon;
            case "Gilded Warrior" -> playerGildedWarriorIcon;
            default -> playerAttackChargeIcon;
        };
    }

    private ImageIcon getEnemyCharacterIcon(String choice) {
        return switch (choice) {
            case "Knight" -> enemyKnightIcon;
            case "Knave" -> enemyKnaveIcon;
            case "Gilded Warrior" -> enemyGildedWarriorIcon;
            default -> playerDefendChargeIcon;
        };
    }

    public void setPlayerCharacter(String characterName) {
        this.playerCharacter = switch (characterName) {
            case "The Knight" -> CharacterType.KNIGHT;
            case "The Knave" -> CharacterType.KNAVE;
            case "The Gilded Warrior" -> CharacterType.GILDED_WARRIOR;
            default -> CharacterType.KNIGHT;
        };

        // Pick a random different opponent
        java.util.List<CharacterType> others = new java.util.ArrayList<>();
        for (CharacterType c : CharacterType.values()) {
            if (c != playerCharacter) others.add(c);
        }
        java.util.Collections.shuffle(others);
        this.enemyCharacter = others.get(0);

        updatePhaseSprites();
    }

    private String getRandomOpponentChoice(String playerChoice) {
        List<String> options = new ArrayList<>();
        if (!playerChoice.equals("Knight")) options.add("Knight");
        if (!playerChoice.equals("Knave")) options.add("Knave");
        if (!playerChoice.equals("Gilded Warrior")) options.add("Gilded Warrior");
        Collections.shuffle(options);
        return options.get(0);
    }

    private void updateSprites() { updatePhaseSprites(); }

    private void showSuccessSprite(PhaseManager.PowerUp powerUpUsed) {
        if (currentPhase.equals(ATTACK)) {
            playerSprite.setIcon(attackSuccessIcons.get(playerCharacter));
        } else if (currentPhase.equals(DEFEND) && powerUpUsed == PhaseManager.PowerUp.BRICK_WALL) {
            playerSprite.setIcon(defendChargeIcons.get(playerCharacter));
        } else if (currentPhase.equals(DEFEND)) {
            enemySprite.setIcon(flipIconHorizontally(defendFailIcons.get(enemyCharacter)));
        }

        resetSpriteAfterDelay();
    }

    private void showFailSprite(PhaseManager.PowerUp powerUpUsed) {
        if (currentPhase.equals(ATTACK)) {
            enemySprite.setIcon(flipIconHorizontally(attackSuccessIcons.get(enemyCharacter)));
        } else if (currentPhase.equals(DEFEND) && powerUpUsed == PhaseManager.PowerUp.BRICK_WALL) {
            playerSprite.setIcon(defendChargeIcons.get(playerCharacter));
        } else if (currentPhase.equals(DEFEND)) {
            playerSprite.setIcon(defendFailIcons.get(playerCharacter));
        }

        resetSpriteAfterDelay();
    }

    private void resetSpriteAfterDelay() {
        // Briefly show the reaction sprite before restoring the default phase pose.
        Timer spriteTimer = new Timer(400, e -> updatePhaseSprites());
        spriteTimer.setRepeats(false);
        spriteTimer.start();
    }

    // -------------------------------------------------------------------------
    // Timers
    // -------------------------------------------------------------------------

    private void setupTimer() {
        if (turnTimer != null) turnTimer.stop();

        turnTimer = new Timer(50, e -> {
            if (paused || gameOver) return;

            int val       = timerBar.getValue();
            int depletion = calculateTimerDepletion();

            if (val > 0) {
                timerBar.setValue(Math.max(0, val - depletion));
            } else {
                if (currentPhase.equals(DEFEND)) {
                    processTurn(false);
                } else {
                    phaseManager.clearPowerUp();
                    updateStats();
                    if (!checkGameOver()) { switchPhase(); resetTimer(); }
                }
            }
        });
    }

    private int calculateTimerDepletion() {
        int base = 2;
        if (endlessSelected) base += 1 + (successfulInputs / 10);
        return Math.min(base, 8);
    }

    private void setupMatchTimer() {
        if (matchTimer != null) matchTimer.stop();
        matchTimer = new Timer(1000, e -> {
            if (paused || gameOver) return;
            matchTimerSeconds++;
            updateMatchTimerLabel();
        });
    }

    private void updateMatchTimerLabel() {
        if (matchTimerLabel == null) return;
        long elapsedMs = System.currentTimeMillis() - matchStartTime;
        int seconds    = (int)(elapsedMs / 1000);
        matchTimerLabel.setText(String.format("Time: %02d:%02d", seconds / 60, seconds % 60));
    }

    // -------------------------------------------------------------------------
    // Key bindings
    // -------------------------------------------------------------------------

    private void setupKeyBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pauseGame");
        getActionMap().put("pauseGame", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!gameOver && pauseButton != null && pauseButton.isShowing()) togglePause();
            }
        });
        SwingUtilities.invokeLater(() -> {
            if (inputField != null) inputField.requestFocusInWindow();
            else                    requestFocusInWindow();
        });
    }

    // -------------------------------------------------------------------------
    // Input handling
    // -------------------------------------------------------------------------

    private void submitCurrentInput() {
        if (paused || gameOver || turnTimer == null || inputField == null) return;

        String typed = inputField.getText().trim();
        if (typed.isEmpty()) return;

        totalKeystrokes += typed.length();
        boolean correct = targetWord != null && typed.equalsIgnoreCase(targetWord);

        if (correct) {
            totalCharactersTyped += targetWord.length();
            correctKeystrokes    += targetWord.length();
        } else {
            errorCount++;
            phaseErrorCount++;
            totalWordErrors++;
        }

        processTurn(correct);
        inputField.setText("");
    }

    // -------------------------------------------------------------------------
    // Word management
    // -------------------------------------------------------------------------

    private void loadWords() {
        if (endlessSelected) {
            List<WordEntry> easy   = new ArrayList<>(wordBank.getWordsByDifficulty("EASY"));
            List<WordEntry> medium = new ArrayList<>(wordBank.getWordsByDifficulty("MEDIUM"));
            List<WordEntry> hard   = new ArrayList<>(wordBank.getWordsByDifficulty("HARD"));
            Collections.shuffle(easy);
            Collections.shuffle(medium);
            Collections.shuffle(hard);

            List<WordEntry> mixed = new ArrayList<>();
            int max = Math.max(easy.size(), Math.max(medium.size(), hard.size()));
            for (int i = 0; i < max; i++) {
                if (i < easy.size())   mixed.add(easy.get(i));
                if (i < medium.size()) mixed.add(medium.get(i));
                if (i < hard.size())   mixed.add(hard.get(i));
            }
            availableWords   = mixed;
            loadedDifficulty = "ENDLESS";
        } else {
            String diff = getActiveDifficultyName();
            List<WordEntry> words = new ArrayList<>(wordBank.getWordsByDifficulty(diff));
            Collections.shuffle(words);
            availableWords   = words;
            loadedDifficulty = diff;
        }
    }

    private String getActiveDifficultyName() {
        return endlessSelected ? endlessMode.getCurrentDifficulty().name() : difficulty;
    }

    private WordEntry getNextWord() {
        String activeDiff  = getActiveDifficultyName();
        boolean needsReload = (availableWords == null || availableWords.isEmpty());
        if (!endlessSelected) needsReload |= !activeDiff.equals(loadedDifficulty);
        if (needsReload) loadWords();
        if (availableWords.isEmpty())
            return new WordEntry("typing", activeDiff, "GENERAL", BASE_PHASE_DAMAGE);
        return availableWords.remove(0);
    }

    private void generateNewWord() {
        currentWordEntry = getNextWord();
        targetWord       = currentWordEntry.getText();
        typingEngine.loadWord(targetWord);
        phaseStartMs    = System.currentTimeMillis();
        phaseErrorCount = 0;

        if (targetWordLabel != null) targetWordLabel.setText("Word: " + targetWord);
        if (inputField != null) {
            inputField.setText("");
            scheduleInputFieldFeedbackUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Input field colour feedback
    // -------------------------------------------------------------------------

    private void scheduleInputFieldFeedbackUpdate() {
        if (inputField == null || inputFeedbackRefreshQueued) return;
        inputFeedbackRefreshQueued = true;
        SwingUtilities.invokeLater(() -> {
            inputFeedbackRefreshQueued = false;
            updateInputFieldFeedback();
        });
    }

    private void updateInputFieldFeedback() {
        if (inputField == null) return;
        String typed = inputField.getText();
        StyledDocument doc = inputField.getStyledDocument();
        if (typed == null || targetWord == null || targetWord.isEmpty()) return;

        SimpleAttributeSet defaultStyle   = createInputTextStyle(INPUT_TEXT_COLOR);
        SimpleAttributeSet correctStyle   = createInputTextStyle(INPUT_CORRECT_TEXT);
        SimpleAttributeSet incorrectStyle = createInputTextStyle(INPUT_INCORRECT_TEXT);
        boolean[] correct = computeTypedCharacterMatches(typed, targetWord);

        doc.setCharacterAttributes(0, typed.length(), defaultStyle, true);
        for (int i = 0; i < typed.length(); i++) {
            boolean ok = i < correct.length && correct[i];
            doc.setCharacterAttributes(i, 1, ok ? correctStyle : incorrectStyle, true);
        }

        resetInputTypingStyle(defaultStyle);
        applyInputParagraphStyle();
        inputField.setCaretPosition(doc.getLength());
    }

    private SimpleAttributeSet createInputTextStyle(Color color) {
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setForeground(s, color);
        StyleConstants.setFontFamily(s, "Monospaced");
        StyleConstants.setFontSize(s, 28);
        StyleConstants.setBold(s, true);
        return s;
    }

    private boolean[] computeTypedCharacterMatches(String typed, String target) {
        int tl = typed.length(), rl = target.length();
        boolean[] matches = new boolean[tl];
        int[][] dp    = new int[tl + 1][rl + 1];
        byte[][] step = new byte[tl + 1][rl + 1];

        for (int i = tl; i >= 0; i--) {
            for (int j = rl; j >= 0; j--) {
                if (i == tl) { dp[i][j] = rl - j; step[i][j] = 4; continue; }
                if (j == rl) { dp[i][j] = tl - i; step[i][j] = 3; continue; }

                char tc = Character.toLowerCase(typed.charAt(i));
                char rc = Character.toLowerCase(target.charAt(j));

                if (tc == rc) { dp[i][j] = dp[i+1][j+1]; step[i][j] = 1; continue; }

                int sub = 1 + dp[i+1][j+1];
                int ins = 1 + dp[i+1][j];
                int del = 1 + dp[i][j+1];
                int best = sub; byte bs = 2;
                if (ins < best) { best = ins; bs = 3; }
                if (del < best) { best = del; bs = 4; }
                dp[i][j] = best; step[i][j] = bs;
            }
        }

        int i = 0, j = 0;
        while (i < tl && j <= rl) {
            byte a = step[i][j];
            if      (a == 1) { matches[i] = true;  i++; j++; }
            else if (a == 2) { matches[i] = false; i++; j++; }
            else if (a == 3) { matches[i] = false; i++; }
            else if (a == 4) { j++; }
            else break;
        }
        return matches;
    }

    private void resetInputTypingStyle(SimpleAttributeSet defaultStyle) {
        StyledEditorKit kit = (StyledEditorKit) inputField.getEditorKit();
        MutableAttributeSet ia = kit.getInputAttributes();
        ia.removeAttributes(ia);
        ia.addAttributes(defaultStyle);
    }

    private void applyInputParagraphStyle() {
        if (inputField == null) return;
        SimpleAttributeSet centered = new SimpleAttributeSet();
        StyleConstants.setAlignment(centered, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(centered, "Monospaced");
        StyleConstants.setFontSize(centered, 28);
        StyleConstants.setBold(centered, true);
        inputField.getStyledDocument().setParagraphAttributes(
                0, inputField.getDocument().getLength(), centered, false);
    }

    // -------------------------------------------------------------------------
    // Turn processing
    // -------------------------------------------------------------------------

    private void processTurn(boolean success) {
        if (paused || gameOver) return;

        turnTimer.stop();
        PhaseManager.PhaseResolution resolution =
                phaseManager.resolvePhaseEffectDetailed(success, getPhaseDamage(), endlessSelected);
        PhaseManager.PowerUp powerUpUsed = resolution.getPowerUpUsed();

        if (currentPhase.equals(ATTACK)) {
            if (success) { showSuccessSprite(powerUpUsed); playEffect(ATTACK_SUCCESS_SOUND); }
            else           showFailSprite(powerUpUsed);
        } else {
            if (!success) { showFailSprite(powerUpUsed);   playEffect(DEFEND_FAIL_SOUND); }
            else          { showSuccessSprite(powerUpUsed); playEffect(DEFEND_SUCCESS_SOUND); }
        }

        if (success) {
            successfulInputs++;
            if (currentPhase.equals(ATTACK)) successfulAttacks++;
            else                             successfulDefends++;
            applySuccessfulTurnRewards(powerUpUsed);
            maybeGrantPowerUp();
        }

        refreshEndlessOpponent();
        updateHpBars();
        updateStats();

        if (!checkGameOver()) { switchPhase(); resetTimer(); }
    }

    private void applySuccessfulTurnRewards(PhaseManager.PowerUp powerUpUsed) {
        if (endlessSelected) {
            long elapsedMs = System.currentTimeMillis() - phaseStartMs;
            boolean flurryUsed = currentPhase.equals(ATTACK) && powerUpUsed == PhaseManager.PowerUp.FLURRY_RUSH;
            endlessMode.onPhaseCompleted(getCurrentWordPointValue(), elapsedMs, phaseErrorCount, flurryUsed);
            score = endlessMode.getTotalScore();
            return;
        }
        score += currentPhase.equals(ATTACK) ? 100 : 50;
    }

    private int getCurrentWordPointValue() {
        return currentWordEntry != null ? currentWordEntry.getPointValue() : BASE_PHASE_DAMAGE;
    }

    private int getPhaseDamage() {
        return endlessSelected ? endlessMode.getAttackDamage() : BASE_PHASE_DAMAGE;
    }

    private void maybeGrantPowerUp() {
        if (currentPhase.equals(ATTACK) && successfulAttacks % FLURRY_RUSH_INTERVAL == 0)
            pendingFlurryRushAwards++;
        if (currentPhase.equals(DEFEND) && successfulDefends % BRICK_WALL_INTERVAL == 0)
            pendingBrickWallAwards++;
    }

    private void activatePendingPhasePowerUp() {
        if (phaseManager.getActivePowerUp() != PhaseManager.PowerUp.NONE) return;
        if (currentPhase.equals(ATTACK) && pendingFlurryRushAwards > 0) {
            phaseManager.activatePowerUp(PhaseManager.PowerUp.FLURRY_RUSH);
            pendingFlurryRushAwards--;
            return;
        }
        if (currentPhase.equals(DEFEND) && pendingBrickWallAwards > 0) {
            phaseManager.activatePowerUp(PhaseManager.PowerUp.BRICK_WALL);
            pendingBrickWallAwards--;
        }
    }

    private void refreshEndlessOpponent() {
        if (!endlessSelected || phaseManager.getOpponentHp() > 0) return;
        phaseManager.replenishOpponentHp();
    }

    // -------------------------------------------------------------------------
    // Phase / HUD updates
    // -------------------------------------------------------------------------

    private void updatePhaseLabel() {
        if (phaseLabel == null) return;
        phaseLabel.setText(currentPhase + "!");
        phaseLabel.setForeground(currentPhase.equals(ATTACK) ? Color.RED : new Color(70, 170, 255));
    }

    private String getPowerUpStatusText() {
        if (phaseManager.getActivePowerUp() == PhaseManager.PowerUp.NONE) {
            if (currentPhase.equals(ATTACK) && pendingFlurryRushAwards > 0) {
                return "Power-Up: Flurry Rush";
            }
            if (currentPhase.equals(DEFEND) && pendingBrickWallAwards > 0) {
                return "Power-Up: Brick Wall";
            }
        }
        if (phaseManager.getActivePowerUp() == PhaseManager.PowerUp.NONE) {
            if (currentPhase.equals(ATTACK) && pendingFlurryRushAwards > 0) return "Power-Up: Flurry Rush";
            if (currentPhase.equals(DEFEND) && pendingBrickWallAwards  > 0) return "Power-Up: Brick Wall";
        }
        return switch (phaseManager.getActivePowerUp()) {
            case FLURRY_RUSH -> "Power-Up: Flurry Rush";
            case BRICK_WALL  -> "Power-Up: Brick Wall";
            case NONE        -> NO_POWER_UP_TEXT;
        };
    }

    private void updateHpBars() {
        if (playerHealth != null) playerHealth.setValue(phaseManager.getPlayerHp());
        if (enemyHealth  != null) enemyHealth.setValue(phaseManager.getOpponentHp());
    }

    private void switchPhase() {
        phaseManager.togglePhase();
        currentPhase = phaseManager.getCurrentPhase().name();
        activatePendingPhasePowerUp();
        updatePhaseLabel();
        updateStats();
        generateNewWord();
        updatePhaseSprites();
    }

    private void resetTimer() {
        timerBar.setValue(100);
        timerBar.repaint();
        turnTimer.start();
    }

    private void updateStats() {
        scoreLabel.setText("Score: " + score);
        int wpm = successfulInputs == 0 ? 0 : successfulInputs * 12;
        wpmLabel.setText("WPM: " + wpm);
        if (stageLabel   != null) stageLabel.setText(endlessSelected ? endlessMode.getStageLabel() : "Mode: " + difficulty);
        if (powerUpLabel != null) powerUpLabel.setText(getPowerUpStatusText());
    }

    // -------------------------------------------------------------------------
    // Game-over / end screen
    // -------------------------------------------------------------------------

    private boolean checkGameOver() {
        boolean wonStandard = !endlessSelected && phaseManager.getOpponentHp() <= 0;
        boolean ended       = endlessSelected ? phaseManager.getPlayerHp() <= 0 : phaseManager.isGameOver();
        if (ended) {
            gameOver = true;
            gameInProgress = false;
            turnTimer.stop();
            if (matchTimer != null) matchTimer.stop();
            if (endlessSelected) endlessMode.endSession();
            recordScoreOnGameOver(wonStandard);
            showEndScreen(wonStandard);
            return true;
        }
        return false;
    }

    private void recordScoreOnGameOver(boolean wonStandardMatch) {
        updateStatsAndSaveScore(wonStandardMatch);
    }

    private void updateStatsAndSaveScore(boolean wonStandardMatch) {
        Player currentPlayer = authService.getCurrentPlayer();
        if (currentPlayer == null) return;

        PlayerStats stats = currentPlayer.getStats();
        long sessionTimeSeconds = (System.currentTimeMillis() - sessionStartMs) / 1000;
        double minutes = sessionTimeSeconds / 60.0;
        double sessionWPM = minutes > 0 ? (totalCharactersTyped / 5.0) / minutes : 0;
        double accuracyPercent = totalKeystrokes > 0
                ? ((double) correctKeystrokes / totalKeystrokes) * 100 : 0;

        stats.setTotalTimePlayed(stats.getTotalTimePlayed() + (int) sessionTimeSeconds);
        stats.setWordsTyped(stats.getWordsTyped() + (int)(totalCharactersTyped / 5));
        stats.setErrorCount(stats.getErrorCount() + totalWordErrors);

        if (stats.getAverageWPM() == 0) stats.setAverageWPM(sessionWPM);
        else stats.setAverageWPM((stats.getAverageWPM() + sessionWPM) / 2.0);

        if (sessionWPM > stats.getPeakWPM()) stats.setPeakWPM(sessionWPM);
        stats.setAccuracy(accuracyPercent);
        if (this.score > stats.getHighScore()) stats.setHighScore(this.score);

        try {
            Difficulty diff = endlessSelected ? Difficulty.ENDLESS : Difficulty.valueOf(difficulty);
            boolean alreadyHasScore = currentPlayer.hasScoreFor(diff);
            int currentBest = getBestScoreFor(currentPlayer, diff);
            if (!alreadyHasScore || score > currentBest) currentPlayer.addOrReplaceScore(diff, score);

            if (!endlessSelected) {
                progressionService.syncUnlockedProgress(currentPlayer);
                stats.setHighestLevel(Math.max(stats.getHighestLevel(), getReachedStageNumber(diff)));
                if (wonStandardMatch) progressionService.unlockNextDifficulty(currentPlayer, diff);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid difficulty: " + difficulty);
        }

        dataStore.savePlayer(currentPlayer);
    }

    private int getBestScoreFor(Player player, Difficulty diff) {
        return player.getScores().stream()
                .filter(s -> s.getDifficulty() == diff)
                .mapToInt(s -> s.getValue())
                .findFirst().orElse(0);
    }

    private int getReachedStageNumber(Difficulty diff) {
        return switch (diff) {
            case EASY -> 1; case MEDIUM -> 2; case HARD -> 3; case ENDLESS -> 3;
        };
    }

    private void showEndScreen(boolean win) {
        stopBackgroundMusic();
        centerPanel.removeAll();
        centerPanel.setLayout(new GridBagLayout());

        String bannerText = endlessSelected ? "❆ ENDLESS RUN OVER ❆" : (win ? "❆ VICTORY ❆" : "❆ DEFEAT ❆");

        // --- Banner ---
        JPanel bannerPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 140, 0), 0, getHeight(), new Color(180, 60, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(30, 30, 30));
                g2.setStroke(new BasicStroke(4));
                g2.drawLine(0, 2, getWidth(), 2);
                g2.drawLine(0, getHeight() - 3, getWidth(), getHeight() - 3);
                g2.setColor(new Color(220, 220, 220));
                g2.setFont(new Font("SansSerif", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(bannerText,
                        (getWidth() - fm.stringWidth(bannerText)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        bannerPanel.setOpaque(false);
        bannerPanel.setPreferredSize(new Dimension(0, 100));

        // --- Score label ---
        String endSummary = endlessSelected
                ? "Final Score: " + score + "  |  " + endlessMode.getStageLabel()
                : "Final Score: " + score + "  |  Difficulty: " + difficulty;

        JLabel finalScore = new JLabel(endSummary, SwingConstants.CENTER);
        finalScore.setFont(new Font("SansSerif", Font.BOLD, 20));
        finalScore.setForeground(TEXT_YELLOW);

        // --- Two properly sized buttons side by side ---
        JButton restart = createGameButton("Play Again");
        restart.setPreferredSize(new Dimension(200, 52));
        restart.addActionListener(e -> initializeGame());

        JButton exitBtn = createGameButton("Back to Level Select");
        exitBtn.setPreferredSize(new Dimension(240, 52));
        exitBtn.addActionListener(e -> screenManager.showScreen(ScreenManager.LEVEL_SELECT));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 0));
        btnRow.setOpaque(false);
        btnRow.add(restart);
        btnRow.add(exitBtn);

        // --- GridBagLayout so everything stacks and centers correctly ---
        GridBagConstraints egbc = new GridBagConstraints();
        egbc.gridx = 0; egbc.weightx = 1.0;

        // Row 0: banner fills full width at top
        egbc.gridy = 0; egbc.weighty = 0.0;
        egbc.fill = GridBagConstraints.HORIZONTAL;
        egbc.anchor = GridBagConstraints.NORTH;
        egbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(bannerPanel, egbc);

        // Row 1: score label centered
        egbc.gridy = 1; egbc.weighty = 0.0;
        egbc.fill = GridBagConstraints.NONE;
        egbc.anchor = GridBagConstraints.CENTER;
        egbc.insets = new Insets(36, 20, 16, 20);
        centerPanel.add(finalScore, egbc);

        // Row 2: button row centered, pushes to vertical middle
        egbc.gridy = 2; egbc.weighty = 1.0;
        egbc.anchor = GridBagConstraints.NORTH;
        egbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(btnRow, egbc);

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // -------------------------------------------------------------------------
    // Pause
    // -------------------------------------------------------------------------

    private void togglePause() {
        if (gameOver) return;

        if (paused) {
            resumeFromPause();
            return;
        }

        paused = true;
        pauseButton.setText("Resume");
        turnTimer.stop();
        if (matchTimer != null) matchTimer.stop();
        phaseLabel.setText("PAUSED");

        boolean exitChosen = showStyledPauseDialog();

        if (exitChosen) {
            paused = false;
            gameInProgress = false;
            stopBackgroundMusic();
            screenManager.showScreen(ScreenManager.LEVEL_SELECT);
        } else {
            resumeFromPause();
        }
    }

    private void resumeFromPause() {
        paused = false;
        pauseButton.setText("Pause");
        if (turnTimer  != null) turnTimer.start();
        if (matchTimer != null) matchTimer.start();
        updatePhaseLabel();
    }

    /**
     * Shows a fully custom dark-themed pause dialog with two styled buttons:
     * "Resume" (continue playing) and "Exit" (go back to level select).
     *
     * @return {@code true} if the player chose Exit, {@code false} if Resume
     */
    private boolean showStyledPauseDialog() {
        // result[0]: false = resume, true = exit
        boolean[] exitChosen = {false};

        // Build the dialog
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this), "Paused",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        // Outer panel with dark background and orange border
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 140, 0));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(32, 40, 28, 40));

        // Title
        JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(new Color(255, 160, 0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Subtitle
        JLabel sub = new JLabel("What would you like to do?", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sub.setForeground(new Color(190, 180, 160));
        sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        // Top text block
        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        textBlock.add(title);
        textBlock.add(sub);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 70, 60));
        sep.setBackground(PANEL_BG);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Resume button
        JButton resumeBtn = createGameButton("▶  Resume");
        resumeBtn.setPreferredSize(new Dimension(200, 52));
        resumeBtn.setMaximumSize(new Dimension(200, 52));
        resumeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumeBtn.addActionListener(e -> dialog.dispose());

        // Exit button  
        JButton exitBtn = createGameButton("✕  Exit to Level Select");
        exitBtn.setPreferredSize(new Dimension(240, 52));
        exitBtn.setMaximumSize(new Dimension(240, 52));
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.addActionListener(e -> {
            exitChosen[0] = true;
            dialog.dispose();
        });

        // Button panel
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        btnPanel.add(resumeBtn);
        btnPanel.add(Box.createVerticalStrut(14));
        btnPanel.add(exitBtn);

        // Assemble
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(textBlock);
        content.add(sep);
        content.add(btnPanel);

        outer.add(content, BorderLayout.CENTER);
        dialog.setContentPane(outer);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, 260));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); // blocks until dialog is disposed

        return exitChosen[0];
    }

    /**
     * Opens the SettingsPanel as a modal popup over the gameplay screen.
     * The game timers are paused while the dialog is open and resumed on close.
     */
    private void openSettingsPopup() {
        if (gameOver) return;

        // Pause timers while settings are open
        boolean wasAlreadyPaused = paused;
        if (!paused) {
            paused = true;
            if (turnTimer  != null) turnTimer.stop();
            if (matchTimer != null) matchTimer.stop();
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this), "Settings",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(false);
        dialog.setResizable(false);

        SettingsPanel settingsPanel = new SettingsPanel(screenManager);
        settingsPanel.setPreferredSize(new Dimension(820, 560));

        dialog.setContentPane(settingsPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Resume timers when dialog is closed (unless game was already paused)
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (!wasAlreadyPaused && !gameOver) {
                    paused = false;
                    if (turnTimer  != null) turnTimer.start();
                    if (matchTimer != null) matchTimer.start();
                }
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Audio
    // -------------------------------------------------------------------------

    private void startBackgroundMusic() {
        stopBackgroundMusic();
        backgroundMusicClip = createClip(BACKGROUND_MUSIC);
        if (backgroundMusicClip != null) {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
            backgroundMusicClip = null;
        }
    }

    @Override
    public void removeNotify() {
        UiPreferences.removeVolumeChangeListener(volumeRefreshListener);
        stopBackgroundMusic();
        super.removeNotify();
    }

    private void playEffect(String fileName) {
        new Thread(() -> {
            Clip clip = createClip(fileName);
            if (clip == null) return;
            clip.addLineListener(event -> { 
                if (event.getType() == LineEvent.Type.STOP) clip.close(); 
            });
            clip.start();
        }).start();
    }

    private Clip createClip(String fileName) {
        try (AudioInputStream stream = openAudioStream(fileName)) {
            if (stream == null) return null;
            
            // Convert to 16-bit if needed
            AudioInputStream convertedStream = convertTo16Bit(stream);
            
            Clip clip = AudioSystem.getClip();
            clip.open(convertedStream);
            applyVolume(clip);
            return clip;
        } catch (Exception ex) {
            System.err.println("Audio error for " + fileName + ": " + ex.getMessage());
            return null;
        }
    }

    private AudioInputStream convertTo16Bit(AudioInputStream sourceStream) throws Exception {
        AudioFormat sourceFormat = sourceStream.getFormat();
        if (sourceFormat.getSampleSizeInBits() == 16) {
            return sourceStream;
        }
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.getSampleRate(),
            16,
            sourceFormat.getChannels(),
            sourceFormat.getChannels() * 2,
            sourceFormat.getSampleRate(),
            false
        );
        return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
    }

    private AudioInputStream openAudioStream(String fileName) throws Exception {
        File f = new File("resources/sounds", fileName);
        if (f.exists()) return AudioSystem.getAudioInputStream(f);
        File f2 = new File("out/sounds", fileName);
        if (f2.exists()) return AudioSystem.getAudioInputStream(f2);
        var s = getClass().getResourceAsStream("/sounds/" + fileName);
        return s != null ? AudioSystem.getAudioInputStream(new BufferedInputStream(s)) : null;
    }

    private void applyVolume(Clip clip) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        int vol = UiPreferences.getVolumePercent();
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (vol <= 0) { gain.setValue(gain.getMinimum()); return; }
        float dB = (float)(20.0 * Math.log10(vol / 100.0));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
    }

    private void refreshAudioVolume() {
        if (backgroundMusicClip != null && backgroundMusicClip.isOpen()) {
            applyVolume(backgroundMusicClip);
        }
    }

    // -------------------------------------------------------------------------
    // UI factory helpers
    // -------------------------------------------------------------------------

    private JProgressBar createHPBar(String label, boolean isPlayer) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(100);
        bar.setString(label);
        bar.setStringPainted(true);
        bar.setFont(new Font("SansSerif", Font.BOLD, 16));
        bar.setForeground(isPlayer ? new Color(35, 230, 35) : new Color(255, 40, 40));
        bar.setBackground(new Color(65, 65, 80));
        bar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        bar.setPreferredSize(new Dimension(420, 24));
        bar.setUI(new StyledHpBarUI());
        return bar;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(TEXT_YELLOW);
        return label;
    }

    private JButton createGameButton(String text) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                int w = getWidth(), h = getHeight(), cut = 12;
                int[] xp = {cut, w-cut, w, w, w-cut, cut, 0, 0};
                int[] yp = {0, 0, cut, h-cut, h, h, h-cut, cut};
                g2.setColor(new Color(10, 10, 10));
                g2.fillPolygon(new Polygon(xp, yp, 8));
                int[] xi = {cut+3, w-cut-3, w-3, w-3, w-cut-3, cut+3, 3, 3};
                int[] yi = {3, 3, cut+3, h-cut-3, h-3, h-3, h-cut-3, cut+3};
                g2.setPaint(new GradientPaint(0, 0, new Color(90,90,90), 0, h, new Color(50,50,50)));
                g2.fillPolygon(new Polygon(xi, yi, 8));
                g2.setColor(new Color(130, 130, 130));
                g2.drawLine(cut+4, 5, w-cut-4, 5);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(LIGHT_TEXT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setForeground(new Color(255, 220, 120)); }
            @Override public void mouseExited(MouseEvent e)  { button.setForeground(LIGHT_TEXT); }
        });
        return button;
    }

    // -------------------------------------------------------------------------
    // Background painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (gameplayBackground != null && centerPanel != null) {
            g.drawImage(gameplayBackground,
                    centerPanel.getX(), centerPanel.getY(),
                    centerPanel.getWidth(), centerPanel.getHeight(), this);
        }
    }

    // -------------------------------------------------------------------------
    // Inner UI classes
    // -------------------------------------------------------------------------

    static class StyledHpBarUI extends BasicProgressBarUI {
        @Override protected void paintDeterminate(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = progressBar.getWidth(), h = progressBar.getHeight();
            g2.setColor(progressBar.getBackground()); g2.fillRect(0, 0, w, h);
            g2.setColor(progressBar.getForeground()); g2.fillRect(0, 0, getAmountFull(null, w, h), h);
            g2.setColor(Color.WHITE); g2.drawRect(0, 0, w - 1, h - 1);
            if (progressBar.isStringPainted()) {
                String txt = progressBar.getString();
                g2.setFont(progressBar.getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(txt, (w - fm.stringWidth(txt)) / 2,
                        (h + fm.getAscent() - fm.getDescent()) / 2 - 1);
            }
            g2.dispose();
        }
    }

    static class VerticalTimerUI extends BasicProgressBarUI {
        @Override protected void paintDeterminate(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = c.getWidth(), h = c.getHeight();
            int fill = (int)(w * progressBar.getPercentComplete());
            g2.setColor(c.getBackground()); g2.fillRect(0, 0, w, h);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 190, 60), w, 0, new Color(255, 120, 0)));
            g2.fillRect(0, 0, fill, h);
            g2.setColor(new Color(25, 25, 25)); g2.drawRect(0, 0, w - 1, h - 1);
            g2.dispose();
        }
    }

    private ImageIcon flipIconHorizontally(ImageIcon icon) {
        if (icon == null) return null;
        java.awt.Image img = icon.getImage();
        java.awt.image.BufferedImage buffered = new java.awt.image.BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        java.awt.Graphics2D g2 = buffered.createGraphics();
        g2.drawImage(img, img.getWidth(null), 0, -img.getWidth(null), img.getHeight(null), null);
        g2.dispose();
        return new ImageIcon(buffered);
    }
}
