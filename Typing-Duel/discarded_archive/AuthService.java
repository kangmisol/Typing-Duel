package com.template.service;

import java.io.IOException;

import com.template.model.User;
import com.template.persistence.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private User currentUser;
    private User currentGuardianTarget;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean loginPlayer(String username, char[] password) throws IOException {
        User user = userRepository.findByUsername(username);
        boolean valid = user != null && user.getPassword().equals(new String(password));
        if (valid) {
            currentUser = user;
        }
        return valid;
    }

    public boolean loginGuardian(String playerUsername, String password) throws IOException {
        User user = userRepository.findByUsername(playerUsername);
        boolean valid = user != null && user.getPassword().equals(password);
        if (valid) {
            currentGuardianTarget = user;
        }
        return valid;
    }

    public boolean registerPlayer(String username, char[] password, String guardianLink) throws IOException {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }
        User user = new User(username, new String(password), guardianLink);
        userRepository.save(user);
        currentUser = user;
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getCurrentGuardianTarget() {
        return currentGuardianTarget;
    }

    public void logoutPlayer() {
        currentUser = null;
    }

    public void logoutGuardian() {
        currentGuardianTarget = null;
    }
}
