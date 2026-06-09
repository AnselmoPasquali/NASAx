package com.example.nasax.domain.model;

public class FavoritePhoto {

    private String id;
    private String title;
    private String date;
    private String imageUrl;
    private String explanation;
    private String type = "APOD";
    private String mediaType = "image"; // "image" o "video"
    private String videoUrl;            // URL YouTube (solo se mediaType == "video")

    public FavoritePhoto() {}

    public FavoritePhoto(String id, String title, String date,
                         String imageUrl, String explanation) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.imageUrl = imageUrl;
        this.explanation = explanation;
    }

    public boolean isVideo() { return "video".equalsIgnoreCase(mediaType); }

    /**
     * Restituisce l'URL da usare per caricare l'anteprima:
     * - Se imageUrl è un embed YouTube (salvato prima della fix), estrae il thumbnail
     * - Altrimenti usa imageUrl normalmente
     */
    public String getDisplayImageUrl() {
        if (imageUrl != null && (imageUrl.contains("youtube.com") || imageUrl.contains("youtu.be"))) {
            String ytThumb = Apod.extractYoutubeThumbnail(imageUrl);
            return (ytThumb != null) ? ytThumb : imageUrl;
        }
        return imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}