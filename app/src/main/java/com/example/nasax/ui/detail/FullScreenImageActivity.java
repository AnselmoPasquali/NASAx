package com.example.nasax.ui.detail;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.nasax.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL   = "extra_image_url";
    public static final String EXTRA_IMAGE_TITLE = "extra_image_title";

    private String imageUrl;
    private String imageTitle;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageUrl   = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        imageTitle = getIntent().getStringExtra(EXTRA_IMAGE_TITLE);
        if (imageTitle == null) imageTitle = "NASAX";

        ImageView imageView = findViewById(R.id.fullscreen_image);
        ImageRequest request = new ImageRequest.Builder(this)
                .data(imageUrl)
                .crossfade(true)
                .target(imageView)
                .build();
        Coil.imageLoader(this).enqueue(request);

        imageView.setOnClickListener(v -> finish());

        findViewById(R.id.btnDownload).setOnClickListener(v -> downloadImage());
        findViewById(R.id.btnShare).setOnClickListener(v -> shareImage());
    }

    // ── Download ──────────────────────────────────────────────────────────────

    private void downloadImage() {
        Toast.makeText(this, getString(R.string.saving), Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                Bitmap bmp = fetchBitmap(imageUrl);
                if (bmp == null) { showToast(getString(R.string.download_failed)); return; }

                String filename = "NASAX_" + System.currentTimeMillis() + ".jpg";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // API 29+ — no storage permission needed
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/NASAX");

                    Uri uri = getContentResolver()
                            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) { showToast(getString(R.string.could_not_save)); return; }

                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    }
                } else {
                    // API 27/28 — WRITE_EXTERNAL_STORAGE permission declared in manifest
                    File dir = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "NASAX");
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                    File file = new File(dir, filename);
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    }
                    // Notify gallery
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(file)));
                }
                showToast(getString(R.string.saved_to_gallery));
            } catch (Exception e) {
                showToast(getString(R.string.error_prefix) + e.getMessage());
            }
        });
    }

    // ── Share ─────────────────────────────────────────────────────────────────

    private void shareImage() {
        Toast.makeText(this, getString(R.string.preparing_share), Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                Bitmap bmp = fetchBitmap(imageUrl);
                if (bmp == null) { showToast(getString(R.string.could_not_load_image)); return; }

                // Save to cache/images/share.jpg
                File cacheDir = new File(getCacheDir(), "images");
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
                File shareFile = new File(cacheDir, "share.jpg");
                try (FileOutputStream out = new FileOutputStream(shareFile)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }

                Uri uri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        shareFile);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, imageTitle + "\n\nnasaimage.nasa.gov");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> startActivity(
                        Intent.createChooser(shareIntent, getString(R.string.share_chooser_title) + imageTitle)));

            } catch (Exception e) {
                showToast(getString(R.string.error_prefix) + e.getMessage());
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Bitmap fetchBitmap(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(20_000);
            conn.connect();
            InputStream stream = conn.getInputStream();
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            stream.close();
            conn.disconnect();
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
