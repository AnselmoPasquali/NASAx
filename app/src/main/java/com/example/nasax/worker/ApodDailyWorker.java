package com.example.nasax.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.nasax.R;
import com.example.nasax.domain.model.Apod;
import com.example.nasax.ui.main.MainActivity;
import com.example.nasax.util.AppConstants;
import com.squareup.moshi.Moshi;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApodDailyWorker extends Worker {

    public static final String CHANNEL_ID   = "nasax_apod_daily";
    public static final String WORK_NAME    = "apod_daily_work";
    private static final int NOTIF_ID = 1001;

    public ApodDailyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // ── 1. Fetch today's APOD ─────────────────────────────────────────
            OkHttpClient client = new OkHttpClient();
            String apiUrl = "https://api.nasa.gov/planetary/apod?thumbs=true&api_key=" + AppConstants.NASA_API_KEY;
            Request httpRequest = new Request.Builder().url(apiUrl).build();
            Response httpResponse = client.newCall(httpRequest).execute();

            if (!httpResponse.isSuccessful() || httpResponse.body() == null) {
                return Result.retry();
            }

            String json = httpResponse.body().string();
            Moshi moshi = new Moshi.Builder().build();
            Apod apod = moshi.adapter(Apod.class).fromJson(json);
            if (apod == null) return Result.failure();

            // ── 2. Download thumbnail (scaled to 512×256 max for binder IPC) ──
            String thumbUrl = apod.isVideo() ? apod.getDisplayUrl() : apod.getUrl();
            Bitmap thumbnail = downloadScaledBitmap(thumbUrl, 512, 256);

            // ── 3. Show notification ──────────────────────────────────────────
            showNotification(apod.getTitle(), apod.getExplanation(), thumbnail);

            // ── 4. Update home screen widget ──────────────────────────────────
            com.example.nasax.ui.widget.ApodWidget.updateWidget(
                    getApplicationContext(), apod.getTitle(), apod.getDate(), thumbnail);

            return Result.success();

        } catch (Exception e) {
            return Result.retry();
        }
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private void showNotification(String title, String explanation, Bitmap thumbnail) {
        Context ctx = getApplicationContext();
        NotificationManager manager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create / update channel
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "APOD Daily", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Astronomy Picture of the Day – daily update");
        manager.createNotificationChannel(channel);

        // Tap → open MainActivity
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Short text for collapsed notification
        String shortText = explanation != null && explanation.length() > 120
                ? explanation.substring(0, 120) + "…" : explanation;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.nasa_logo)
                .setContentTitle("🌌 " + title)
                .setContentText(shortText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (thumbnail != null) {
            builder.setLargeIcon(thumbnail);
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(thumbnail)
                    .bigLargeIcon((Bitmap) null)   // hide large icon when expanded
                    .setSummaryText(shortText));
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(shortText));
        }

        manager.notify(NOTIF_ID, builder.build());
    }

    // ── Bitmap helpers ────────────────────────────────────────────────────────

    /**
     * Downloads and scales a bitmap so that neither dimension exceeds maxW×maxH.
     * Returns null on failure (notification will still show, just without image).
     */
    public static Bitmap downloadScaledBitmap(String imageUrl, int maxW, int maxH) {
        if (imageUrl == null) return null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8_000);
            conn.setReadTimeout(15_000);
            conn.connect();

            // Decode bounds only first
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            InputStream stream = conn.getInputStream();
            BitmapFactory.decodeStream(stream, null, opts);
            stream.close();
            conn.disconnect();

            // Calculate inSampleSize
            int sampleSize = 1;
            int w = opts.outWidth, h = opts.outHeight;
            while (w / sampleSize > maxW || h / sampleSize > maxH) sampleSize *= 2;

            // Decode for real
            HttpURLConnection conn2 = (HttpURLConnection) new URL(imageUrl).openConnection();
            conn2.setConnectTimeout(8_000);
            conn2.setReadTimeout(15_000);
            conn2.connect();
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = sampleSize;
            Bitmap bmp = BitmapFactory.decodeStream(conn2.getInputStream(), null, opts);
            conn2.disconnect();
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }
}
