package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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
        Log.d(TAG, "login: calling" );
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
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(MainActivity.this, "Please login to join the chat", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, SessionChatActivity.class);
        startActivity(intent);
    }

    public void launchSession(View view) {
        Intent intent = new Intent(this, SessionAdminActivity.class);
        startActivity(intent);
    }

    public void launchNonAdminSession(View view) {
        Intent intent = new Intent(this, NonAdminSessionActivity.class);
        intent.putExtra("session_id", "sessionTest1");
        intent.putExtra("session_name", "Session 1");
        startActivity(intent);
    }
}