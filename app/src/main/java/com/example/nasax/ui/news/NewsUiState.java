package com.example.nasax.ui.news;

import java.util.List;

public class NewsUiState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final List<NewsItem> items;
    private final String errorMessage;

    private NewsUiState(Status s, List<NewsItem> items, String err) {
        this.status = s; this.items = items; this.errorMessage = err;
    }

    public static NewsUiState loading()                        { return new NewsUiState(Status.LOADING, null, null); }
    public static NewsUiState success(List<NewsItem> items)    { return new NewsUiState(Status.SUCCESS, items, null); }
    public static NewsUiState error(String msg)                { return new NewsUiState(Status.ERROR, null, msg); }

    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError()   { return status == Status.ERROR; }
    public List<NewsItem> getItems() { return items; }
    public String getErrorMessage()  { return errorMessage; }
}
