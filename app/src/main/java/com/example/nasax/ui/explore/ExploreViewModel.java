package com.example.nasax.ui.explore;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.data.repository.ApodRepository;
import com.example.nasax.util.AppConstants;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ExploreViewModel extends ViewModel {

    private static final int COUNT = 12; // APOD casuali per sessione

    private final ApodRepository repository;
    private final MutableLiveData<ExploreUiState> uiState = new MutableLiveData<>();

    public LiveData<ExploreUiState> getUiState() { return uiState; }

    @Inject
    public ExploreViewModel(ApodRepository repository) {
        this.repository = repository;
        shuffle();
    }

    /** Carica un nuovo set di APOD casuali */
    public void shuffle() {
        uiState.setValue(ExploreUiState.loading());
        new Thread(() -> {
            try {
                uiState.postValue(ExploreUiState.success(
                        repository.getRandomApods(COUNT, AppConstants.NASA_API_KEY)));
            } catch (Exception e) {
                Log.e("ExploreViewModel", "shuffle failed", e);
                uiState.postValue(ExploreUiState.error(e.getMessage()));
            }
        }).start();
    }
}
