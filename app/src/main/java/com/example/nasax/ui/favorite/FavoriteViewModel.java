package com.example.nasax.ui.favorite;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.data.repository.FavoriteRepository;
import com.example.nasax.domain.model.FavoritePhoto;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FavoriteViewModel extends ViewModel {

    private final FavoriteRepository repository;
    private final MutableLiveData<List<FavoritePhoto>> favoritesLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public FavoriteViewModel(FavoriteRepository repository) {
        this.repository = repository;
        loadFavorites();
    }

    public LiveData<List<FavoritePhoto>> getFavoritesLiveData() {
        return favoritesLiveData;
    }

    public void loadFavorites() {
        executor.execute(() -> {
            List<FavoritePhoto> favorites = repository.getAllFavorites();
            favoritesLiveData.postValue(favorites);
        });
    }

    public void addToFavorites(FavoritePhoto favorite) {
        executor.execute(() -> {
            repository.addToFavorites(favorite);
            List<FavoritePhoto> updated = repository.getAllFavorites();
            favoritesLiveData.postValue(updated);
        });
    }

    public void removeFromFavorites(FavoritePhoto favorite) {
        executor.execute(() -> {
            repository.removeFromFavorites(favorite);
            List<FavoritePhoto> updated = repository.getAllFavorites();
            favoritesLiveData.postValue(updated);
        });
    }

    public void removeFromFavoritesById(String id) {
        executor.execute(() -> {
            repository.removeFromFavoritesById(id);
            List<FavoritePhoto> updated = repository.getAllFavorites();
            favoritesLiveData.postValue(updated);
        });
    }

    /**
     * Pulls favorites from Firestore and merges them into the local DB.
     * Called once after a successful login.
     */
    public void syncFromFirestore() {
        executor.execute(() -> {
            repository.syncFromFirestore();
            favoritesLiveData.postValue(repository.getAllFavorites());
        });
    }

    /** Returns a one-shot LiveData that emits whether the given id is a favorite. */
    public LiveData<Boolean> checkIsFavorite(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(repository.isFavorite(id)));
        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown(); // evita memory leak
    }
}