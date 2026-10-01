package com.template.service;

import com.template.model.User;

public class ProgressionService {
    public boolean isEndlessUnlocked(User user) {
        return user != null
            && user.hasScoreFor("easy")
            && user.hasScoreFor("medium")
            && user.hasScoreFor("hard");
    }
}
