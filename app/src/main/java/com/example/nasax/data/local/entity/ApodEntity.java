package com.example.nasax.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "apod")
public class ApodEntity {

    @PrimaryKey
    @NonNull
    private String date;

    private String title;
    private String explanation;
    private String url;
    private String hdUrl;
    private String mediaType;
    private String thumbnailUrl;
    private String copyright;

    // Costruttore vuoto richiesto da Room
    public ApodEntity() {}

    // Costruttore con parametri (usato dal Repository)
    @Ignore
    public ApodEntity(String date, String title, String explanation, String url,
                      String hdUrl, String mediaType, String thumbnailUrl, String copyright) {
        this.date = date;
        this.title = title;
        this.explanation = explanation;
        this.url = url;
        this.hdUrl = hdUrl;
        this.mediaType = mediaType;
        this.thumbnailUrl = thumbnailUrl;
        this.copyright = copyright;
    }

    // GETTER & SETTER
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getHdUrl() { return hdUrl; }
    public void setHdUrl(String hdUrl) { this.hdUrl = hdUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }
}