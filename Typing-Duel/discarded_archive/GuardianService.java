package com.template.service;

import java.io.IOException;
import java.nio.file.Path;

import com.template.persistence.CsvDatabaseHandler;
import com.template.persistence.UserRepository;

public class GuardianService {
    private final UserRepository userRepository;
    private final CsvDatabaseHandler csvDatabaseHandler;
    private final Path leaderboardPath;
    private final Path leaderboardTemplatePath;

    public GuardianService(UserRepository userRepository, CsvDatabaseHandler csvDatabaseHandler,
            Path leaderboardPath, Path leaderboardTemplatePath) {
        this.userRepository = userRepository;
        this.csvDatabaseHandler = csvDatabaseHandler;
        this.leaderboardPath = leaderboardPath;
        this.leaderboardTemplatePath = leaderboardTemplatePath;
    }

    public void resetPlayer(String username) throws IOException {
        userRepository.resetPlayer(username);
    }

    public void restoreDefaultLeaderboard() throws IOException {
        csvDatabaseHandler.overwriteWithTemplate(leaderboardPath, leaderboardTemplatePath);
    }
}
