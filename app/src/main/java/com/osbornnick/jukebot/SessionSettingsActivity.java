package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SessionSettingsActivity extends AppCompatActivity {

    private static final String TAG = "SessionSettingsActivity";
    private String SESSION_ID;
    private String SESSION_NAME;

    TextView sessionTitle;
    ImageButton back, leaveSession;
    SwitchCompat joinSessionSwitch, toggleFriendInvite, toggleChat;
    Button cancel, save;

    Map<String, Object> state;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_settings);

        Intent i = getIntent();
        SESSION_ID = i.getStringExtra("session_id");
        SESSION_NAME = i.getStringExtra("session_name");

        sessionTitle = findViewById(R.id.sessionTitle);
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        joinSessionSwitch = findViewById(R.id.joinSessionSwitch);
        toggleFriendInvite = findViewById(R.id.toggleFriendInvite);
        toggleChat = findViewById(R.id.toggleChat);
        cancel = findViewById(R.id.cancel);
        save = findViewById(R.id.save);


        state = new HashMap<String, Object>();

        //initialize toolbar actions
        initToolbar();

        //initialize settings and set listeners
        initSettings();
    }

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        leaveSession.setOnClickListener(v -> {
            Intent i = new Intent(SessionSettingsActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }

    private void initSettings() {
        //read settings state
        DocumentReference docRef = db.collection("Session").document(SESSION_ID);
        docRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "listen:error", error);
                return;
            }
            if (value == null) return;
            joinSessionSwitch.setChecked((boolean) value.get("allowJoins"));
            toggleFriendInvite.setChecked((boolean) value.get("allowInvite"));
            toggleChat.setChecked((boolean) value.get("allowChat"));
            Log.d(TAG, "Session Settings updated");
        });

        //set change & click listeners
        joinSessionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                state.put("allowJoins", joinSessionSwitch.isChecked());
            }
        });

        toggleFriendInvite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                state.put("allowInvite", toggleFriendInvite.isChecked());
            }
        });

        toggleChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                state.put("allowChat", toggleChat.isChecked());
            }
        });

        cancel.setOnClickListener(v -> onBackPressed());

        save.setOnClickListener(v -> {
            db.collection("Session").document(SESSION_ID).update(state);
            onBackPressed();
        });
    }
}