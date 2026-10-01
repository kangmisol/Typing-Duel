package frontend;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import frontend.ScreenManager;

/**
 * ExitConfirmPanel shows a confirmation screen before the application closes.
 * It lets the user safely return to the main menu or confirm that they want
 * to exit Typing Duel.
 */
public class ExitConfirmPanel extends BaseScreenPanel {
    public ExitConfirmPanel(ScreenManager screenManager) {
        super("TITLE GRAPHIC");

        JPanel frame = createMockFrame();
        JPanel inner = createInnerBox();
        GridBagConstraints gbc = createGbc();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inner.add(createBodyLabel("Exit Typing Duel?", 20, javax.swing.SwingConstants.CENTER), gbc);

        javax.swing.JButton yesButton = createMenuButton("[ Yes ]");
        javax.swing.JButton noButton = createMenuButton("[ No ]");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        inner.add(yesButton, gbc);
        gbc.gridx = 1;
        inner.add(noButton, gbc);

        yesButton.addActionListener(event -> System.exit(0));
        noButton.addActionListener(event -> screenManager.showScreen(ScreenManager.MAIN_MENU));

        frame.add(inner, BorderLayout.CENTER);
        add(frame, BorderLayout.CENTER);
    }
}
