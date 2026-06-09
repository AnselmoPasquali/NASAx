package com.example.nasax.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.nasax.data.local.entity.FavoriteEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteEntity favorite);

    @Delete
    void delete(FavoriteEntity favorite);

    @Query("SELECT * FROM favorites ORDER BY date DESC")
    List<FavoriteEntity> getAllFavorites();

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    boolean isFavorite(String id);

    @Query("DELETE FROM favorites WHERE id = :id")
    void deleteById(String id);
}