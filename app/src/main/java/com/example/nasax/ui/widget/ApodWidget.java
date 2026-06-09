package com.example.nasax.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.example.nasax.R;
import com.example.nasax.ui.main.MainActivity;
import com.example.nasax.util.AppConstants;
import com.example.nasax.worker.ApodDailyWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApodWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // When the widget is first placed / system requests update, trigger a worker run
        // so the latest APOD is fetched and shown.
        scheduleImmediateUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {
        // First widget instance added to home screen
        scheduleImmediateUpdate(context);
    }

    /**
     * Called by ApodDailyWorker after fetching fresh data.
     * Updates all active widget instances.
     */
    public static void updateWidget(Context context, String title, String date, Bitmap bitmap) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, ApodWidget.class);
        int[] widgetIds = manager.getAppWidgetIds(widgetComponent);

        if (widgetIds == null || widgetIds.length == 0) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_apod);

        // Tap → open app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent);

        if (title != null)  views.setTextViewText(R.id.widget_title, title);
        if (date  != null)  views.setTextViewText(R.id.widget_date,  date);
        if (bitmap != null) views.setImageViewBitmap(R.id.widget_image, bitmap);

        for (int id : widgetIds) {
            manager.updateAppWidget(id, views);
        }
    }

    /**
     * Runs a one-time WorkManager request immediately so the widget
     * shows fresh data right after being placed on the home screen.
     */
    private void scheduleImmediateUpdate(Context context) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Fetch and update synchronously on the background thread
                String apiUrl = "https://api.nasa.gov/planetary/apod?thumbs=true&api_key="
                        + AppConstants.NASA_API_KEY;
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Response resp = client.newCall(
                        new okhttp3.Request.Builder().url(apiUrl).build()).execute();

                if (!resp.isSuccessful() || resp.body() == null) return;
                String json = resp.body().string();

                com.squareup.moshi.Moshi moshi = new com.squareup.moshi.Moshi.Builder().build();
                com.example.nasax.domain.model.Apod apod =
                        moshi.adapter(com.example.nasax.domain.model.Apod.class).fromJson(json);
                if (apod == null) return;

                String thumbUrl = apod.isVideo() ? apod.getDisplayUrl() : apod.getUrl();
                Bitmap bmp = ApodDailyWorker.downloadScaledBitmap(thumbUrl, 512, 256);

                updateWidget(context, apod.getTitle(), apod.getDate(), bmp);
            } catch (Exception ignored) { }
        });
        executor.shutdown();
    }
}
