package com.example.nasax.ui.archive;

import com.example.nasax.domain.model.Apod;

import java.util.List;

public class ArchiveUiState {

    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final List<Apod> data;
    private final String errorMessage;

    private ArchiveUiState(Status status, List<Apod> data, String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static ArchiveUiState loading() {
        return new ArchiveUiState(Status.LOADING, null, null);
    }

    public static ArchiveUiState success(List<Apod> apods) {
        return new ArchiveUiState(Status.SUCCESS, apods, null);
    }

    public static ArchiveUiState error(String message) {
        return new ArchiveUiState(Status.ERROR, null, message);
    }

    public boolean isLoading()  { return status == Status.LOADING; }
    public boolean isSuccess()  { return status == Status.SUCCESS; }
    public boolean isError()    { return status == Status.ERROR; }

    public List<Apod> getData()       { return data; }
    public String getErrorMessage()   { return errorMessage; }
}
