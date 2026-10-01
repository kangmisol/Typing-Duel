package frontend;

import backend.logic.AuthService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.HierarchyEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

/**
 * ForgotPasswordPanel handles the password recovery workflow for existing users.
 * It guides the player through username verification, security-question
 * validation, and new-password entry before returning to the login screen.
 */
public class ForgotPasswordPanel extends BaseScreenPanel {
    public ForgotPasswordPanel(ScreenManager screenManager, AuthService authService) {
        super("Forgot Password");

        JPanel frame = createMockFrame();
        JPanel form = createInnerBox();
        GridBagConstraints gbc = createGbc();

        JTextField usernameField = createStyledField(16);
        JTextField answerField = createStyledField(16);
        JPasswordField newPasswordField = createStyledPasswordField(16);

        JLabel usernameLabel = createBodyLabel("Username:", 20, SwingConstants.CENTER);
        JLabel questionLabel = createBodyLabel("Security Question:", 20, SwingConstants.CENTER);
        JLabel questionValueLabel = createBodyLabel(" ", 18, SwingConstants.CENTER);
        JLabel statusLabel = createBodyLabel("Enter your username to begin.", 16, SwingConstants.CENTER);

        questionLabel.setVisible(false);
        questionValueLabel.setVisible(false);
        answerField.setVisible(false);
        newPasswordField.setVisible(false);

        JLabel answerLabel = createBodyLabel("Security Answer:", 20, SwingConstants.CENTER);
        JLabel newPasswordLabel = createBodyLabel("Enter New Password:", 20, SwingConstants.CENTER);
        answerLabel.setVisible(false);
        newPasswordLabel.setVisible(false);

        JButton nextButton = createMenuButton("Next");
        JButton backButton = createMenuButton("Back");

        // These holders preserve recovery state as the user moves through each step.
        final String[] activeQuestion = {null};
        final String[] activeUsername = {null};
        final int[] step = {0};

        Runnable resetForm = () -> {
            // Reset the panel so previously entered recovery data does not persist.
            usernameField.setText("");
            answerField.setText("");
            newPasswordField.setText("");
            questionValueLabel.setText(" ");
            statusLabel.setText("Enter your username to begin.");
            usernameLabel.setVisible(true);
            usernameField.setVisible(true);
            questionLabel.setVisible(false);
            questionValueLabel.setVisible(false);
            answerLabel.setVisible(false);
            answerField.setVisible(false);
            newPasswordLabel.setVisible(false);
            newPasswordField.setVisible(false);
            nextButton.setVisible(true);
            activeQuestion[0] = null;
            activeUsername[0] = null;
            step[0] = 0;
        };

        Runnable submitNewPassword = () -> {
            // Finalize the recovery flow by replacing the stored password.
            String newPassword = new String(newPasswordField.getPassword()).trim();
            if (newPassword.isEmpty()) {
                statusLabel.setText("Please enter a new password.");
                return;
            }

            boolean updated = authService.recoverPassword(activeUsername[0], newPassword);
            if (updated) {
                statusLabel.setText("Password updated. Returning to login...");
                screenManager.showScreen(ScreenManager.LOGIN);
            } else {
                statusLabel.setText("Unable to update password.");
            }
        };

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(usernameLabel, gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(questionLabel, gbc);
        gbc.gridx = 1;
        form.add(questionValueLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(answerLabel, gbc);
        gbc.gridx = 1;
        form.add(answerField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(newPasswordLabel, gbc);
        gbc.gridx = 1;
        form.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(nextButton, gbc);

        gbc.gridy = 5;
        form.add(statusLabel, gbc);

        nextButton.addActionListener(event -> {
            if (step[0] == 0) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    statusLabel.setText("Please enter a username.");
                    return;
                }

                String[] questions = authService.getQuestionsForUser(username);
                if (questions == null || questions.length == 0 || questions[0] == null || questions[0].isBlank()) {
                    statusLabel.setText("That username does not exist.");
                    return;
                }

                activeUsername[0] = username;
                activeQuestion[0] = questions[0];
                questionLabel.setVisible(true);
                questionValueLabel.setText(questions[0]);
                questionValueLabel.setVisible(true);
                answerLabel.setVisible(true);
                answerField.setVisible(true);
                statusLabel.setText("Answer the security question.");
                step[0] = 1;
                revalidate();
                repaint();
                return;
            }

            if (step[0] == 1) {
                String answer = answerField.getText().trim();
                if (answer.isEmpty()) {
                    statusLabel.setText("Please enter your security answer.");
                    return;
                }

                boolean correct = authService.verifySecurityAnswers(activeUsername[0], new String[]{answer});
                if (!correct) {
                    statusLabel.setText("Incorrect answer. Please try again.");
                    return;
                }

                usernameLabel.setVisible(false);
                usernameField.setVisible(false);
                questionLabel.setVisible(false);
                questionValueLabel.setVisible(false);
                answerLabel.setVisible(false);
                answerField.setVisible(false);
                newPasswordLabel.setVisible(true);
                newPasswordField.setVisible(true);
                nextButton.setVisible(true);
                statusLabel.setText("Enter your new password.");
                step[0] = 2;
                revalidate();
                repaint();
                return;
            }

            submitNewPassword.run();
        });
        newPasswordField.addActionListener(event -> submitNewPassword.run());

        backButton.addActionListener(event -> screenManager.showScreen(ScreenManager.LOGIN));

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
