package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartSessionActivity extends AppCompatActivity {
    FirebaseUser user;
    private static final String TAG = "StartSessionActivity";
    FirebaseFirestore db;
    String username = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_session);

        TextView name = findViewById(R.id.txt_loggedInName);
        TextView email = findViewById(R.id.txt_email);
        db = FirebaseFirestore.getInstance();

        email.setText("You are the host now");
        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        Log.d(TAG, "onCreate: " + uid);

        db.collection("users")
                .document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            username = task.getResult().getString("username");
                            Log.d(TAG, "onComplete: username " + username);
                            name.setText("username: " + username);
                        }
                    }
                });

    }



}