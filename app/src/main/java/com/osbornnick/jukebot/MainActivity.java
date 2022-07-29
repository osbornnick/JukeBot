package com.osbornnick.jukebot1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = new Intent(this, SpotifyTestActivity.class);
        startActivity(i);
    }

    // temp code
    public void Home(View view){
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void SessionSettings(View view){
        Intent intent = new Intent(MainActivity.this, SessionSettingsActivity.class);
        startActivity(intent);
    }


}