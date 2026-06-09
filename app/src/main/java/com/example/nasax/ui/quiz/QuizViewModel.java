package com.example.nasax.ui.quiz;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.data.local.dao.QuizScoreDao;
import com.example.nasax.data.local.entity.QuizScoreEntity;
import com.example.nasax.data.repository.FirestoreRepository;
import com.example.nasax.data.repository.QuizRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuizViewModel extends ViewModel {

    private static final String TAG = "QuizViewModel";

    private final QuizRepository      repository;
    private final QuizScoreDao        quizScoreDao;
    private final FirestoreRepository firestoreRepository;
    private final ExecutorService     executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<QuizUiState> state = new MutableLiveData<>();
    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int score        = 0;

    public LiveData<QuizUiState> getState() { return state; }

    @Inject
    public QuizViewModel(QuizRepository repository,
                         QuizScoreDao quizScoreDao,
                         FirestoreRepository firestoreRepository) {
        this.repository          = repository;
        this.quizScoreDao        = quizScoreDao;
        this.firestoreRepository = firestoreRepository;
        fetchQuestions();
    }

    // ── public actions ────────────────────────────────────────────────────────

    public void submitAnswer(String chosen) {
        QuizUiState current = state.getValue();
        if (current == null || current.getPhase() != QuizUiState.Phase.QUESTION) return;
        QuizQuestion q = current.currentQuestion();
        if (q == null) return;
        if (chosen.equals(q.getCorrectAnswer())) score++;
        state.setValue(QuizUiState.answered(questions, currentIndex, score, chosen));
    }

    public void nextQuestion() {
        currentIndex++;
        if (currentIndex >= questions.size()) {
            saveScore(score, questions.size());
            state.setValue(QuizUiState.gameOver(score, questions.size()));
        } else {
            state.setValue(QuizUiState.question(questions, currentIndex, score));
        }
    }

    public void restart() {
        currentIndex = 0;
        score        = 0;
        fetchQuestions();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void saveScore(int score, int total) {
        long timestamp = System.currentTimeMillis();
        executor.execute(() -> {
            quizScoreDao.insert(new QuizScoreEntity(score, total, timestamp));
            firestoreRepository.saveQuizScore(score, total, timestamp);
        });
    }

    // ── fetch ─────────────────────────────────────────────────────────────────

    private void fetchQuestions() {
        state.setValue(QuizUiState.loading());
        new Thread(() -> {
            try {
                questions = repository.fetchAstronomyQuestions();
                state.postValue(QuizUiState.question(questions, 0, 0));
            } catch (Exception e) {
                Log.e(TAG, "fetchQuestions failed", e);
                state.postValue(QuizUiState.error(e.getMessage()));
            }
        }).start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
