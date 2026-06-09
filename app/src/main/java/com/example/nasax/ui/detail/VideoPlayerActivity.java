package com.example.nasax.ui.detail;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nasax.R;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        webView = findViewById(R.id.webView);
        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        if (videoUrl != null) {
            // Se è un embed YouTube, avvolgiamo in HTML per gestire il fullscreen correttamente
            if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                String html = "<html><body style='margin:0;padding:0;background:#000;'>"
                        + "<iframe width='100%' height='100%' src='" + videoUrl
                        + "' frameborder='0' allowfullscreen></iframe>"
                        + "</body></html>";
                webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null);
            } else {
                webView.loadUrl(videoUrl);
            }
        }

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
