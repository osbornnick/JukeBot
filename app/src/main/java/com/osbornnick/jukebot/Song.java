package com.osbornnick.jukebot;

import android.graphics.Bitmap;

public class Song {
    private String key, name, artist, suggestedBy;
    private Bitmap albumImage;
    private long duration;
    private int score;
    private boolean anonymous;

    public Song() {
        this.anonymous = true;
    }

    public Song(String id, String name, String artist, String suggestedBy, Bitmap albumImage, long duration, int score, boolean anonymous) {
        this.key = id;
        this.name = name;
        this.artist = artist;
        this.suggestedBy = suggestedBy;
        this.albumImage = albumImage;
        this.duration = duration;
        this.score = score;
        this.anonymous = anonymous;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getSuggestedBy() {
        return suggestedBy;
    }

    public Bitmap getAlbumImage() {
        return albumImage;
    }

    public long getDuration() {
        return duration;
    }

    public int getScore() {
        return score;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
