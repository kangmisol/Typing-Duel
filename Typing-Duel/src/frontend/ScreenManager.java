package frontend;
import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JPanel;

/**
 * ScreenManager coordinates navigation between the application's Swing screens
 * using a CardLayout. It also stores lightweight shared UI state, such as the
 * currently selected character, and notifies screens when they become visible.
 */
public class ScreenManager extends JPanel {
    public static final String MAIN_MENU = "mainMenu";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String FORGOT_PASSWORD = "forgotPassword";
    public static final String GUARDIAN_LOGIN = "guardianLogin";
    public static final String PLAYER_MENU = "playerMenu";
    public static final String LOGOUT_CONFIRM = "logoutConfirm";
    public static final String TUTORIAL = "tutorial";
    public static final String LEVEL_SELECT = "levelSelect";
    public static final String STANDARD_GAMEPLAY = "standardGameplay";
    public static final String HIGH_SCORES = "highScores";
    public static final String SETTINGS = "settings";
    public static final String GUARDIAN_CONTROLS = "guardianControls";
    public static final String EXIT_CONFIRM = "exitConfirm";
    private final CardLayout cardLayout;

    // store selected character globally
    private String selectedCharacter = "The Knight"; // default
    public ScreenManager() {
        this.cardLayout = new CardLayout();
        setLayout(cardLayout);
    }
    public void registerScreen(String key, JPanel panel) {
        add(panel, key);
    }
    public void showScreen(String key) {
        cardLayout.show(this, key);
        for (Component c : getComponents()) {
            if (c.isVisible() && c instanceof ScreenAware) {
                ((ScreenAware) c).onScreenShown();
            }
        }
    }

    // ===============================
    // CHARACTER SELECTION METHODS
    // ===============================

    public void setSelectedCharacter(String character) {
        this.selectedCharacter = character;
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public interface ScreenAware {
        void onScreenShown();
    }
}
