package com.example.nasax.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.nasax.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/** Utility for sharing an APOD image via Android share sheet. */
public final class ShareHelper {

    private ShareHelper() {}

    /**
     * Downloads {@code imageUrl} in the background (using {@code executor}), caches it, and
     * launches the system share sheet with the image and {@code title}.
     *
     * @param fragment  the calling fragment (used for context and activity access)
     * @param imageUrl  URL of the image to share
     * @param title     title shown in the share text
     * @param executor  background executor owned by the caller
     */
    public static void share(Fragment fragment, String imageUrl, String title,
                             ExecutorService executor) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        Context ctx = fragment.requireContext();
        Toast.makeText(ctx, ctx.getString(R.string.preparing_share), Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
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

                if (bmp == null) {
                    showToast(fragment, ctx.getString(R.string.could_not_load_image));
                    return;
                }

                File cacheDir = new File(ctx.getCacheDir(), "images");
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
                File shareFile = new File(cacheDir, "share.jpg");
                try (FileOutputStream out = new FileOutputStream(shareFile)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }

                Uri uri = FileProvider.getUriForFile(
                        ctx, ctx.getPackageName() + ".fileprovider", shareFile);

                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("image/jpeg")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .putExtra(Intent.EXTRA_TEXT, title + "\n\nnasaimage.nasa.gov")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (fragment.getActivity() != null) {
                    fragment.getActivity().runOnUiThread(() ->
                            fragment.startActivity(Intent.createChooser(
                                    intent, ctx.getString(R.string.share_chooser_title) + title)));
                }

            } catch (Exception e) {
                showToast(fragment, ctx.getString(R.string.share_failed) + e.getMessage());
            }
        });
    }

    private static void showToast(Fragment fragment, String msg) {
        if (fragment.getActivity() != null) {
            fragment.getActivity().runOnUiThread(() ->
                    Toast.makeText(fragment.requireContext(), msg, Toast.LENGTH_SHORT).show());
        }
    }
}
