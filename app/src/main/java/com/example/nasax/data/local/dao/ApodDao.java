package com.example.nasax.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.nasax.data.local.entity.ApodEntity;

import java.util.List;

@Dao
public interface ApodDao {

    @Query("SELECT * FROM apod WHERE date = :date LIMIT 1")
    ApodEntity getApodByDate(String date);

    @Query("SELECT * FROM apod ORDER BY date DESC LIMIT 1")
    ApodEntity getLatestApod();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertApod(ApodEntity apodEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertApods(List<ApodEntity> apods);

    @Query("SELECT * FROM apod WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    List<ApodEntity> getApodsByDateRange(String startDate, String endDate);

    /** Ultimi N APOD per data — usato come fallback offline */
    @Query("SELECT * FROM apod ORDER BY date DESC LIMIT :limit")
    List<ApodEntity> getRecentApods(int limit);

    @Query("DELETE FROM apod")
    void deleteAllApods();
}