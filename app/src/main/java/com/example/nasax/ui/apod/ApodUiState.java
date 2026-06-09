package com.example.nasax.ui.apod;

import com.example.nasax.domain.model.Apod;

public class ApodUiState {

    private final Status status;
    private final Apod data;
    private final String errorMessage;

    private ApodUiState(Status status, Apod data, String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static ApodUiState loading() {
        return new ApodUiState(Status.LOADING, null, null);
    }

    public static ApodUiState success(Apod apod) {
        return new ApodUiState(Status.SUCCESS, apod, null);
    }

    public static ApodUiState error(String message) {
        return new ApodUiState(Status.ERROR, null, message);
    }

    // Getter
    public Status getStatus() { return status; }
    public Apod getData() { return data; }
    public String getErrorMessage() { return errorMessage; }

    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }

    public enum Status {
        LOADING, SUCCESS, ERROR
    }
}