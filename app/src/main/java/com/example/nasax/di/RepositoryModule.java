package com.example.nasax.di;

import com.example.nasax.data.local.dao.ApodDao;
import com.example.nasax.data.repository.ApodRepository;
import com.example.nasax.data.local.dao.FavoriteDao;
import com.example.nasax.data.repository.FavoriteRepository;
import com.example.nasax.data.repository.FirestoreRepository;
import com.example.nasax.data.remote.api.ApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public ApodRepository provideApodRepository(
            ApiService apiService,
            ApodDao apodDao) {

        return new ApodRepository(apiService, apodDao);
    }

    @Provides
    @Singleton
    public FavoriteRepository provideFavoritesRepository(
            FavoriteDao favoriteDao,
            FirestoreRepository firestoreRepository) {

        return new FavoriteRepository(favoriteDao, firestoreRepository);
    }

}