package com.example.nasax.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.nasax.data.local.entity.QuizScoreEntity;

import java.util.List;

@Dao
public interface QuizScoreDao {

    @Insert
    void insert(QuizScoreEntity score);

    /** Ultimi 20 punteggi, dal più recente */
    @Query("SELECT * FROM quiz_scores ORDER BY timestamp DESC LIMIT 20")
    LiveData<List<QuizScoreEntity>> getRecentScores();

    @Query("SELECT COUNT(*) FROM quiz_scores")
    int getTotalGames();

    @Query("SELECT AVG(CAST(score AS REAL) / total) FROM quiz_scores WHERE total > 0")
    float getAveragePercentage();
}
