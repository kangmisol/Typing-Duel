package backend.model;

/**
 * AccountType.java
 * <p>
 * Defines the privilege levels for user accounts within Typing Duel.
 * Used to determine access to specific game features and administrative 
 * controls (FR 3.1.11).
 * </p>
 *
 * <p>Team 49 â€“ CS2212B Winter 2026</p>
 * @author Misol Kang
 */
public enum AccountType {
    /** * Standard user account. Has access to gameplay, personal statistics, 
     * and high scores. 
     */
    PLAYER,

    /** * Elevated account type with administrative privileges. Grants access 
     * to the Guardian Control Panel to manage other player accounts, 
     * reset passwords, and view aggregate statistics (FR 3.1.7, 3.1.11).
     */
    PARENT_TEACHER
}