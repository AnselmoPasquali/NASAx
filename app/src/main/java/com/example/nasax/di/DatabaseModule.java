package com.example.nasax.di;

import android.content.Context;

import com.example.nasax.data.local.dao.ApodDao;
import com.example.nasax.data.local.dao.FavoriteDao;
import com.example.nasax.data.local.dao.QuizScoreDao;
import com.example.nasax.data.local.database.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context);
    }

    @Provides
    @Singleton
    public ApodDao provideApodDao(AppDatabase appDatabase) {
        return appDatabase.apodDao();
    }

    @Provides
    @Singleton
    public FavoriteDao provideFavoriteDao(AppDatabase appDatabase) {
        return appDatabase.favoriteDao();
    }

    @Provides
    @Singleton
    public QuizScoreDao provideQuizScoreDao(AppDatabase appDatabase) {
        return appDatabase.quizScoreDao();
    }
}