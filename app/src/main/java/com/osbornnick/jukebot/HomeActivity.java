package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE = 1337;
    private static final String AUTH_TOKEN = "AUTH_TOKEN";
    private static final String CLIENT_ID = "690520ea8148443da28b0dd4555c8ef2";
    private static final String REDIRECT_URI = "com.jukebot://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView tv_play = (TextView) findViewById(R.id.start_session);
        TextView tv_join = (TextView) findViewById(R.id.join_group);

        ImageView img_settings = (ImageView) findViewById(R.id.personal_settings);

        tv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        tv_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.join_group) {
                    getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).replace(R.id.action_bar_container, new JoinSessionFragment()).commit();
                }
            }
        });

        img_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PersonalSettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void authenticate(){
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-email"});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE){
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
            switch (response.getType()){
                case TOKEN:
                    Log.d(TAG, "onActivityResult: " + response.getAccessToken());
                    Intent intent = new Intent(HomeActivity.this, StartSessionActivity.class);
                    intent.putExtra(AUTH_TOKEN, response.getAccessToken());
                    startActivity(intent);
                    destroy();
                    break;
                case ERROR:
                    Log.d(TAG, "onActivityResult: " + response.getError());
                    break;
                default:
                    Log.d(TAG, "onActivityResult: " + response.getType());
            }
        }
    }

    public void destroy(){
        HomeActivity.this.finish();
    }


}