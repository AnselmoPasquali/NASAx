package com.example.nasax.data.remote.api;

import com.example.nasax.domain.model.Apod;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    /** APOD di oggi — thumbs=true restituisce thumbnail_url per i video */
    @GET("planetary/apod?thumbs=true")
    Call<Apod> getTodayApod(@Query("api_key") String apiKey);

    /** Lista di APOD in un intervallo di date */
    @GET("planetary/apod?thumbs=true")
    Call<List<Apod>> getApodsByDateRange(
            @Query("api_key")    String apiKey,
            @Query("start_date") String startDate,
            @Query("end_date")   String endDate
    );

    /** APOD di una data specifica */
    @GET("planetary/apod?thumbs=true")
    Call<Apod> getApodByDate(
            @Query("api_key") String apiKey,
            @Query("date")    String date
    );

    /** N APOD casuali (non ordinati, dates random) */
    @GET("planetary/apod?thumbs=true")
    Call<List<Apod>> getRandomApods(
            @Query("api_key") String apiKey,
            @Query("count")   int count
    );
}
