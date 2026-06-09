package com.example.nasax.data.repository;

import android.util.Log;

import com.example.nasax.data.local.dao.FavoriteDao;
import com.example.nasax.data.local.entity.FavoriteEntity;
import com.example.nasax.domain.model.FavoritePhoto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FavoriteRepository {

    private static final String TAG = "FavoriteRepository";

    private final FavoriteDao favoriteDao;
    private final FirestoreRepository firestoreRepository;

    @Inject
    public FavoriteRepository(FavoriteDao favoriteDao, FirestoreRepository firestoreRepository) {
        this.favoriteDao         = favoriteDao;
        this.firestoreRepository = firestoreRepository;
    }

    public void addToFavorites(FavoritePhoto favorite) {
        favoriteDao.insert(mapToEntity(favorite));
        firestoreRepository.saveFavorite(favorite); // fire-and-forget
    }

    public void removeFromFavorites(FavoritePhoto favorite) {
        favoriteDao.delete(mapToEntity(favorite));
        firestoreRepository.deleteFavorite(favorite.getId());
    }

    public void removeFromFavoritesById(String id) {
        favoriteDao.deleteById(id);
        firestoreRepository.deleteFavorite(id);
    }

    public List<FavoritePhoto> getAllFavorites() {
        List<FavoriteEntity> entities = favoriteDao.getAllFavorites();
        List<FavoritePhoto> result = new ArrayList<>();
        for (FavoriteEntity e : entities) {
            result.add(mapToDomain(e));
        }
        return result;
    }

    public boolean isFavorite(String id) {
        return favoriteDao.isFavorite(id);
    }

    /**
     * Pulls all favorites from Firestore and upserts them into the local Room DB.
     * Must be called on a background thread (blocks on Tasks.await inside).
     */
    public void syncFromFirestore() {
        try {
            List<FavoritePhoto> remote = firestoreRepository.fetchFavoritesSync();
            for (FavoritePhoto photo : remote) {
                favoriteDao.insert(mapToEntity(photo)); // REPLACE on conflict
            }
        } catch (Exception e) {
            Log.e(TAG, "syncFromFirestore failed", e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private FavoriteEntity mapToEntity(FavoritePhoto photo) {
        FavoriteEntity entity = new FavoriteEntity(
                photo.getId(),
                photo.getTitle(),
                photo.getDate(),
                photo.getImageUrl(),
                photo.getExplanation(),
                photo.getType()
        );
        entity.setMediaType(photo.getMediaType());
        entity.setVideoUrl(photo.getVideoUrl());
        return entity;
    }

    private FavoritePhoto mapToDomain(FavoriteEntity entity) {
        FavoritePhoto photo = new FavoritePhoto(
                entity.getId(),
                entity.getTitle(),
                entity.getDate(),
                entity.getImageUrl(),
                entity.getExplanation()
        );
        photo.setType(entity.getType());
        photo.setMediaType(entity.getMediaType() != null ? entity.getMediaType() : "image");
        photo.setVideoUrl(entity.getVideoUrl());
        return photo;
    }
}
