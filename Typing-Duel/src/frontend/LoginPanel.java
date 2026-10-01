package frontend;

import backend.logic.AuthService;
import backend.logic.GuardianService;
import backend.model.AccountType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * LoginPanel provides the main authentication screen for Typing Duel.
 * It supports player and guardian login, account creation navigation,
 * password recovery entry points, and returning to the main menu.
 */
public class LoginPanel extends BaseScreenPanel {
    public LoginPanel(ScreenManager screenManager, AuthService authService,
            GuardianService guardianService) {
        super("Welcome to Typing Duel!");

        JPanel frame = createMockFrame();
        // Keep the form centered so the login flow stays readable on larger windows.
        JPanel form = createInnerBox();
        GridBagConstraints gbc = createGbc();

        JTextField usernameField = createStyledField(16);
        JPasswordField passwordField = createStyledPasswordField(16);
        JLabel statusLabel = createBodyLabel(" ", 18, SwingConstants.CENTER);
        JLabel guardianLabel = createBodyLabel("Parent / Teacher account detected", 18, SwingConstants.CENTER);
        JLabel guardianHelper = createBodyLabel("Enter the guardian code below to continue.", 14, SwingConstants.CENTER);
        JTextField guardianCodeField = new JTextField(14);
        guardianLabel.setVisible(false);
        guardianHelper.setVisible(false);
        guardianCodeField.setVisible(false);
        guardianCodeField.setBorder(BorderFactory.createLineBorder(BTN_BORDER, 1));

        JPanel guardianPanel = new JPanel(new BorderLayout(8, 8));
        guardianPanel.setOpaque(false);
        guardianPanel.add(guardianLabel, BorderLayout.NORTH);
        guardianPanel.add(guardianCodeField, BorderLayout.CENTER);
        guardianPanel.add(guardianHelper, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(createBodyLabel("Username:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(createBodyLabel("Password:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(guardianPanel, gbc);

        javax.swing.JButton loginButton = createMenuButton("Login");
        javax.swing.JButton backButton = createMenuButton("Back");
        JLabel registerLink = createBodyLabel("Don't have an account? Create Account", 16, SwingConstants.CENTER);
        JLabel forgotPasswordLink = createBodyLabel("Did you forget password?", 16, SwingConstants.CENTER);
        registerLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registerLink.setText("<html><u>Don't have an account? Create Account</u></html>");
        forgotPasswordLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        forgotPasswordLink.setText("<html><u>Did you forget password?</u></html>");

        Runnable resetForm = () -> {
            // Clear sensitive values whenever the panel is shown again.
            usernameField.setText("");
            passwordField.setText("");
            guardianCodeField.setText("");
            guardianLabel.setVisible(false);
            guardianHelper.setVisible(false);
            guardianCodeField.setVisible(false);
            loginButton.setText("Login");
            statusLabel.setText(" ");
        };

        gbc.gridy = 3;
        form.add(loginButton, gbc);

        gbc.gridy = 4;
        form.add(registerLink, gbc);

        gbc.gridy = 5;
        form.add(forgotPasswordLink, gbc);

        gbc.gridy = 6;
        form.add(statusLabel, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (guardianLabel.isVisible()) {
                String guardianCode = guardianCodeField.getText().trim();
                if (guardianService.verifyPin(authService.getCurrentPlayer(), guardianCode)) {
                    statusLabel.setText("Guardian code accepted. Logging in...");
                    screenManager.showScreen(ScreenManager.PLAYER_MENU);
                } else {
                    statusLabel.setText("Invalid guardian code.");
                }
                return;
            }

            boolean success = authService.login(username, password);
            if (success) {
                if (authService.getCurrentPlayer() != null
                        && authService.getCurrentPlayer().getAccountType() == AccountType.PARENT_TEACHER) {
                    guardianLabel.setVisible(true);
                    guardianCodeField.setVisible(true);
                    guardianHelper.setVisible(true);
                    loginButton.setText("Verify Code");
                    statusLabel.setText("Guardian approval required.");
                    revalidate();
                    repaint();
                    return;
                }

                statusLabel.setText("Logging in...");
                screenManager.showScreen(ScreenManager.PLAYER_MENU);
            } else {
                statusLabel.setText("Invalid login. Check username/password.");
            }
        });

        backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.MAIN_MENU));
        // Keyboard shortcuts support faster login and accessible navigation.
        bindButtonShortcut("ENTER", loginButton);
        bindButtonShortcut("ESCAPE", backButton);

        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                screenManager.showScreen(ScreenManager.REGISTER);
            }
        });

        forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                screenManager.showScreen(ScreenManager.FORGOT_PASSWORD);
            }
        });

        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                resetForm.run();
            }
        });

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setOpaque(false);
        bottomBar.add(backButton);

        frame.add(form, BorderLayout.CENTER);
        frame.add(bottomBar, BorderLayout.SOUTH);
        add(frame, BorderLayout.CENTER);
    }
}
