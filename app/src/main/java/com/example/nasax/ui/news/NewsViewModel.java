package com.example.nasax.ui.news;

import android.util.Log;
import android.util.Xml;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NewsViewModel extends ViewModel {

    // NASA breaking news RSS
    private static final String RSS_URL =
            "https://www.nasa.gov/feeds/oembed/feed.rss";
    // Fallback: image of the day
    private static final String RSS_URL_ALT =
            "https://www.nasa.gov/rss/dyn/lg_image_of_the_day.rss";

    private final MutableLiveData<NewsUiState> state = new MutableLiveData<>();

    public LiveData<NewsUiState> getState() { return state; }

    @Inject
    public NewsViewModel() { refresh(); }

    public void refresh() {
        state.setValue(NewsUiState.loading());
        new Thread(this::fetchRss).start();
    }

    private void fetchRss() {
        try {
            List<NewsItem> items = parseFeed(RSS_URL);
            if (items.isEmpty()) items = parseFeed(RSS_URL_ALT);
            if (items.isEmpty()) {
                state.postValue(NewsUiState.error("No news available"));
            } else {
                state.postValue(NewsUiState.success(items));
            }
        } catch (Exception e) {
            Log.e("NewsViewModel", "fetchRss failed", e);
            state.postValue(NewsUiState.error(e.getMessage()));
        }
    }

    private List<NewsItem> parseFeed(String feedUrl) throws Exception {
        List<NewsItem> items = new ArrayList<>();
        URL url = new URL(feedUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(12_000);
        conn.setReadTimeout(15_000);
        conn.setRequestProperty("User-Agent", "NasaX/1.0");
        conn.connect();

        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            return items;
        }

        InputStream stream = conn.getInputStream();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, "UTF-8");

        String title = null, description = null, link = null, pubDate = null, imageUrl = null;
        boolean inItem = false;
        String tag;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                tag = parser.getName();
                if ("item".equals(tag)) {
                    inItem = true;
                    title = description = link = pubDate = imageUrl = null;
                } else if (inItem) {
                    switch (tag) {
                        case "title":
                            title = readText(parser); break;
                        case "description":
                            description = stripHtml(readText(parser)); break;
                        case "link":
                            link = readText(parser); break;
                        case "pubDate":
                            pubDate = readText(parser); break;
                        case "enclosure":
                            String type = parser.getAttributeValue(null, "type");
                            if (type != null && type.startsWith("image")) {
                                imageUrl = parser.getAttributeValue(null, "url");
                            }
                            break;
                        case "media:thumbnail":
                        case "media:content":
                            if (imageUrl == null)
                                imageUrl = parser.getAttributeValue(null, "url");
                            break;
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && "item".equals(parser.getName())) {
                if (inItem && title != null) {
                    items.add(new NewsItem(title, description, link, formatDate(pubDate), imageUrl));
                }
                inItem = false;
                if (items.size() >= 20) break;
            }
            eventType = parser.next();
        }
        stream.close();
        conn.disconnect();
        return items;
    }

    private String readText(XmlPullParser parser) throws Exception {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result != null ? result.trim() : "";
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                   .replaceAll("&nbsp;", " ").trim();
    }

    /** Formats RSS date: "Wed, 28 May 2025 12:00:00 +0000" → "May 28, 2025" */
    private String formatDate(String rss) {
        if (rss == null || rss.length() < 16) return rss != null ? rss : "";
        try {
            String[] parts = rss.trim().split("\\s+");
            String day = parts[1], mon = parts[2], year = parts[3];
            return mon + " " + day + ", " + year;
        } catch (Exception e) {
            return rss;
        }
    }
}
