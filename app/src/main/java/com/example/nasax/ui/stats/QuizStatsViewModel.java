package com.example.nasax.ui.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.data.local.dao.QuizScoreDao;
import com.example.nasax.data.local.entity.QuizScoreEntity;
import com.example.nasax.data.repository.FirestoreRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuizStatsViewModel extends ViewModel {

    private final QuizScoreDao quizScoreDao;
    private final FirestoreRepository firestoreRepository;
    private final LiveData<List<QuizScoreEntity>> scores;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public QuizStatsViewModel(QuizScoreDao quizScoreDao, FirestoreRepository firestoreRepository) {
        this.quizScoreDao        = quizScoreDao;
        this.firestoreRepository = firestoreRepository;
        this.scores              = quizScoreDao.getRecentScores();
    }

    public LiveData<List<QuizScoreEntity>> getScores() { return scores; }

    /** Saves the score locally (Room) and to Firestore if the user is logged in. */
    public void saveScore(int score, int total) {
        long timestamp = System.currentTimeMillis();
        executor.execute(() -> {
            quizScoreDao.insert(new QuizScoreEntity(score, total, timestamp));
            firestoreRepository.saveQuizScore(score, total, timestamp); // fire-and-forget
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
