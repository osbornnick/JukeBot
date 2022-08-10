package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        sendToFirestoreDB(token);
                        Log.d(TAG, "Token: " + token);
                       // Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendToFirestoreDB(String token) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);

        db.collection("users").document(user.getUid()).set(tokenData, SetOptions.merge());
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
        intent.putExtra("session_id", "sessionTest1");
        intent.putExtra("session_name", "Session 1");
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void testAuth(View view) {
        Intent intent = new Intent(this, SpotifyAuthActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addSong(View view) {
        Intent intent = new Intent(this, AddSongActivity.class);
        intent.putExtra("session_id", "8DyHL0nZYAeWFkqcRfVE1wzLSoJ2");
        intent.putExtra("session_name", "Yuna's Session");
        intent.putExtra("admin", true);
        startActivity(intent);
    }

    public void inviteFriend(View view) {
        Intent intent = new Intent(this, InviteFriendsActivity.class);
        intent.putExtra("session_id", "8DyHL0nZYAeWFkqcRfVE1wzLSoJ2");
        intent.putExtra("session_name", "Yuna's Session");
        startActivity(intent);
    }
}