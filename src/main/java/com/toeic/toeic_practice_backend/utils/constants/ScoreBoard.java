package com.toeic.toeic_practice_backend.utils.constants;

public class ScoreBoard {
    private static final ScoreBoard INSTANCE = new ScoreBoard();
    public final int[] scoreBoardReading = new int[101];
    public final int[] scoreBoardListening = new int[101];

    private ScoreBoard() {
        setupInitialReadingScores();
        setupInitialListeningScores();
    }

    public static ScoreBoard getInstance() {
        return INSTANCE;
    }

    private void setupInitialReadingScores() {
        for (int i = 0; i < 10; i++) {
            scoreBoardReading[i] = 5;
        }
        int score = 10;
        for (int i = 10; i < 97; i++) {
            scoreBoardReading[i] = score;
            score += 5;
        }
        for (int i = 97; i < 101; i++) {
            scoreBoardReading[i] = 495;
        }
    }

    private void setupInitialListeningScores() {
        for (int i = 0; i < 7; i++) {
            scoreBoardListening[i] = 5;
        }
        int score = 10;
        for (int i = 7; i < 93; i++) {
            scoreBoardListening[i] = score;
            score += 5;
        }
        for (int i = 93; i < 101; i++) {
            scoreBoardListening[i] = 495;
        }
    }

    public int getReadingScore(int index) {
        if (index < 0 || index >= scoreBoardReading.length) {
            throw new IllegalArgumentException("Index out of range");
        }
        return scoreBoardReading[index];
    }

    public int getListeningScore(int index) {
        if (index < 0 || index >= scoreBoardListening.length) {
            throw new IllegalArgumentException("Index out of range");
        }
        return scoreBoardListening[index];
    }
}
