package frontend;

import backend.logic.AuthService;
import backend.logic.GuardianService;
import backend.model.Player;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
/**
 * PlayerMenuPanel displays the main dashboard for a logged-in player.
 * It provides navigation to gameplay, tutorial, scores, settings, logout,
 * and character selection before the player enters a match.
 */
public class PlayerMenuPanel extends BaseScreenPanel {
    private final AuthService authService;
    private final GuardianService guardianService;
    private JLabel nameLabel;
    private JLabel imageLabel;
    private String selectedCharacter;

    private final Map<String, String> characterSprites = new LinkedHashMap<>();

    public PlayerMenuPanel(ScreenManager screenManager, AuthService authService, GuardianService guardianService) {
        super("Player Screen");
        this.authService = authService;
        this.guardianService = guardianService;

        // Map character names to their preview sprites for the selector panel.
        characterSprites.put("The Knight", "images/sprites/charDFNDchargeup.png");
        characterSprites.put("The Gilded Warrior", "images/sprites/charDFNDchargeup_BlackGold.png");
        characterSprites.put("The Knave", "images/sprites/charDFNDchargeup_BlackRed.png");

        selectedCharacter = "The Knight"; // default
        screenManager.setSelectedCharacter(selectedCharacter); // 🔥 store default

        JPanel root = new JPanel(new BorderLayout(24, 24));
        root.setOpaque(false);

        JPanel shell = createMockFrame();
        shell.setLayout(new BorderLayout(24, 24));

        // ---------------- Menu buttons ----------------
        JPanel menu = new JPanel(new GridLayout(0, 1, 12, 12));
        menu.setOpaque(false);
        menu.setPreferredSize(new Dimension(240, 0));

        JButton playButton = createMenuButton("PLAY");
        JButton tutorialButton = createMenuButton("TUTORIAL");
        JButton statsLeaderboardButton = createMenuButton("PLAYER STATS & LEADERBOARD");
        JButton settingsButton = createMenuButton("SETTINGS");
        JButton logoutButton = createMenuButton("LOGOUT");

        playButton.addActionListener(event -> handlePlay(screenManager));
        tutorialButton.addActionListener(event -> screenManager.showScreen(ScreenManager.TUTORIAL));
        statsLeaderboardButton.addActionListener(event -> {
            screenManager.registerScreen(ScreenManager.HIGH_SCORES, new HighScorePanel(screenManager, authService));
            screenManager.showScreen(ScreenManager.HIGH_SCORES);
        });
        settingsButton.addActionListener(event -> screenManager.showScreen(ScreenManager.SETTINGS));
        logoutButton.addActionListener(event -> screenManager.showScreen(ScreenManager.LOGOUT_CONFIRM));

        // Shortcuts
        bindButtonShortcut("1", playButton);
        bindButtonShortcut("2", tutorialButton);
        bindButtonShortcut("3", statsLeaderboardButton);
        bindButtonShortcut("4", settingsButton);
        bindButtonShortcut("5", logoutButton);
        bindButtonShortcut("ENTER", playButton);
        bindButtonShortcut("ESCAPE", logoutButton);

        menu.add(playButton);
        menu.add(tutorialButton);
        menu.add(statsLeaderboardButton);
        menu.add(settingsButton);
        menu.add(logoutButton);

        // ---------------- Preview panel ----------------
        JPanel preview = new JPanel(new BorderLayout());
        preview.setOpaque(true);
        preview.setBackground(BACKGROUND);
        preview.setBorder(OUTLINE);

        Player currentUser = authService.getCurrentPlayer();
        String username = (currentUser == null) ? "Guest" : currentUser.getUsername();

        Font labelFont = new Font("Tektur", Font.PLAIN, 16);
        Font usernameFont = new Font("Tektur", Font.BOLD, 24);

        this.nameLabel = new JLabel(username, SwingConstants.CENTER);
        this.nameLabel.setForeground(ACCENT);
        this.nameLabel.setFont(usernameFont);

        // ---------------- Character selection ----------------
        JPanel charactersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 12));
        charactersPanel.setOpaque(false);

        for (String characterName : characterSprites.keySet()) {
            JPanel charPanel = new JPanel();
            charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
            charPanel.setOpaque(false);

            JLabel namePlate = new JLabel(characterName, SwingConstants.CENTER);
            namePlate.setFont(labelFont);
            namePlate.setForeground(ACCENT);
            namePlate.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel charImageLabel = new JLabel();
            charImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            java.net.URL url = getClass().getClassLoader().getResource(characterSprites.get(characterName));
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                charImageLabel.setIcon(new ImageIcon(scaled));
            } else {
                charImageLabel.setText("[Image Missing]");
                charImageLabel.setForeground(ACCENT);
            }

            charImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton selectButton = new JButton("Select");
            selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            selectButton.setFont(labelFont);
            selectButton.setForeground(ACCENT);

            //store selection globally
            selectButton.addActionListener(e -> {
                selectedCharacter = characterName;
                screenManager.setSelectedCharacter(characterName); // 🔥 key line
                updateCharacterSprite(selectedCharacter);
            });

            charPanel.add(namePlate);
            charPanel.add(Box.createVerticalStrut(8));
            charPanel.add(charImageLabel);
            charPanel.add(Box.createVerticalStrut(8));
            charPanel.add(selectButton);

            charactersPanel.add(charPanel);
        }

        // ---------------- Large preview sprite ----------------
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateCharacterSprite(selectedCharacter);

        JPanel previewContent = new JPanel();
        previewContent.setLayout(new BoxLayout(previewContent, BoxLayout.Y_AXIS));
        previewContent.setOpaque(false);

        charactersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewContent.add(charactersPanel);
        previewContent.add(Box.createVerticalStrut(16));
        previewContent.add(imageLabel);
        previewContent.add(Box.createVerticalStrut(12));
        previewContent.add(nameLabel);

        preview.add(previewContent, BorderLayout.CENTER);

        shell.add(menu, BorderLayout.WEST);
        shell.add(preview, BorderLayout.CENTER);
        root.add(shell, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);

        // Update the name label every time this screen becomes visible
        // This is the key fix — fires on every navigation to this screen
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                updateNameLabel();
            }
        });
    }

    /** Refreshes the displayed username from the current logged-in player. */
    private void updateNameLabel() {
        if (nameLabel == null) return;
        Player currentUser = authService.getCurrentPlayer();
        String username = (currentUser == null) ? "Guest" : currentUser.getUsername();
        nameLabel.setText(username);
        nameLabel.setForeground(BTN_BORDER);
    }

    private void handlePlay(ScreenManager screenManager) {
        Player currentUser = authService.getCurrentPlayer();
        String username = (currentUser == null) ? "Guest" : currentUser.getUsername();

        // Always persist the current character selection before leaving the menu.
        screenManager.setSelectedCharacter(selectedCharacter);

        if (currentUser == null) {
            screenManager.showScreen(ScreenManager.LEVEL_SELECT);
            return;
        }

        // The first dialog lets the player decide whether to keep or reset progress.
        Boolean firstChoice = showStyledDecisionDialog(
                "NEW GAME",
                "Would you like to continue?",
                "<html><div style='text-align:center; width:320px;'><b>IMPORTANT WARNING:</b> Starting a New Game will RESET player's scores, high scores, typing statistics, and unlocked stages.<br/><br/>Only <b>EASY</b> will remain unlocked.</div></html>",
                "Start New Game",
                "Keep Progress"
        );

        if (Boolean.FALSE.equals(firstChoice)) {
            screenManager.showScreen(ScreenManager.LEVEL_SELECT);
            return;
        }

        if (!Boolean.TRUE.equals(firstChoice)) return;

        // Use a second confirmation because this action clears saved player progress.
        Boolean secondChoice = showStyledDecisionDialog(
                "ARE YOU SURE?",
                "This action cannot be undone.",
                "<html><div style='text-align:center; width:300px;'>Reset all progress for <b>" + username + "</b> and start from the beginning?</div></html>",
                "✔  Yes, Reset",
                "✕  Cancel"
        );

        if (Boolean.TRUE.equals(secondChoice)) {
            guardianService.resetPlayerProgress(username);
            screenManager.showScreen(ScreenManager.LEVEL_SELECT);
        }
    }

    private void updateCharacterSprite(String character) {
        String path = characterSprites.getOrDefault(character, characterSprites.get("The Knight"));
        java.net.URL imgUrl = getClass().getClassLoader().getResource(path);

        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
            imageLabel.setText(null);
        } else {
            imageLabel.setText("[ Image Missing ]");
            imageLabel.setIcon(null);
            imageLabel.setForeground(ACCENT);
        }
    }

    private Boolean showStyledDecisionDialog(
            String titleText,
            String subtitleText,
            String bodyHtml,
            String primaryText,
            String secondaryText
    ) {
        final Boolean[] result = {null};

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                titleText,
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

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

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setForeground(ACCENT);
        title.setFont(BITCOUNT.deriveFont(34f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText, SwingConstants.CENTER);
        subtitle.setForeground(new Color(220, 210, 190));
        subtitle.setFont(TEKTUR.deriveFont(16f));
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel body = new JLabel(bodyHtml, SwingConstants.CENTER);
        body.setForeground(new Color(245, 235, 210));
        body.setFont(TEKTUR.deriveFont(14f));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton primary = createMenuButton(primaryText);
        primary.setAlignmentX(Component.CENTER_ALIGNMENT);
        primary.addActionListener(e -> {
            result[0] = Boolean.TRUE;
            dialog.dispose();
        });

        JButton secondary = createMenuButton(secondaryText);
        secondary.setAlignmentX(Component.CENTER_ALIGNMENT);
        secondary.addActionListener(e -> {
            result[0] = Boolean.FALSE;
            dialog.dispose();
        });

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(title);
        content.add(subtitle);
        content.add(body);
        content.add(primary);
        content.add(Box.createVerticalStrut(14));
        content.add(secondary);

        outer.add(content, BorderLayout.CENTER);
        dialog.setContentPane(outer);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(560, 360));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateNameLabel();
    }
}
