package backend;

import java.net.URL;

/**
 * Simple utility for verifying that packaged application resources can be resolved.
 *
 * <p>This class looks up a small set of known classpath resources and prints the
 * resolved URLs to standard output. It is intended for quick manual validation
 * of resource packaging during development.</p>
 *
 * <p>Team 49 - CS2212B Winter 2026</p>
 *
 * @author Mrida Hingmire
 */
public class TestResource {

    /**
     * Runs a simple resource lookup check for a fixed list of expected assets.
     *
     * @param args command-line arguments; ignored by this utility
     */
    public static void main(String[] args) {
        String[] paths = {"/backgrounds/typingDuelBackground.jpg", "/images/TypingDuelLogo.png"};
        for (String p : paths) {
            URL url = TestResource.class.getResource(p);
            System.out.println(p + " -> " + url);
        }
    }
}
