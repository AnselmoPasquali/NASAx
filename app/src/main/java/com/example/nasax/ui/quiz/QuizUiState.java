package com.example.nasax.ui.quiz;

import java.util.List;

/** Immutable snapshot of the quiz screen state */
public class QuizUiState {

    public enum Phase { LOADING, QUESTION, ANSWERED, GAME_OVER, ERROR }

    private final Phase          phase;
    private final List<QuizQuestion> questions;
    private final int            currentIndex;
    private final int            score;
    private final String         selectedAnswer;  // null until the user taps
    private final String         error;

    private QuizUiState(Phase phase, List<QuizQuestion> questions,
                        int currentIndex, int score,
                        String selectedAnswer, String error) {
        this.phase          = phase;
        this.questions      = questions;
        this.currentIndex   = currentIndex;
        this.score          = score;
        this.selectedAnswer = selectedAnswer;
        this.error          = error;
    }

    // ── factories ────────────────────────────────────────────────────────────

    public static QuizUiState loading() {
        return new QuizUiState(Phase.LOADING, null, 0, 0, null, null);
    }

    public static QuizUiState question(List<QuizQuestion> qs, int idx, int score) {
        return new QuizUiState(Phase.QUESTION, qs, idx, score, null, null);
    }

    public static QuizUiState answered(List<QuizQuestion> qs, int idx,
                                       int score, String selected) {
        return new QuizUiState(Phase.ANSWERED, qs, idx, score, selected, null);
    }

    public static QuizUiState gameOver(int score, int total) {
        return new QuizUiState(Phase.GAME_OVER, null, total, score, null, null);
    }

    public static QuizUiState error(String msg) {
        return new QuizUiState(Phase.ERROR, null, 0, 0, null, msg);
    }

    // ── accessors ────────────────────────────────────────────────────────────

    public Phase          getPhase()          { return phase;          }
    public List<QuizQuestion> getQuestions()  { return questions;      }
    public int            getCurrentIndex()   { return currentIndex;   }
    public int            getScore()          { return score;          }
    public String         getSelectedAnswer() { return selectedAnswer; }
    public String         getError()          { return error;          }

    /** Convenience: returns the current question (null if not in QUESTION/ANSWERED phase) */
    public QuizQuestion currentQuestion() {
        if (questions == null || currentIndex >= questions.size()) return null;
        return questions.get(currentIndex);
    }

    public int totalQuestions() {
        return questions != null ? questions.size() : currentIndex;
    }
}
