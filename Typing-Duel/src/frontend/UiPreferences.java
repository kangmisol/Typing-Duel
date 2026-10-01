package frontend;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public final class UiPreferences {
    public enum ColorMode {
        DEFAULT,
        RED_GREEN,
        BLUE_YELLOW
    }

    public static final String CHAR_ATK_CHARGEUP = "images/sprites/charATKchargeup.png";
    public static final String CHAR_ATK_SUCCESS = "images/sprites/charATKsuccessFIX.png";
    public static final String CHAR_DFND_CHARGEUP = "images/sprites/charDFNDchargeup.png";
    public static final String CHAR_DFND_FAIL = "images/sprites/charDFNDfail.png";
    public static final String BACKGROUND_IMAGE = "images/backgrounds/typingDuelBackground.jpg";

    public static ImageIcon loadIcon(String path) {
        java.net.URL imgURL = UiPreferences.class.getResource("/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private static int volumePercent = 75;
    private static int brightnessPercent = 60;
    private static ColorMode colorMode = ColorMode.DEFAULT;
    private static final List<Runnable> volumeListeners = new ArrayList<>();

    private UiPreferences() {
    }

    public static int getVolumePercent() {
        return volumePercent;
    }

    public static void setVolumePercent(int volumePercent) {
        int clamped = Math.max(0, Math.min(100, volumePercent));
        if (UiPreferences.volumePercent == clamped) {
            return;
        }
        UiPreferences.volumePercent = clamped;
        notifyVolumeListeners();
    }

    public static int getBrightnessPercent() {
        return brightnessPercent;
    }

    public static void setBrightnessPercent(int brightnessPercent) {
        UiPreferences.brightnessPercent = Math.max(0, Math.min(100, brightnessPercent));
    }

    public static ColorMode getColorMode() {
        return colorMode;
    }

    public static void setColorMode(ColorMode colorMode) {
        UiPreferences.colorMode = colorMode == null ? ColorMode.DEFAULT : colorMode;
    }

    public static Color getPlayerHpColor() {
        return switch (colorMode) {
            case RED_GREEN -> new Color(245, 210, 70);
            case BLUE_YELLOW -> new Color(245, 140, 40);
            default -> new Color(35, 230, 55);
        };
    }

    public static Color getEnemyHpColor() {
        return switch (colorMode) {
            case RED_GREEN -> new Color(70, 130, 245);
            case BLUE_YELLOW, DEFAULT -> new Color(240, 35, 35);
        };
    }

    public static Color getDepletedHpColor() {
        return Color.WHITE;
    }

    public static void addVolumeChangeListener(Runnable listener) {
        if (listener != null && !volumeListeners.contains(listener)) {
            volumeListeners.add(listener);
        }
    }

    public static void removeVolumeChangeListener(Runnable listener) {
        volumeListeners.remove(listener);
    }

    private static void notifyVolumeListeners() {
        List<Runnable> listeners = new ArrayList<>(volumeListeners);
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
