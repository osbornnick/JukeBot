package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    TextView txt_username, txt_date;
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        txt_username = findViewById(R.id.txt_username);
        txt_date = findViewById(R.id.txt_date);

        getUserInfo();
    }

    private void getUserInfo(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    String username = task.getResult().getString("username");
                    String date = task.getResult().getTimestamp("dateCreated").toDate().toString();
                    txt_username.setText(username);
                    txt_date.setText(date);

                }
            }
        });
    }
}