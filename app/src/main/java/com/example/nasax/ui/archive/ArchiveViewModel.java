package com.example.nasax.ui.archive;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.data.repository.ApodRepository;
import com.example.nasax.util.AppConstants;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ArchiveViewModel extends ViewModel {

    // APOD iniziò il 16 giugno 1995
    private static final int MIN_YEAR  = AppConstants.APOD_MIN_YEAR;
    private static final int MIN_MONTH = AppConstants.APOD_MIN_MONTH;

    private final ApodRepository repository;
    private final MutableLiveData<ArchiveUiState> uiState     = new MutableLiveData<>();
    private final MutableLiveData<String>         monthLabel  = new MutableLiveData<>();
    private final MutableLiveData<Boolean>        canGoPrev   = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean>        canGoNext   = new MutableLiveData<>(false);

    private int currentYear;
    private int currentMonth; // 1-12

    public LiveData<ArchiveUiState> getUiState()    { return uiState;    }
    public LiveData<String>         getMonthLabel() { return monthLabel; }
    public LiveData<Boolean>        getCanGoPrev()  { return canGoPrev;  }
    public LiveData<Boolean>        getCanGoNext()  { return canGoNext;  }

    @Inject
    public ArchiveViewModel(ApodRepository repository) {
        this.repository = repository;
        Calendar cal = Calendar.getInstance();
        currentYear  = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH) + 1;
        loadMonth(currentYear, currentMonth);
    }

    public void previousMonth() {
        if (currentMonth == 1) { currentMonth = 12; currentYear--; }
        else currentMonth--;
        // Non andare prima di giugno 1995
        if (currentYear < MIN_YEAR || (currentYear == MIN_YEAR && currentMonth < MIN_MONTH)) {
            currentYear  = MIN_YEAR;
            currentMonth = MIN_MONTH;
        }
        loadMonth(currentYear, currentMonth);
    }

    public void nextMonth() {
        Calendar now      = Calendar.getInstance();
        int thisYear  = now.get(Calendar.YEAR);
        int thisMonth = now.get(Calendar.MONTH) + 1;
        if (currentYear == thisYear && currentMonth == thisMonth) return; // già al mese corrente
        if (currentMonth == 12) { currentMonth = 1; currentYear++; }
        else currentMonth++;
        loadMonth(currentYear, currentMonth);
    }

    private void loadMonth(int year, int month) {
        uiState.setValue(ArchiveUiState.loading());

        // Aggiorna label e stato frecce
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                           "Jul","Aug","Sep","Oct","Nov","Dec"};
        monthLabel.setValue(months[month - 1] + " " + year);

        Calendar now = Calendar.getInstance();
        int thisYear  = now.get(Calendar.YEAR);
        int thisMonth = now.get(Calendar.MONTH) + 1;
        canGoNext.setValue(!(year == thisYear && month == thisMonth));
        canGoPrev.setValue(!(year == MIN_YEAR && month == MIN_MONTH));

        new Thread(() -> {
            try {
                uiState.postValue(ArchiveUiState.success(
                        repository.getApodsByMonth(year, month, AppConstants.NASA_API_KEY)));
            } catch (Exception e) {
                Log.e("ArchiveViewModel", "loadMonth failed", e);
                uiState.postValue(ArchiveUiState.error(e.getMessage()));
            }
        }).start();
    }
}
