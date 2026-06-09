package com.example.nasax.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.nasax.data.local.dao.ApodDao;
import com.example.nasax.data.local.dao.FavoriteDao;
import com.example.nasax.data.local.dao.QuizScoreDao;
import com.example.nasax.data.local.entity.ApodEntity;
import com.example.nasax.data.local.entity.FavoriteEntity;
import com.example.nasax.data.local.entity.QuizScoreEntity;

@Database(
        entities = {
                ApodEntity.class,
                FavoriteEntity.class,
                QuizScoreEntity.class
        },
        version = 5,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase{

    public abstract ApodDao apodDao();
    public abstract FavoriteDao favoriteDao();
    public abstract QuizScoreDao quizScoreDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "nasax_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}