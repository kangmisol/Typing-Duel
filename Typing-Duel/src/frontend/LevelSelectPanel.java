package frontend;

import backend.logic.AuthService;
import backend.logic.ProgressionService;
import backend.model.Difficulty;
import backend.model.Player;
import java.awt.*;
import javax.swing.*;

/**
 * LevelSelectPanel lets the player choose which gameplay mode or difficulty to
 * enter. It updates button availability based on saved progression data so
 * locked stages remain inaccessible until they are earned.
 */
public class LevelSelectPanel extends BaseScreenPanel implements ScreenManager.ScreenAware {

    private final ScreenManager screenManager;
    private final ProgressionService progressionService;
    private final AuthService authService;

    private JButton easyButton;
    private JButton mediumButton;
    private JButton hardButton;
    private JButton endlessButton;
    private ImageIcon playerAttackChargeIcon;

    public LevelSelectPanel(ScreenManager screenManager, ProgressionService progressionService, AuthService authService) {
        super("SELECT A LEVEL");
        this.screenManager      = screenManager;
        this.progressionService = progressionService;
        this.authService        = authService;

        loadSpriteIcon();

        JPanel shell = createMockFrame(); // Errors 3,4,5: shell declaration
        shell.setLayout(new BorderLayout(24, 24));

        // --- LEVEL BUTTONS (2x2 Grid) ---
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 20, 20)); // 2x2 Grid with gaps
        buttonGrid.setOpaque(false);
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        easyButton    = createMenuButton("EASY");
        mediumButton  = createMenuButton("MEDIUM");
        hardButton    = createMenuButton("HARD");
        endlessButton = createMenuButton("ENDLESS MODE");

        Font stageFont = easyButton.getFont().deriveFont(22f);
        for (JButton btn : new JButton[]{easyButton, mediumButton, hardButton, endlessButton}) {
            btn.setFont(stageFont);
            btn.setPreferredSize(new Dimension(300, 120));
        }

        easyButton.addActionListener(e -> startGame("EASY"));
        mediumButton.addActionListener(e -> startGame("MEDIUM"));
        hardButton.addActionListener(e -> startGame("HARD"));
        endlessButton.addActionListener(e -> showEndlessPopup());

        buttonGrid.add(easyButton);
        buttonGrid.add(mediumButton);
        buttonGrid.add(hardButton);
        buttonGrid.add(endlessButton);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setOpaque(false);
        JButton backButton = createMenuButton("Back");
        backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.PLAYER_MENU));
        bottomBar.add(backButton);

        JPanel mockFrame = createMockFrame();
        mockFrame.setLayout(new BorderLayout(10, 10));
        mockFrame.add(buttonGrid, BorderLayout.CENTER);
        mockFrame.add(bottomBar, BorderLayout.SOUTH);
        add(mockFrame, BorderLayout.CENTER);

        refreshStageButtons();
    }

    @Override
    public void onScreenShown() {
        refreshStageButtons(); // Refresh button states when screen becomes visible
    }

    private void loadSpriteIcon() {
        playerAttackChargeIcon = UiPreferences.loadIcon(UiPreferences.CHAR_ATK_CHARGEUP);
    }

    private void startGame(String difficulty) {
        screenManager.registerScreen(
            ScreenManager.STANDARD_GAMEPLAY,
            new StandardGameplayPanel(screenManager, false, difficulty, authService)
        );
        screenManager.showScreen(ScreenManager.STANDARD_GAMEPLAY);
    }

    private void refreshStageButtons() {
        Player currentPlayer = authService.getCurrentPlayer();
        applyStageButtonState(easyButton,    "EASY",        false);
        applyStageButtonState(mediumButton,  "MEDIUM",
            !progressionService.isDifficultyUnlocked(currentPlayer, Difficulty.MEDIUM));
        applyStageButtonState(hardButton,    "HARD",
            !progressionService.isDifficultyUnlocked(currentPlayer, Difficulty.HARD));
        applyStageButtonState(endlessButton, "ENDLESS MODE",
            !progressionService.isEndlessUnlocked(currentPlayer));
    }

    private void applyStageButtonState(JButton button, String label, boolean locked) {
        button.setText("<html><center>" + label + (locked ? "<br/>LOCKED" : "") + "</center></html>");
        button.setEnabled(!locked);
        button.setForeground(!locked ? BTN_BORDER : new Color(245, 235, 210));
        button.repaint();
    }

    private void showEndlessPopup() {
        JDialog dialog = new JDialog((Frame) null, "Endless Mode", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(18, 18, 28));

        JLabel info = new JLabel(
            "<html><center><b>Welcome to Endless Mode!</b><br><br>" +
            "Your opponent has infinite health!<br>" +
            "Survive as long as possible and rack up points.<br>" +
            "The opponent gets stronger as you progress.<br><br>" +
            "Good Luck!</center></html>", SwingConstants.CENTER);
        info.setForeground(new Color(245, 235, 210));
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        btnPanel.setOpaque(false);

        JButton beginBtn  = new JButton("Begin Endless Run");
        JButton returnBtn = new JButton("Return to Level Select");

        for (JButton b : new JButton[]{beginBtn, returnBtn}) {
            b.setBackground(new Color(60, 60, 80));
            b.setForeground(new Color(245, 235, 210));
            b.setFocusPainted(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 13));
            b.setBorder(BorderFactory.createLineBorder(new Color(200, 140, 30), 1));
        }

        beginBtn.addActionListener(e -> {
            dialog.dispose();
            screenManager.registerScreen(ScreenManager.STANDARD_GAMEPLAY,
                new StandardGameplayPanel(screenManager, true, "EASY", authService));
            screenManager.showScreen(ScreenManager.STANDARD_GAMEPLAY);
        });
        returnBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(beginBtn);
        btnPanel.add(returnBtn);
        dialog.add(info, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
