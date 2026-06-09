package com.example.nasax.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_scores")
public class QuizScoreEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public int  score;
    public int  total;
    public long timestamp;

    public QuizScoreEntity(int score, int total, long timestamp) {
        this.score     = score;
        this.total     = total;
        this.timestamp = timestamp;
    }
}
