package frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import frontend.ScreenManager;

/**
 * LogoutConfirmPanel asks the player to confirm that they want to end the
 * current session. It provides a safe way to cancel logout and return to the
 * player dashboard without leaving the account.
 */
public class LogoutConfirmPanel extends BaseScreenPanel {
    public LogoutConfirmPanel(ScreenManager screenManager) {
        super("");
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel frame = createMockFrame();
        frame.setLayout(new BorderLayout(20, 20));

        ImageIcon logo = UiPreferences.loadIcon("images/TypingDuelLogo.png");
        if (logo != null) {
            JLabel logoLabel = new JLabel(logo);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            frame.add(logoLabel, BorderLayout.NORTH);
        }

        JPanel inner = createInnerBox();
        inner.setLayout(new BorderLayout(20, 20));

        JLabel prompt = createBodyLabel("Logout of Your Typing Duel Account?", 34, SwingConstants.CENTER);
        JPanel promptWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        promptWrapper.setOpaque(false);
        promptWrapper.add(prompt);
        inner.add(promptWrapper, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton yesButton = createMenuButton("Yes");
        JButton noButton = createMenuButton("No");
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        inner.add(buttonPanel, BorderLayout.CENTER);

        yesButton.addActionListener(event -> screenManager.showScreen(ScreenManager.MAIN_MENU));
        noButton.addActionListener(event -> screenManager.showScreen(ScreenManager.PLAYER_MENU));

        frame.add(inner, BorderLayout.CENTER);
        add(frame, BorderLayout.CENTER);
    }
}
