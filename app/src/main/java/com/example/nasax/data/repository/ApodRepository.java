package com.example.nasax.data.repository;

import com.example.nasax.data.local.dao.ApodDao;
import com.example.nasax.data.local.entity.ApodEntity;
import com.example.nasax.data.remote.api.ApiService;
import com.example.nasax.domain.model.Apod;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Response;

public class ApodRepository {

    private final ApiService apiService;
    private final ApodDao apodDao;

    @Inject
    public ApodRepository(ApiService apiService, ApodDao apodDao) {
        this.apiService = apiService;
        this.apodDao = apodDao;
    }

    /**
     * Restituisce l'APOD del giorno:
     * 1. Prova prima dal database (cache)
     * 2. Se non presente o troppo vecchio → carica dalla rete
     */
    public Apod getTodayApod(String apiKey) throws IOException {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());

        ApodEntity cached = apodDao.getApodByDate(today); // ← preciso sulla data
        if (cached != null) {
            return mapEntityToApod(cached);
        }
        return fetchApodFromNetwork(apiKey);
    }

    /**
     * Forza il refresh dalla rete e salva in cache
     */
    public Apod refreshApod(String apiKey) throws IOException {
        return fetchApodFromNetwork(apiKey);
    }

    /**
     * APOD di una data specifica: cache → rete.
     */
    public Apod getApodBySpecificDate(String date, String apiKey) throws IOException {
        ApodEntity cached = apodDao.getApodByDate(date);
        if (cached != null) return mapEntityToApod(cached);

        Response<Apod> response = apiService.getApodByDate(apiKey, date).execute();
        if (response.isSuccessful() && response.body() != null) {
            Apod apod = response.body();
            apodDao.insertApod(mapApodToEntity(apod));
            return apod;
        }
        throw new IOException("API error:" + response.code());
    }

    /**
     * Ultimi N APOD dalla cache — usato in modalità offline.
     */
    public List<Apod> getOfflineApods(int limit) {
        List<ApodEntity> entities = apodDao.getRecentApods(limit);
        return mapEntitiesToApods(entities);
    }

    /**
     * N APOD casuali dalla rete (non salvati in cache per non inquinare l'archivio).
     */
    public List<Apod> getRandomApods(int count, String apiKey) throws IOException {
        Response<List<Apod>> response = apiService.getRandomApods(apiKey, count).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        throw new IOException("API error:" + response.code());
    }

    /**
     * Restituisce tutti gli APOD del mese specificato.
     * Usa la cache Room se già completa, altrimenti scarica dalla rete.
     */
    public List<Apod> getApodsByMonth(int year, int month, String apiKey) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Primo giorno del mese
        String startDate = String.format(Locale.US, "%04d-%02d-01", year, month);

        // Ultimo giorno disponibile: oggi se è il mese corrente, altrimenti fine mese
        Calendar cal = Calendar.getInstance();
        String endDate;
        int expectedDays;

        if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month) {
            endDate = sdf.format(cal.getTime());
            expectedDays = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            Calendar end = Calendar.getInstance();
            end.set(year, month - 1, 1);
            end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = sdf.format(end.getTime());
            expectedDays = end.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        // Controlla cache
        List<ApodEntity> cached = apodDao.getApodsByDateRange(startDate, endDate);
        if (cached != null && cached.size() >= expectedDays) {
            return mapEntitiesToApods(cached);
        }

        // Scarica dalla rete e salva in cache
        try {
            Response<List<Apod>> response = apiService
                    .getApodsByDateRange(apiKey, startDate, endDate)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                List<Apod> apods = response.body();
                List<ApodEntity> entities = new ArrayList<>();
                for (Apod apod : apods) {
                    entities.add(mapApodToEntity(apod));
                }
                apodDao.insertApods(entities);
                return apods;
            } else {
                throw new IOException("API response error:" + response.code());
            }
        } catch (IOException networkEx) {
            // Fallback offline: restituisce la cache parziale del mese se esiste
            if (cached != null && !cached.isEmpty()) return mapEntitiesToApods(cached);
            throw networkEx;
        }
    }

    private List<Apod> mapEntitiesToApods(List<ApodEntity> entities) {
        List<Apod> result = new ArrayList<>();
        for (ApodEntity e : entities) {
            result.add(mapEntityToApod(e));
        }
        return result;
    }

    private Apod fetchApodFromNetwork(String apiKey) throws IOException {
        Response<Apod> response = apiService.getTodayApod(apiKey).execute();

        if (response.isSuccessful() && response.body() != null) {
            Apod apod = response.body();
            // Salva in cache
            ApodEntity entity = mapApodToEntity(apod);
            apodDao.insertApod(entity);
            return apod;
        } else {
            throw new IOException("API response error:" + response.code());
        }
    }

    // ====================== MAPPING ======================

    private ApodEntity mapApodToEntity(Apod apod) {
        return new ApodEntity(
                apod.getDate(),
                apod.getTitle(),
                apod.getExplanation(),
                apod.getUrl(),
                apod.getHdUrl(),
                apod.getMediaType(),
                apod.getThumbnailUrl(),
                apod.getCopyright()
        );
    }

    private Apod mapEntityToApod(ApodEntity entity) {
        Apod apod = new Apod();
        apod.setDate(entity.getDate());
        apod.setTitle(entity.getTitle());
        apod.setExplanation(entity.getExplanation());
        apod.setUrl(entity.getUrl());
        apod.setHdUrl(entity.getHdUrl());
        apod.setMediaType(entity.getMediaType());
        apod.setThumbnailUrl(entity.getThumbnailUrl());
        apod.setCopyright(entity.getCopyright());
        return apod;
    }
}