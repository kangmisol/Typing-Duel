// package frontend;

// import backend.logic.AuthService;
// import java.awt.*;
// import javax.swing.*;

// public class RegisterPanel extends BaseScreenPanel {
//     public RegisterPanel(ScreenManager screenManager, AuthService authService) {
//         super("Create Account");

//         JPanel frame = createMockFrame();
//         JPanel form = createInnerBox();
//         GridBagConstraints gbc = createGbc();

//         JTextField usernameField = createStyledField(16);
//         JPasswordField passwordField = createStyledPasswordField(16);

//         String[] questions = {
//             "What city were you born in?",
//             "What is your mother's maiden name?",
//             "What was the name of your first pet?",
//             "What was your childhood nickname?"
//         };
//         JComboBox<String> securityQuestion = new JComboBox<>(questions);
//         JTextField securityAnswer = new JTextField(16);

//         // Age question
//         JLabel ageLabel = createBodyLabel("Are you below the age of 16?", 20, SwingConstants.CENTER);
//         JButton yesButton = createMenuButton("Yes");
//         JButton noButton = createMenuButton("No");

//         // Guardian code (hidden by default)
//         JLabel guardianCodeLabel = createBodyLabel("Guardian Code:", 20, SwingConstants.CENTER);
//         JTextField guardianCodeField = new JTextField(16);
//         guardianCodeLabel.setVisible(false);
//         guardianCodeField.setVisible(false);

//         JLabel statusLabel = createBodyLabel(" ", 16, SwingConstants.CENTER);
//         JButton registerButton = createMenuButton("[ Create new Account ]");
//         JButton backButton = createMenuButton("[ Back ]");

//         int row = 0;

//         gbc.gridx = 0; gbc.gridy = row;
//         form.add(createBodyLabel("Username:", 20, SwingConstants.CENTER), gbc);
//         gbc.gridx = 1; form.add(usernameField, gbc); row++;

//         gbc.gridx = 0; gbc.gridy = row;
//         form.add(createBodyLabel("Password:", 20, SwingConstants.CENTER), gbc);
//         gbc.gridx = 1; form.add(passwordField, gbc); row++;

//         gbc.gridx = 0; gbc.gridy = row;
//         form.add(createBodyLabel("Security Question:", 20, SwingConstants.CENTER), gbc);
//         gbc.gridx = 1; form.add(securityQuestion, gbc); row++;

//         gbc.gridx = 0; gbc.gridy = row;
//         form.add(createBodyLabel("Security Answer:", 20, SwingConstants.CENTER), gbc);
//         gbc.gridx = 1; form.add(securityAnswer, gbc); row++;

//         // Age row
//         gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
//         form.add(ageLabel, gbc); row++;

//         JPanel ageButtons = new JPanel(new FlowLayout());
//         ageButtons.setOpaque(false);
//         ageButtons.add(yesButton);
//         ageButtons.add(noButton);
//         gbc.gridy = row; form.add(ageButtons, gbc); row++;

//         // Guardian code rows
//         gbc.gridwidth = 1;
//         gbc.gridx = 0; gbc.gridy = row;
//         form.add(guardianCodeLabel, gbc);
//         gbc.gridx = 1; form.add(guardianCodeField, gbc); row++;

//         gbc.gridwidth = 2;
//         gbc.gridx = 0; gbc.gridy = row; form.add(registerButton, gbc); row++;
//         gbc.gridy = row; form.add(backButton, gbc); row++;
//         gbc.gridy = row; form.add(statusLabel, gbc);

//         // Age toggle logic
//         yesButton.addActionListener(e -> {
//             guardianCodeLabel.setVisible(true);
//             guardianCodeField.setVisible(true);
//             form.revalidate();
//             form.repaint();
//         });

//         noButton.addActionListener(e -> {
//             guardianCodeLabel.setVisible(false);
//             guardianCodeField.setVisible(false);
//             guardianCodeField.setText("");
//             form.revalidate();
//             form.repaint();
//         });

//         registerButton.addActionListener(e -> {
//             String username = usernameField.getText().trim();
//             String password = new String(passwordField.getPassword());
//             String question = (String) securityQuestion.getSelectedItem();
//             String answer = securityAnswer.getText().trim();
//             String guardianCode = guardianCodeField.getText().trim();

//             if (username.isEmpty() || password.isEmpty() || answer.isEmpty()) {
//                 statusLabel.setText("Please fill in all required fields.");
//                 return;
//             }

//             if (guardianCodeLabel.isVisible() && guardianCode.isEmpty()) {
//                 statusLabel.setText("Guardian code is required for users under 16.");
//                 return;
//             }

//             boolean success = authService.register(username, password, new String[]{question}, new String[]{answer}, guardianCode);

//             if (success) {
//                 statusLabel.setText("Account created!");
//                 screenManager.showScreen(ScreenManager.PLAYER_MENU);
//             } else {
//                 statusLabel.setText("Username already exists.");
//             }
//         });

//         backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.LOGIN));

//         frame.add(form, BorderLayout.CENTER);
//         add(frame, BorderLayout.CENTER);
//     }
// }


package frontend;

import backend.logic.AuthService;
import java.awt.*;
import javax.swing.*;

/**
 * RegisterPanel handles local account creation for new users.
 * It collects login credentials, security-question information, and guardian
 * approval details when required before saving a new player account.
 */
public class RegisterPanel extends BaseScreenPanel {
    public RegisterPanel(ScreenManager screenManager, AuthService authService) {
        super("Create Account");

        JPanel frame = createMockFrame();
        JPanel form = createInnerBox();
        GridBagConstraints gbc = createGbc();

        JTextField usernameField = createStyledField(16);
        JPasswordField passwordField = createStyledPasswordField(16);
        JTextField securityAnswer = createStyledField(16);
        JTextField guardianCodeField = createStyledField(16);

        String[] questions = {
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What was your childhood nickname?"
        };

        // Styled dropdown
        JComboBox<String> securityQuestion = new JComboBox<>(questions);
        securityQuestion.setBackground(new Color(40, 40, 55));
        securityQuestion.setForeground(new Color(245, 235, 210));
        securityQuestion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        securityQuestion.setBorder(BorderFactory.createLineBorder(new Color(200, 140, 30), 2));

        JLabel ageLabel = createBodyLabel("Are you below the age of 16?", 18, SwingConstants.CENTER);
        JButton yesButton = createMenuButton("Yes");
        JButton noButton = createMenuButton("No");

        JLabel guardianCodeLabel = createBodyLabel("Guardian Code:", 20, SwingConstants.CENTER);
        guardianCodeLabel.setVisible(false);
        guardianCodeField.setVisible(false);

        JLabel statusLabel = createBodyLabel(" ", 16, SwingConstants.CENTER);
        statusLabel.setForeground(new Color(255, 100, 100));
        JButton registerButton = createMenuButton("[ Create Account ]");
        JButton backButton = createMenuButton("[ Back ]");

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Username:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(usernameField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Password:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(passwordField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Security Question:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(securityQuestion, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createBodyLabel("Security Answer:", 20, SwingConstants.CENTER), gbc);
        gbc.gridx = 1; form.add(securityAnswer, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(ageLabel, gbc); row++;

        JPanel ageButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        ageButtons.setOpaque(false);
        ageButtons.add(yesButton);
        ageButtons.add(noButton);
        gbc.gridy = row; form.add(ageButtons, gbc); row++;

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(guardianCodeLabel, gbc);
        gbc.gridx = 1; form.add(guardianCodeField, gbc); row++;

        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = row; form.add(registerButton, gbc); row++;
        gbc.gridy = row; form.add(backButton, gbc); row++;
        gbc.gridy = row; form.add(statusLabel, gbc);

        yesButton.addActionListener(e -> {
            guardianCodeLabel.setVisible(true);
            guardianCodeField.setVisible(true);
            form.revalidate();
            form.repaint();
        });

        noButton.addActionListener(e -> {
            guardianCodeLabel.setVisible(false);
            guardianCodeField.setVisible(false);
            guardianCodeField.setText("");
            form.revalidate();
            form.repaint();
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String question = (String) securityQuestion.getSelectedItem();
            String answer = securityAnswer.getText().trim();
            String guardianCode = guardianCodeField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || answer.isEmpty()) {
                statusLabel.setText("Please fill in all required fields.");
                return;
            }

            if (guardianCodeLabel.isVisible() && guardianCode.isEmpty()) {
                statusLabel.setText("Guardian code required for users under 16.");
                return;
            }

            boolean success = authService.register(username, password,
                new String[]{question}, new String[]{answer}, guardianCode);

            if (success) {
                statusLabel.setText("Account created!");
                screenManager.showScreen(ScreenManager.PLAYER_MENU);
            } else {
                statusLabel.setText("Username already exists.");
            }
        });

        backButton.addActionListener(e -> screenManager.showScreen(ScreenManager.LOGIN));

        frame.add(form, BorderLayout.CENTER);
        add(frame, BorderLayout.CENTER);
    }
}
