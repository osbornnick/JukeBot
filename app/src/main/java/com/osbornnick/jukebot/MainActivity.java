package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void SpotifyAuth(View view) {
        Intent i = new Intent(this, SpotifyTestActivity.class);
        startActivity(i);
    }

    public void login(View view) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void createAccount(View view) {
        Intent i = new Intent(this, CreateAccountActivity.class);
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

    public void SessionChat(View view){
        Intent intent = new Intent(MainActivity.this, SessionChatActivity.class);
        startActivity(intent);
    }


}