package com.template.model;

public class Score {
    private String difficulty;
    private int value;

    public Score(String difficulty, int value) {
        this.difficulty = difficulty;
        this.value = value;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return difficulty + ":" + value;
    }
}
