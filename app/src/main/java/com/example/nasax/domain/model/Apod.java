package com.example.nasax.domain.model;

import com.squareup.moshi.Json;

public class Apod {

    @Json(name = "date")
    private String date;

    @Json(name = "explanation")
    private String explanation;

    @Json(name = "hdurl")
    private String hdUrl;

    @Json(name = "media_type")
    private String mediaType;

    @Json(name = "service_version")
    private String serviceVersion;

    @Json(name = "title")
    private String title;

    @Json(name = "url")
    private String url;

    @Json(name = "copyright")
    private String copyright;

    @Json(name = "thumbnail_url")   // importante per i video
    private String thumbnailUrl;

    // ==================== HELPER METHODS ====================

    public boolean isImage() {
        return "image".equalsIgnoreCase(mediaType);
    }

    public boolean isVideo() {
        return "video".equalsIgnoreCase(mediaType);
    }

    /**
     * Restituisce l'URL migliore da mostrare:
     * - Immagine → hdurl se esiste, altrimenti url
     * - Video   → thumbnail_url dalla NASA → fallback thumbnail YouTube → url
     */
    public String getDisplayUrl() {
        if (isImage()) {
            return (hdUrl != null && !hdUrl.isEmpty()) ? hdUrl : url;
        } else {
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) return thumbnailUrl;
            String ytThumb = extractYoutubeThumbnail(url);
            if (ytThumb != null) return ytThumb;
            return url;
        }
    }

    public String getVideoUrl() {
        return isVideo() ? url : null;
    }

    /**
     * Estrae il thumbnail di YouTube dall'URL embed.
     * Supporta: youtube.com/embed/ID  e  youtu.be/ID
     * Restituisce null se l'URL non è YouTube.
     */
    public static String extractYoutubeThumbnail(String videoUrl) {
        if (videoUrl == null) return null;
        String videoId = null;
        if (videoUrl.contains("youtube.com/embed/")) {
            int start = videoUrl.indexOf("youtube.com/embed/") + "youtube.com/embed/".length();
            int end   = videoUrl.indexOf("?", start);
            videoId   = (end > 0) ? videoUrl.substring(start, end) : videoUrl.substring(start);
        } else if (videoUrl.contains("youtu.be/")) {
            int start = videoUrl.indexOf("youtu.be/") + "youtu.be/".length();
            int end   = videoUrl.indexOf("?", start);
            videoId   = (end > 0) ? videoUrl.substring(start, end) : videoUrl.substring(start);
        }
        if (videoId != null && !videoId.trim().isEmpty()) {
            return "https://img.youtube.com/vi/" + videoId.trim() + "/hqdefault.jpg";
        }
        return null;
    }

    // ==================== GETTER & SETTER ====================

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getHdUrl() { return hdUrl; }
    public void setHdUrl(String hdUrl) { this.hdUrl = hdUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getServiceVersion() { return serviceVersion; }
    public void setServiceVersion(String serviceVersion) { this.serviceVersion = serviceVersion; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}