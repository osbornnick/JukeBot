package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView name = findViewById(R.id.txt_usernameDisplay);
        TextView email = findViewById(R.id.txt_emailDisplay);
        TextView country = findViewById(R.id.txt_countryDisplay);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString("Name", "");
        String spotify_email = preferences.getString("Email","");
        String countryCode = preferences.getString("Country","");

        name.setText(username);
        email.setText(spotify_email);
        country.setText(countryCode);

    }
}