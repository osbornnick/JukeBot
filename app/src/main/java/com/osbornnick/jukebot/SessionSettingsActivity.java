package com.osbornnick.jukebot1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SessionSettingsActivity extends AppCompatActivity {

    private static final String TAG = "SessionSettingsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_settings);

        Button btn_deviceSearch = findViewById(R.id.deviceSearch);
        btn_deviceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SessionSettingsActivity.this, BluetoothActivity.class);
                startActivity(intent);
                Log.d(TAG, "onClick: clicking device search");
            }
        });
    }
}