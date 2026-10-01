package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * BaseScreenPanel defines the shared visual style and helper utilities used by
 * the application's Swing screens. It centralizes common colours, fonts,
 * button styling, layout helpers, and keyboard shortcut bindings so the UI
 * remains consistent across different panels.
 */
public abstract class BaseScreenPanel extends JPanel {


    protected static final Color BACKGROUND  = new Color(18, 18, 28);
    protected static final Color FOREGROUND  = new Color(240, 240, 240);
    protected static final Color ACCENT      = new Color(255, 165, 0);
    protected static final Color BTN_TOP     = new Color(60, 60, 80);
    protected static final Color BTN_BOT     = new Color(25, 25, 40);
    protected static final Color BTN_BORDER  = new Color(200, 140, 30); // Darker Bronze
    protected static final Color GOLD        = new Color(255, 215, 0);  // Bright Gold (#FFD700)
    protected static final Color BTN_HOVER   = new Color(255, 190, 50);
    protected static final Border OUTLINE    = BorderFactory.createLineBorder(new Color(80, 80, 100), 1);   

    protected static final Font TEKTUR;
    static {
        Font loaded;
        try {
            java.io.InputStream is = BaseScreenPanel.class
                .getResourceAsStream("/fonts/Tektur-Bold.ttf");
            loaded = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.BOLD, 20f);
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .registerFont(loaded);
        } catch (Exception e) {
            System.err.println("Tektur font not found, using fallback.");
            loaded = new Font("SansSerif", Font.BOLD, 20);
        }
        TEKTUR = loaded;
    }


    protected static final Font BITCOUNT;
    static {
        Font loaded;
        try {
            java.io.InputStream is = BaseScreenPanel.class
                .getResourceAsStream("/fonts/BitcountPropDouble.ttf");

            loaded = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.BOLD, 36f);

            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .registerFont(loaded);

        } catch (Exception e) {
            System.err.println("Bitcount font not found, using fallback.");
            loaded = new Font("SansSerif", Font.BOLD, 36);
        }

        BITCOUNT = loaded;
    }

    protected BaseScreenPanel(String title) {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setBackground(BACKGROUND);

        if (title != null && !title.trim().isEmpty()) {
            JLabel heading = new JLabel(title, SwingConstants.CENTER);
            heading.setForeground(BTN_BORDER);
            heading.setFont(BITCOUNT.deriveFont(28f));
            heading.setPreferredSize(new Dimension(0, 60));
            add(heading, BorderLayout.NORTH);
        }
    }

    protected JPanel createMockFrame() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(28, 28, 42));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BTN_BORDER, 2),
            BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));
        return panel;
    }

    protected JPanel createInnerBox() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(28, 28, 42));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BTN_BORDER, 2),
            BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        return panel;
    }

    protected GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    protected JButton createMenuButton(String text) {
        return createMenuButton(text, false);
    }

    protected JButton createMenuButton(String text, boolean isActive) {
        JButton button = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);

            int w = getWidth();
            int h = getHeight();
            int cut = 12; // how much the corners are cut

            // Build the hexagon-like clipped shape
            int[] xPoints = {cut, w - cut, w, w, w - cut, cut, 0, 0};
            int[] yPoints = {0, 0, cut, h - cut, h, h, h - cut, cut};
            java.awt.Polygon shape = new java.awt.Polygon(xPoints, yPoints, 8);

            // Outer border: Bronze if active, Black otherwise
            Color borderColor = (getClientProperty("active") == Boolean.TRUE) ? BTN_BORDER : new Color(10, 10, 10);
            g2.setColor(borderColor);
            g2.fillPolygon(shape);

            // Shrink for grey inner fill
            int[] xInner = {cut+3, w-cut-3, w-3, w-3, w-cut-3, cut+3, 3, 3};
            int[] yInner = {3, 3, cut+3, h-cut-3, h-3, h-3, h-cut-3, cut+3};
            java.awt.Polygon innerShape = new java.awt.Polygon(xInner, yInner, 8);

            // Grey gradient fill
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(90, 90, 90),
                0, h, new Color(50, 50, 50));
            g2.setPaint(gp);
            g2.fillPolygon(innerShape);

            // Subtle highlight line at top
            g2.setColor(new Color(130, 130, 130));
            g2.drawLine(cut + 4, 5, w - cut - 4, 5);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Suppress default border — we draw our own
        }
    };

    button.putClientProperty("active", isActive);
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setForeground(isActive ? BTN_BORDER : new Color(245, 235, 210)); // Bronze if active
    button.setFont(TEKTUR.deriveFont(12f));
    button.setPreferredSize(new Dimension(300, 52));
    button.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
    button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

    button.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            button.setForeground(new Color(255, 220, 120)); // gold on hover
        }
        @Override
        public void mouseExited(MouseEvent e) {
            button.setForeground(new Color(245, 235, 210)); // back to beige
        }
    });

    return button;
}

    protected JLabel createBodyLabel(String text, int size, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setForeground(FOREGROUND);
        label.setFont(TEKTUR.deriveFont(Font.BOLD, (float) size));
        return label;
    }


    protected javax.swing.JTextField createStyledField(int columns) {
        javax.swing.JTextField field = new javax.swing.JTextField(columns);
        field.setBackground(new Color(40, 40, 55));
        field.setForeground(new Color(245, 235, 210));
        field.setCaretColor(new Color(245, 235, 210));
        field.setFont(TEKTUR.deriveFont(Font.PLAIN, 16f));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 140, 30), 2),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    protected javax.swing.JPasswordField createStyledPasswordField(int columns) {
        javax.swing.JPasswordField field = new javax.swing.JPasswordField(columns);
        field.setBackground(new Color(40, 40, 55));
        field.setForeground(new Color(245, 235, 210));
        field.setCaretColor(new Color(245, 235, 210));
        field.setFont(TEKTUR.deriveFont(Font.PLAIN, 16f));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 140, 30), 2),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    protected void bindShortcut(String keyStroke, Runnable action) {
        String actionKey = "shortcut:" + keyStroke + ":" + System.identityHashCode(action);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    protected void bindButtonShortcut(String keyStroke, JButton button) {
        bindShortcut(keyStroke, () -> {
            if (button.isEnabled() && button.isShowing()) {
                button.doClick();
            }
        });
    }

    protected JPanel createShortcutHintPanel(String title, String... mappings) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(28, 28, 42));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BTN_BORDER, 2),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel heading = new JLabel(title);
        heading.setForeground(new Color(245, 235, 210));
        heading.setFont(TEKTUR.deriveFont(13f));

        StringBuilder html = new StringBuilder("<html>");
        for (String mapping : mappings) {
            html.append(mapping).append("<br/>");
        }
        html.append("</html>");

        JLabel body = new JLabel(html.toString());
        body.setForeground(new Color(220, 214, 196));
        body.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

}
