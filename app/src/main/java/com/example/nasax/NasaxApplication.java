package com.example.nasax;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.nasax.worker.ApodDailyWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class NasaxApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        scheduleDailyWorker();
    }

    /** Crea il canale notifiche (obbligatorio su Android 8+) */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                ApodDailyWorker.CHANNEL_ID,
                "APOD Daily",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Astronomy Picture of the Day – daily update at 9:00");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    /**
     * Pianifica il worker giornaliero alle 9:00.
     * KEEP: non sovrascrive se già schedulato (evita reset dell'ora all'ogni avvio).
     */
    private void scheduleDailyWorker() {
        long initialDelay = millisToNextNineAM();

        PeriodicWorkRequest dailyWork = new PeriodicWorkRequest.Builder(
                ApodDailyWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                ApodDailyWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,   // non resetta se già esiste
                dailyWork);
    }

    /** Calcola i millisecondi mancanti alla prossima 09:00 */
    private long millisToNextNineAM() {
        Calendar now  = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 9);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1); // già passate le 9 → domani
        return next.getTimeInMillis() - now.getTimeInMillis();
    }
}
