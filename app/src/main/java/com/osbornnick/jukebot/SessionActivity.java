package com.osbornnick.jukebot1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

public class SessionActivity extends AppCompatActivity {

    RecyclerView songQueue;
    TextView songTitle, songArtist, songLength, songLengthRemaining;
    Slider songProgressBar;
    ImageButton skipPrevious, playButton, pauseButton, skipNext;
    ImageView coverArt;
    FloatingActionButton addSongFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        songQueue = findViewById(R.id.songQueue);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songLength = findViewById(R.id.songLength);
        songLengthRemaining = findViewById(R.id.songLengthRemaining);
        songProgressBar = findViewById(R.id.songProgressBar);
        skipPrevious = findViewById(R.id.skipPrevious);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        skipNext = findViewById(R.id.skipNext);
        coverArt = findViewById(R.id.coverArt);
        addSongFAB = findViewById(R.id.addSongFAB);

    }
}