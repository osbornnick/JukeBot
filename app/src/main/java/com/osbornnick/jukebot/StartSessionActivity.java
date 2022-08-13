package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StartSessionActivity extends AppCompatActivity {
    FirebaseUser user;
    private static final String TAG = "StartSessionActivity";
    FirebaseFirestore db;
    String username = null;
    EditText mSessionName;
    Button btn_confirm, btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_session);

        // initialize UI elements
        mSessionName = findViewById(R.id.session_Name);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);

        db = FirebaseFirestore.getInstance();


        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        Log.d(TAG, "onCreate: " + uid);


        // cancel btn brings up to previous activity
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSessionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                btn_confirm.setClickable(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0){
                    btn_confirm.setClickable(false);
                } else {
                    btn_confirm.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // confirm opens new activity
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSessionName.getText().length() == 0){
                    Toast.makeText(StartSessionActivity.this,"Please enter session name", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    String sessionName = mSessionName.getText().toString();
                    addSessionName(sessionName);
                    Log.d(TAG, "onClick: " + sessionName);
                    //mSessionName.setText("");
                    Intent intent = new Intent(StartSessionActivity.this, SessionAdminActivity.class);
                    intent.putExtra("session_id", user.getUid());
                    intent.putExtra("session_name", sessionName);
                    startActivity(intent);
                }
            }
        });


    }

    // add session name to the database
    private void addSessionName(String sessionName){
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", sessionName);
        userData.put("adminUser", user.getUid());
        userData.put("allowChat", true);
        userData.put("allowInvite", true);
        userData.put("allowJoins", true);

        // add session name
        db.collection("Session").document(user.getUid()).set(userData);

        Map<String, Object> queueData = new HashMap<>();
        queueData.put("deleted",true);
        queueData.put("played",false);
        queueData.put("playing",false);
        // set queue collection
        db.collection("Session").document(user.getUid()).collection("queue").document("initialSong").set(queueData);
    }



}