package frontend;

import backend.logic.AuthService;
import backend.logic.GuardianService;
import backend.model.AccountType;
import java.awt.*;
import javax.swing.*;

/**
 * GuardianLoginPanel provides the protected login flow for parent and teacher
 * accounts. It validates guardian credentials and guardian code information
 * before opening the guardian management screen.
 */
public class GuardianLoginPanel extends BaseScreenPanel {
    public GuardianLoginPanel(ScreenManager screenManager, AuthService authService,
            GuardianService guardianService) {
        super("Guardian Login");

        JPanel frame = createMockFrame();
        JPanel form = createInnerBox();
        GridBagConstraints gbc = createGbc();

        JTextField usernameField = createStyledField(16);
        JPasswordField passwordField = createStyledPasswordField(16);
        JPasswordField guardianCodeField = createStyledPasswordField(16);
        JLabel statusLabel = createBodyLabel(" ", 18, SwingConstants.CENTER);
        statusLabel.setForeground(new Color(255, 100, 100));

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Username:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(usernameField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Password:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(passwordField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Guardian Code:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(guardianCodeField, gbc); row++;

        // Back and Login buttons side by side
        JButton backButton = createMenuButton("[ Back ]");
        JButton loginButton = createMenuButton("[ Login ]");

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(backButton);
        buttonRow.add(loginButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(buttonRow, gbc); row++;

        gbc.gridy = row;
        form.add(statusLabel, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String code = new String(guardianCodeField.getPassword()).trim();

            if (code.isEmpty()) {
                statusLabel.setText("Guardian code is required.");
                return;
            }

            boolean success = authService.login(username, password);
            if (success && authService.getCurrentPlayer() != null &&
                authService.getCurrentPlayer().getAccountType() == AccountType.PARENT_TEACHER &&
                guardianService.verifyPin(authService.getCurrentPlayer(), code)) {
                screenManager.showScreen(ScreenManager.GUARDIAN_CONTROLS);
            } else if (success && authService.getCurrentPlayer() != null &&
                    authService.getCurrentPlayer().getAccountType() == AccountType.PARENT_TEACHER) {
                authService.logout();
                statusLabel.setText("Invalid guardian code.");
            } else {
                authService.logout();
                statusLabel.setText("Access denied. Guardian authorization required.");
            }
        });

        backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.MAIN_MENU));

        frame.add(form, BorderLayout.CENTER);
        add(frame, BorderLayout.CENTER);
    }
}
