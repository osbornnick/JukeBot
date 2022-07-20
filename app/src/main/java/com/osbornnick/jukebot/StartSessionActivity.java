package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class StartSessionActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_session);

        TextView name = findViewById(R.id.txt_loggedInName);
        TextView email = findViewById(R.id.txt_email);

        String display_name = getIntent().getStringExtra("display_name");
        String spotify_email = getIntent().getStringExtra("email");

        name.setText(display_name);
        email.setText(spotify_email);

    }
}