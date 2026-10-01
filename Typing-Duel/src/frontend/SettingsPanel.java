package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeListener;

/**
 * SettingsPanel provides the in-game configuration screen for player-adjustable
 * preferences such as audio volume, brightness, and colourblind-friendly HP bar
 * palettes. It also includes a live preview so users can see the effect of
 * their choices before returning to gameplay.
 */
public class SettingsPanel extends BaseScreenPanel {
    private static final int DEFAULT_BRIGHTNESS = 60;

    public SettingsPanel(ScreenManager screenManager) {
        super("Settings");

        JPanel shell = createMockFrame();
        shell.setLayout(new BorderLayout(24, 24));

        JPanel settings = new JPanel(new GridBagLayout());
        settings.setOpaque(false);

        JSlider volumeSlider = new JSlider(0, 100, UiPreferences.getVolumePercent());
        JSlider brightnessSlider = new JSlider(0, 100, UiPreferences.getBrightnessPercent());
        JCheckBox redGreenMode = new JCheckBox("Red-Green");
        JCheckBox blueYellowMode = new JCheckBox("Blue-Yellow");

        styleSlider(volumeSlider);
        styleSlider(brightnessSlider);
        styleCheckbox(redGreenMode);
        styleCheckbox(blueYellowMode);

        JLabel volumeLabel = createSettingsLabel("Volume", 26f);
        JLabel brightnessLabel = createSettingsLabel("Brightness", 26f);
        JLabel modesLabel = createSettingsLabel("Colourblind Accomodation Modes", 22f);
        JLabel volumeValueLabel = createSettingsLabel(UiPreferences.getVolumePercent() + "%", 18f);
        JLabel brightnessValueLabel = createSettingsLabel(UiPreferences.getBrightnessPercent() + "%", 18f);
        JLabel modeDescriptionLabel = createSettingsLabel("Preview: Default HP colours", 18f);

        JButton testSoundButton = createMenuButton("Play Test Sound");
        JButton backButton = createMenuButton("Back");

        HealthBarPreview preview = new HealthBarPreview();
        JPanel previewWrapper = createMockFrame();
        previewWrapper.setPreferredSize(new Dimension(420, 0));

        JPanel previewContent = new JPanel(new BorderLayout(16, 16));
        previewContent.setOpaque(false);
        JLabel previewTitle = createSettingsLabel("HP Bar Colour Preview", 24f);
        previewTitle.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel previewInfo = createPreviewInfoPanel();

        previewContent.add(previewTitle, BorderLayout.NORTH);
        previewContent.add(preview, BorderLayout.CENTER);
        previewContent.add(previewInfo, BorderLayout.SOUTH);
        previewWrapper.add(previewContent, BorderLayout.CENTER);

        // Keep the labels, preferences, and on-screen brightness in sync.
        ChangeListener settingsListener = event -> {
            volumeValueLabel.setText(volumeSlider.getValue() + "%");
            brightnessValueLabel.setText(brightnessSlider.getValue() + "%");
            UiPreferences.setVolumePercent(volumeSlider.getValue());
            UiPreferences.setBrightnessPercent(brightnessSlider.getValue());
            applyBrightnessToWindow(screenManager, brightnessSlider.getValue());
        };

        volumeSlider.addChangeListener(settingsListener);
        brightnessSlider.addChangeListener(settingsListener);

        redGreenMode.addActionListener(event -> {
            if (redGreenMode.isSelected()) {
                blueYellowMode.setSelected(false);
                preview.setPalette(new Color(245, 210, 70), new Color(70, 130, 245), Color.WHITE);
                modeDescriptionLabel.setText("Preview: Red-Green mode active");
                UiPreferences.setColorMode(UiPreferences.ColorMode.RED_GREEN);
            } else {
                preview.setPalette(new Color(35, 230, 55), new Color(240, 35, 35), Color.WHITE);
                modeDescriptionLabel.setText("Preview: Default HP colours");
                UiPreferences.setColorMode(UiPreferences.ColorMode.DEFAULT);
            }
        });

        blueYellowMode.addActionListener(event -> {
            if (blueYellowMode.isSelected()) {
                redGreenMode.setSelected(false);
                preview.setPalette(new Color(245, 140, 40), new Color(240, 35, 35), Color.WHITE);
                modeDescriptionLabel.setText("Preview: Blue-Yellow mode active");
                UiPreferences.setColorMode(UiPreferences.ColorMode.BLUE_YELLOW);
            } else {
                preview.setPalette(new Color(35, 230, 55), new Color(240, 35, 35), Color.WHITE);
                modeDescriptionLabel.setText("Preview: Default HP colours");
                UiPreferences.setColorMode(UiPreferences.ColorMode.DEFAULT);
            }
        });

        testSoundButton.addActionListener(event -> playPreviewTone(volumeSlider.getValue()));
        backButton.addActionListener(event -> screenManager.showScreen(ScreenManager.PLAYER_MENU));

        // Reset the preview and stored preference back to the default colour palette.
        Runnable applyDefaultColours = () -> {
            redGreenMode.setSelected(false);
            blueYellowMode.setSelected(false);
            preview.setPalette(new Color(35, 230, 55), new Color(240, 35, 35), Color.WHITE);
            modeDescriptionLabel.setText("Preview: Default HP colours");
            UiPreferences.setColorMode(UiPreferences.ColorMode.DEFAULT);
        };

        bindButtonShortcut("ENTER", testSoundButton);
        bindButtonShortcut("ESCAPE", backButton);
        bindShortcut("1", () -> {
            if (!redGreenMode.isSelected()) {
                redGreenMode.doClick();
            }
        });
        bindShortcut("2", () -> {
            if (!blueYellowMode.isSelected()) {
                blueYellowMode.doClick();
            }
        });
        bindShortcut("0", applyDefaultColours);

        GridBagConstraints gbc = createGbc();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        settings.add(volumeLabel, gbc);

        gbc.gridy++;
        settings.add(volumeSlider, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        settings.add(volumeValueLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        settings.add(testSoundButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        settings.add(brightnessLabel, gbc);

        gbc.gridy++;
        settings.add(brightnessSlider, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        settings.add(brightnessValueLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        settings.add(modesLabel, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 8, 8, 0);
        settings.add(redGreenMode, gbc);

        gbc.gridy++;
        settings.add(blueYellowMode, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 20, 0);
        settings.add(modeDescriptionLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        settings.add(backButton, gbc);

        shell.add(settings, BorderLayout.CENTER);
        shell.add(previewWrapper, BorderLayout.EAST);
        add(shell, BorderLayout.CENTER);

        syncInitialUiState(volumeSlider, brightnessSlider, redGreenMode, blueYellowMode, preview, modeDescriptionLabel);
        volumeValueLabel.setText(volumeSlider.getValue() + "%");
        brightnessValueLabel.setText(brightnessSlider.getValue() + "%");
        applyBrightnessToWindow(screenManager, brightnessSlider.getValue());
    }

    private void syncInitialUiState(JSlider volumeSlider, JSlider brightnessSlider, JCheckBox redGreenMode,
                                    JCheckBox blueYellowMode, HealthBarPreview preview, JLabel descriptionLabel) {
        if (UiPreferences.getColorMode() == UiPreferences.ColorMode.RED_GREEN) {
            redGreenMode.setSelected(true);
            preview.setPalette(new Color(245, 210, 70), new Color(70, 130, 245), Color.WHITE);
            descriptionLabel.setText("Preview: Red-Green mode active");
        } else if (UiPreferences.getColorMode() == UiPreferences.ColorMode.BLUE_YELLOW) {
            blueYellowMode.setSelected(true);
            preview.setPalette(new Color(245, 140, 40), new Color(240, 35, 35), Color.WHITE);
            descriptionLabel.setText("Preview: Blue-Yellow mode active");
        }
        volumeSlider.setValue(UiPreferences.getVolumePercent());
        brightnessSlider.setValue(UiPreferences.getBrightnessPercent());
    }

    private JLabel createSettingsLabel(String text, float size) {
        JLabel label = createBodyLabel(text, Math.round(size), SwingConstants.LEFT);
        label.setFont(TEKTUR.deriveFont(size));
        label.setForeground(new Color(245, 235, 210));
        return label;
    }

    private JLabel createBannerLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(ACCENT);
        label.setForeground(new Color(245, 235, 210));
        label.setFont(TEKTUR.deriveFont(16f));
        label.setBorder(OUTLINE);
        label.setPreferredSize(new Dimension(300, 46));
        return label;
    }

    private void styleCheckbox(JCheckBox checkbox) {
        checkbox.setOpaque(false);
        checkbox.setForeground(new Color(245, 235, 210));
        checkbox.setFont(TEKTUR.deriveFont(16f));
        checkbox.setFocusPainted(false);
    }

    private void styleSlider(JSlider slider) {
        slider.setOpaque(false);
        slider.setBackground(new Color(28, 28, 42));
        slider.setForeground(new Color(245, 235, 210));
    }

    private JPanel createPreviewInfoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private ImageIcon loadLogoIcon() {
        try {
            java.net.URL resource = SettingsPanel.class.getResource("/images/TypingDuelLogo.png");
            if (resource != null) {
                return new ImageIcon(resource);
            }

            File file = new File("resources/images/TypingDuelLogo.png");
            if (!file.exists()) {
                file = new File("out/images/TypingDuelLogo.png");
            }
            if (!file.exists()) {
                return null;
            }
            BufferedImage image = ImageIO.read(file);
            return image == null ? null : new ImageIcon(image);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyBrightnessToWindow(ScreenManager screenManager, int brightnessPercent) {
        Window window = SwingUtilities.getWindowAncestor(screenManager);
        if (window == null) {
            return;
        }

        applyBrightnessRecursively(window, brightnessPercent);
        window.repaint();
    }

    private void applyBrightnessRecursively(Component component, int brightnessPercent) {
        if (component instanceof JComponent swingComponent) {
            if (swingComponent.getClientProperty("baseBackground") == null) {
                swingComponent.putClientProperty("baseBackground", swingComponent.getBackground());
            }
            if (swingComponent.getClientProperty("baseForeground") == null) {
                swingComponent.putClientProperty("baseForeground", swingComponent.getForeground());
            }

            Color baseBackground = (Color) swingComponent.getClientProperty("baseBackground");
            Color baseForeground = (Color) swingComponent.getClientProperty("baseForeground");

            if (baseBackground != null && swingComponent.isOpaque()) {
                swingComponent.setBackground(adjustBrightness(baseBackground, brightnessPercent));
            }

            if (baseForeground != null && !(swingComponent instanceof JButton)) {
                swingComponent.setForeground(adjustForeground(baseForeground, brightnessPercent));
            }
        }

        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                applyBrightnessRecursively(child, brightnessPercent);
            }
        }
    }

    private Color adjustBrightness(Color base, int brightnessPercent) {
        int clamped = Math.max(0, Math.min(100, brightnessPercent));
        if (clamped == DEFAULT_BRIGHTNESS) {
            return base;
        }

        if (clamped > DEFAULT_BRIGHTNESS) {
            double ratio = (clamped - DEFAULT_BRIGHTNESS) / (double) (100 - DEFAULT_BRIGHTNESS);
            return blend(base, Color.WHITE, ratio * 0.45);
        }

        double ratio = (DEFAULT_BRIGHTNESS - clamped) / (double) DEFAULT_BRIGHTNESS;
        return blend(base, Color.BLACK, ratio * 0.55);
    }

    private Color adjustForeground(Color base, int brightnessPercent) {
        int clamped = Math.max(0, Math.min(100, brightnessPercent));
        if (clamped > DEFAULT_BRIGHTNESS + 20) {
            return blend(base, new Color(220, 220, 220), 0.15);
        }
        if (clamped < DEFAULT_BRIGHTNESS - 20) {
            return blend(base, Color.WHITE, 0.10);
        }
        return base;
    }

    private Color blend(Color from, Color to, double ratio) {
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        int r = (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * clamped);
        int g = (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
        int b = (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * clamped);
        return new Color(r, g, b);
    }

    private void playPreviewTone(int volumePercent) {
        int safeVolume = Math.max(0, Math.min(100, volumePercent));
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();

                    byte[] buffer = new byte[44100 / 8];
                    double amplitude = safeVolume / 100.0;
                    double frequency = 440.0;

                    for (int i = 0; i < buffer.length / 2; i++) {
                        double angle = 2.0 * Math.PI * i * frequency / 44100.0;
                        short sample = (short) (Math.sin(angle) * amplitude * Short.MAX_VALUE * 0.25);
                        buffer[i * 2] = (byte) (sample & 0xff);
                        buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
                    }

                    line.write(buffer, 0, buffer.length);
                    line.drain();
                }
            } catch (LineUnavailableException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        }, "settings-tone-preview").start();
    }

    private static final class HealthBarPreview extends JPanel {
        private Color playerColor = new Color(35, 230, 55);
        private Color enemyColor = new Color(240, 35, 35);
        private Color depletedColor = Color.WHITE;

        private HealthBarPreview() {
            setOpaque(true);
            setBackground(new Color(28, 28, 42));
            setBorder(OUTLINE);
            setPreferredSize(new Dimension(280, 180));
        }

        private void setPalette(Color playerColor, Color enemyColor, Color depletedColor) {
            this.playerColor = playerColor;
            this.enemyColor = enemyColor;
            this.depletedColor = depletedColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int barX = 24;
            int barY = 24;
            int barWidth = getWidth() - 48;
            int barHeight = 26;
            int currentWidth = (int) (barWidth * 0.72);
            int depletedWidth = barWidth - currentWidth;

            g.setColor(Color.BLACK);
            g.fillRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);
            g.setColor(playerColor);
            g.fillRect(barX, barY, currentWidth, barHeight);
            g.setColor(depletedColor);
            g.fillRect(barX + currentWidth, barY, depletedWidth, barHeight);

            g.setColor(new Color(245, 235, 210));
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString("Player HP", barX, barY + 50);

            int enemyY = barY + 76;
            g.setColor(Color.BLACK);
            g.fillRect(barX - 2, enemyY - 2, barWidth + 4, barHeight + 4);
            g.setColor(depletedColor);
            g.fillRect(barX, enemyY, depletedWidth, barHeight);
            g.setColor(enemyColor);
            g.fillRect(barX + depletedWidth, enemyY, currentWidth, barHeight);

            g.setColor(new Color(245, 235, 210));
            g.drawString("Enemy HP", barX, enemyY + 50);
        }
    }
}
