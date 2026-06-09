package com.example.nasax.ui.apod;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.data.repository.ApodRepository;
import com.example.nasax.domain.model.Apod;
import com.example.nasax.util.AppConstants;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ApodViewModel extends ViewModel {

    private static final String TAG = "ApodViewModel";

    private final ApodRepository repository;
    private final MutableLiveData<ApodUiState> uiState = new MutableLiveData<>();

    public LiveData<ApodUiState> getUiState() { return uiState; }

    @Inject
    public ApodViewModel(ApodRepository repository) {
        this.repository = repository;
        loadTodayApod();
    }

    public void loadTodayApod() {
        uiState.setValue(ApodUiState.loading());
        new Thread(() -> {
            try {
                Apod apod = repository.getTodayApod(AppConstants.NASA_API_KEY);
                uiState.postValue(ApodUiState.success(apod));
            } catch (Exception e) {
                Log.e(TAG, "loadTodayApod failed", e);
                postOfflineFallback(e);
            }
        }).start();
    }

    public void refreshApod() {
        uiState.setValue(ApodUiState.loading());
        new Thread(() -> {
            try {
                Apod apod = repository.refreshApod(AppConstants.NASA_API_KEY);
                uiState.postValue(ApodUiState.success(apod));
            } catch (Exception e) {
                Log.e(TAG, "refreshApod failed", e);
                postOfflineFallback(e);
            }
        }).start();
    }

    /** Carica l'APOD di una data specifica (formato "yyyy-MM-dd") */
    public void loadApodByDate(String date) {
        uiState.setValue(ApodUiState.loading());
        new Thread(() -> {
            try {
                Apod apod = repository.getApodBySpecificDate(date, AppConstants.NASA_API_KEY);
                uiState.postValue(ApodUiState.success(apod));
            } catch (Exception e) {
                Log.e(TAG, "loadApodByDate failed for " + date, e);
                uiState.postValue(ApodUiState.error(e.getMessage()));
            }
        }).start();
    }

    /** Offline fallback: mostra l'APOD più recente in cache, altrimenti errore. */
    private void postOfflineFallback(Exception e) {
        List<Apod> cached = repository.getOfflineApods(1);
        if (!cached.isEmpty()) {
            uiState.postValue(ApodUiState.success(cached.get(0)));
        } else {
            uiState.postValue(ApodUiState.error(e.getMessage()));
        }
    }
}
