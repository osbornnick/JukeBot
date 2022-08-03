package com.osbornnick.jukebot;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Song {
    public String key, name, artist, suggestedBy, uri;
    public Bitmap albumImage;
    public long duration;
    public long score;
    public boolean anonymous;
    public String session_id;
    public boolean played = false;
    public boolean deleted = false;

    public Song(Map<String, Object> data) {
        if (data.containsKey("key")) {
            this.key = (String) data.get("key");
        }
        if (data.containsKey("name")) {
            this.name = (String) data.get("name");
        }
        if (data.containsKey("suggestedBy")) {
            this.suggestedBy = (String) data.get("suggestedBy");
        }
        if (data.containsKey("uri")) {
            this.uri = (String) data.get("uri");
        }
        if (data.containsKey("score")) {
            this.score = (long) data.get("score");
        }
        if (data.containsKey("name")) {
            this.name = (String) data.get("name");
        }
        if (data.containsKey("artist")) {
            this.artist = (String) data.get("artist");
        }
        if (data.containsKey("played")) {
            this.played = (boolean) data.get("played");
        }
        if (data.containsKey("deleted")) {
            this.deleted = (boolean) data.get("deleted");
        }
    }

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

    @Exclude
    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public String getArtist() {
        return artist;
    }

    public String getSuggestedBy() {
        return suggestedBy;
    }

    @Exclude
    public Bitmap getAlbumImage() {
        return albumImage;
    }

    @Exclude
    public long getDuration() {
        return duration;
    }

    public long getScore() {
        return score;
    }


    public String getUri() {return uri;}

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setScore(long score) {
        this.score = score;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("Song uri:%s, score:%d", this.uri, this.score);
    }
}
