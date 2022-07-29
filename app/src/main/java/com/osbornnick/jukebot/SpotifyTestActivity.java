package com.osbornnick.jukebot1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SpotifyTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_test);

    }

    @Override
    protected  void onStart() {
        super.onStart();
        Intent i = new Intent(this, SpotifyAuthActivity.class);
        startActivity(i);
    }
}