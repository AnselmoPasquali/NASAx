package com.example.nasax.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String date;
    private String imageUrl;
    private String explanation;
    private String type;
    private String mediaType = "image";
    private String videoUrl;

    public FavoriteEntity() {}

    public FavoriteEntity(@NonNull String id, String title, String date,
                          String imageUrl, String explanation, String type) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.imageUrl = imageUrl;
        this.explanation = explanation;
        this.type = type;
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