package frontend;

import java.awt.*;
import javax.swing.*;

/**
 * MainMenuPanel is the public landing screen of Typing Duel.
 * It presents the title artwork and the main navigation options that lead to
 * login, guardian login, or application exit.
 */
public class MainMenuPanel extends BaseScreenPanel {
    public MainMenuPanel(ScreenManager screenManager) {
        super("");

        setLayout(new BorderLayout());
        removeAll();

        JPanel frame = new BackgroundPanel("/images/backgrounds/typingDuelBackground.jpg");
        frame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = createGbc();
        gbc.insets = new Insets(10, 10, 10, 10);

        java.net.URL logoUrl = getClass().getResource("/images/TypingDuelLogo.png");
        JLabel title;
        if (logoUrl != null) {
            ImageIcon icon = new ImageIcon(logoUrl);
            title = new JLabel(icon);
        } else {
            title = new JLabel("TYPING DUEL", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 36));
            title.setForeground(new Color(255, 220, 80));
        }
        title.setPreferredSize(new Dimension(300, 225));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton loginButton    = createMenuButton("Login");
        JButton guardianButton = createMenuButton("Guardian Login");
        JButton exitButton     = createMenuButton("Exit");

        loginButton.addActionListener(e -> screenManager.showScreen(ScreenManager.LOGIN));
        guardianButton.addActionListener(e -> screenManager.showScreen(ScreenManager.GUARDIAN_LOGIN));
        exitButton.addActionListener(e -> screenManager.showScreen(ScreenManager.EXIT_CONFIRM));

        bindButtonShortcut("1", loginButton);
        bindButtonShortcut("2", guardianButton);
        bindButtonShortcut("ESCAPE", exitButton);
        bindButtonShortcut("ENTER", loginButton);

        gbc.gridx = 0; gbc.gridy = 0; frame.add(title, gbc);
        gbc.gridy++;    frame.add(loginButton, gbc);
        gbc.gridy++;    frame.add(guardianButton, gbc);
        gbc.gridy++;    frame.add(exitButton, gbc);

        add(frame, BorderLayout.CENTER);
    }
}

/**
 * BackgroundPanel paints a scaled background image behind a menu screen.
 */
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        java.net.URL location = getClass().getResource(imagePath);
        if (location == null) {
            System.err.println("WARNING: image not found at " + imagePath);
            backgroundImage = null;
        } else {
            backgroundImage = new ImageIcon(location).getImage();
        }
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
