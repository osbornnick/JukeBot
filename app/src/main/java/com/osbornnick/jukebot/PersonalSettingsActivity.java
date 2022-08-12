package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class PersonalSettingsActivity extends AppCompatActivity {

    private Switch sw_others;
    private Button signOut;
    private static final String TAG = "PersonalSettingsActivit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_settings);
        TextView txt_userProfile = (TextView) findViewById(R.id.txt_userProfile);
        //sw_others = findViewById(R.id.sw_others);
        sw_others.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Toast.makeText(PersonalSettingsActivity.this,"toggle is on", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PersonalSettingsActivity.this,"toggle is off", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        txt_userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalSettingsActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });

        signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            //send back to login
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "login: calling");
            startActivity(i);
        });
    }



}