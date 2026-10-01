package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * TutorialPanel provides a step-by-step gameplay guide for new players.
 * It combines short written instructions with sprite illustrations so users
 * can learn the combat, scoring, and power-up systems before starting a match.
 */
public class TutorialPanel extends BaseScreenPanel {
    private final String[] tutorialSteps = {
        "Goal:\nHit your opponent enough times to reduce their hitpoints to 0 before they do the same to you!\n\nHitpoints (HP):\nEach time you are struck, your HP is reduced. Once it reaches 0, you lose. Similarly, each time you strike an opponent, their HP is reduced, and once it reaches 0, you win.",
        "Attacking / Defending:\nAttack by typing the word before the timer expires. Defend by typing correctly to block incoming damage.",
        "Score:\nEach successful attack or defense gives score based on the difficulty of the word, and speed at which you typed it.",
        "Power-Ups:\nClaim special words to activate special boosts!\n\nFlurry Rush:\nAfter Flurry Rush is claimed, your next attack will deal double damage.\n\nBrick Wall:\nAfter Brick Wall is claimed, your next successful defense will deal damage to the opponent equal to a normal attack."
    };

    private int currentStep;

    public TutorialPanel(ScreenManager screenManager) {
        super("How to Play Typing Duel");

        JPanel frame = createMockFrame();
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);

        JPanel images = new JPanel(new GridLayout(2, 1, 12, 12));
        images.setOpaque(false);

        ImageIcon attackIcon = UiPreferences.loadIcon("images/sprites/charATKchargeup.png");
        ImageIcon defendIcon = UiPreferences.loadIcon("images/sprites/charDFNDchargeup.png");

        JPanel attackPanel = createTutorialImageCard(
                scaleIcon(attackIcon, 250, 250),
                "Attack: Type fast to deal damage"
        );

        JPanel defendPanel = createTutorialImageCard(
                scaleIcon(defendIcon, 250, 250),
                "Defend: Type before time runs out"
        );

        images.add(attackPanel);
        images.add(defendPanel);

        JTextPane description = createDescriptionPane();
        setDescriptionText(description, tutorialSteps[currentStep]);

        javax.swing.JButton nextButton = createMenuButton("Next");
        javax.swing.JButton backButton = createMenuButton("Back");

        nextButton.addActionListener(event -> {
            currentStep++;
            if (currentStep >= tutorialSteps.length) {
                currentStep = 0;
                nextButton.setText("Next");
                setDescriptionText(description, tutorialSteps[currentStep]);
                screenManager.showScreen(ScreenManager.PLAYER_MENU);
                return;
            }
            if (currentStep == tutorialSteps.length - 1) {
                nextButton.setText("Done");
            }
            setDescriptionText(description, tutorialSteps[currentStep]);
        });

        backButton.addActionListener(event -> {
            if (currentStep > 0) {
                currentStep--;
                nextButton.setText("Next");
                setDescriptionText(description, tutorialSteps[currentStep]);
            } else {
                screenManager.showScreen(ScreenManager.PLAYER_MENU);
            }
        });

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setOpaque(false);
        right.add(description, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.add(backButton);
        buttons.add(nextButton);
        right.add(buttons, BorderLayout.SOUTH);

        content.add(images, BorderLayout.WEST);
        content.add(right, BorderLayout.CENTER);
        frame.add(content, BorderLayout.CENTER);
        add(frame, BorderLayout.CENTER);
    }

    private JTextPane createDescriptionPane() {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setForeground(new java.awt.Color(245, 235, 210));
        pane.setBackground(new java.awt.Color(28, 28, 42));
        pane.setBorder(BorderFactory.createCompoundBorder(
                OUTLINE,
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        pane.setFont(TEKTUR.deriveFont(Font.PLAIN, 16f));
        return pane;
    }

    private void setDescriptionText(JTextPane pane, String text) {
        pane.setText(text);
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(attrs, TEKTUR.getFamily());
        StyleConstants.setFontSize(attrs, 16);
        StyleConstants.setForeground(attrs, new java.awt.Color(245, 235, 210));
        doc.setParagraphAttributes(0, doc.getLength(), attrs, true);
        pane.setCaretPosition(0);
    }

    private JPanel createTutorialImageCard(ImageIcon icon, String caption) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(true);
        panel.setBackground(new java.awt.Color(28, 28, 42));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BTN_BORDER, 2),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(false);
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(createBodyLabel(caption, 14, SwingConstants.CENTER), BorderLayout.SOUTH);
        return panel;
    }

    private ImageIcon scaleIcon(ImageIcon icon, int maxWidth, int maxHeight) {
        if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            return icon;
        }

        double widthRatio = (double) maxWidth / icon.getIconWidth();
        double heightRatio = (double) maxHeight / icon.getIconHeight();
        double scale = Math.min(widthRatio, heightRatio);

        int targetWidth = Math.max(1, (int) Math.round(icon.getIconWidth() * scale));
        int targetHeight = Math.max(1, (int) Math.round(icon.getIconHeight() * scale));

        Image scaled = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private JLabel createImagePlaceholder() {
        JLabel label = new JLabel("Image Placeholder", SwingConstants.CENTER);
        label.setForeground(new Color(90, 90, 90));
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setBorder(OUTLINE);
        label.setOpaque(true);
        label.setBackground(BACKGROUND);
        label.setSize(280, 160);
        return label;
    }
}
