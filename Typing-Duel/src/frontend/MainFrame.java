package frontend;

import java.awt.BorderLayout;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import backend.logic.AuthService;
import backend.logic.GuardianService;

import backend.logic.ProgressionService;
import frontend.GuardianControlPanel;
import frontend.GuardianLoginPanel;
import frontend.HighScorePanel;
import frontend.LevelSelectPanel;
import frontend.StandardGameplayPanel;
//import frontend.EndlessGameplayPanel;
import frontend.LoginPanel;
import frontend.ForgotPasswordPanel;
import frontend.ExitConfirmPanel;
import frontend.MainMenuPanel;
import frontend.PlayerMenuPanel;
import frontend.RegisterPanel;
import frontend.SettingsPanel;
import frontend.TutorialPanel;
import frontend.LogoutConfirmPanel;

/**
 * MainFrame is the top-level application window for Typing Duel.
 * It initializes shared services, registers every navigable screen with the
 * ScreenManager, and shows the starting menu when the game launches.
 */
public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Typing Duel");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Path dataDir = Path.of("data");
        backend.storage.FileDataStore dataStore;
        try {
            dataStore = new backend.storage.FileDataStore(dataDir.toString());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to initialize DataStore", e);
        }
        dataStore.loadConfig();

        AuthService authService = new AuthService(dataStore);
        ProgressionService progressionService = new ProgressionService();
        GuardianService guardianService = new GuardianService(dataStore);

        ScreenManager screenManager = new ScreenManager();
        screenManager.registerScreen(ScreenManager.MAIN_MENU, new MainMenuPanel(screenManager));
        screenManager.registerScreen(ScreenManager.LOGIN, new LoginPanel(screenManager, authService, guardianService));
        screenManager.registerScreen(ScreenManager.REGISTER, new RegisterPanel(screenManager, authService));
        screenManager.registerScreen(ScreenManager.FORGOT_PASSWORD, new ForgotPasswordPanel(screenManager, authService));
        screenManager.registerScreen(ScreenManager.GUARDIAN_LOGIN,
                new GuardianLoginPanel(screenManager, authService, guardianService));
        screenManager.registerScreen(ScreenManager.PLAYER_MENU, 
                new PlayerMenuPanel(screenManager, authService, guardianService));
        screenManager.registerScreen(ScreenManager.TUTORIAL, new TutorialPanel(screenManager));
        screenManager.registerScreen(ScreenManager.LEVEL_SELECT, new LevelSelectPanel(screenManager, progressionService, authService));
        screenManager.registerScreen(ScreenManager.HIGH_SCORES, new HighScorePanel(screenManager, authService));
        screenManager.registerScreen(ScreenManager.SETTINGS, new SettingsPanel(screenManager));
        screenManager.registerScreen(ScreenManager.GUARDIAN_CONTROLS, new GuardianControlPanel(screenManager, guardianService, authService));
        screenManager.registerScreen(ScreenManager.EXIT_CONFIRM, new ExitConfirmPanel(screenManager));
        screenManager.registerScreen(ScreenManager.LOGOUT_CONFIRM, new LogoutConfirmPanel(screenManager));
        screenManager.showScreen(ScreenManager.MAIN_MENU);

        setLayout(new BorderLayout());
        add(screenManager, BorderLayout.CENTER);
    }
}
