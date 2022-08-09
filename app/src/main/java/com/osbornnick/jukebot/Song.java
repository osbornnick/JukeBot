package com.osbornnick.jukebot;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class Song {
    public String key, name, artist, suggestedBy, uri, albumImageURL, albumIconImageURL;
    public Bitmap albumImage, albumIconImage;
    public long duration;
    public long score;
    public String session_id;
    public boolean played = false;
    public boolean deleted = false;
    public boolean playing = false;

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
        if (data.containsKey("artist")) {
            this.artist = (String) data.get("artist");
        }
        if (data.containsKey("played")) {
            this.played = (boolean) data.get("played");
        }
        if (data.containsKey("deleted")) {
            this.deleted = (boolean) data.get("deleted");
        }
        if (data.containsKey("playing")) {
            this.playing = (boolean) data.get("playing");
        }
        if (data.containsKey("albumImageURL")) {
            this.albumImageURL = (String) data.get("albumImageURL");
        }
        if (data.containsKey("albumIconImageURL")) {
            this.albumImageURL = (String) data.get("albumImageURL");
        }
    }

    public Song() {
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

    public String getAlbumImageURL() {
        return albumImageURL;
    }

    @Exclude
    public Bitmap getAlbumImage() {
        try {
            if(albumImage == null) {
                URL url = new URL(albumImageURL);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                albumImage = image;
            }
            return albumImage;
        } catch(IOException e) {
            Log.d("Song", "getAlbumImage: " + e.toString());
            System.out.println(e);
            return null;
        }
    }

    @Exclude
    public Bitmap getAlbumImageIcon() {
        try {
            if(albumIconImage == null) {
                URL url = new URL(albumIconImageURL);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                albumIconImage = image;
            }
            return albumIconImage;
        } catch(IOException e) {
            Log.d("Song", "getAlbumImageIcon: " + e.toString());
            System.out.println(e);
            return null;
        }
    }

    @Exclude
    public long getDuration() {
        return duration;
    }

    public long getScore() {
        return score;
    }

    public String getUri() {return uri;}

    public void setScore(long score) {
        this.score = score;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("Song uri:%s, score:%d", this.uri, this.score);
    }
}
