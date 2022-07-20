package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE = 1337;
    private static final String AUTH_TOKEN = "AUTH_TOKEN";
    private static final String CLIENT_ID = "690520ea8148443da28b0dd4555c8ef2";
    private static final String REDIRECT_URI = "com.jukebot://callback";
    private URL mUrl = null;
    private InputStream mInputStream = null;
    private HttpURLConnection conn = null;
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView tv_play = (TextView) findViewById(R.id.start_session);
        ImageView img_settings = (ImageView) findViewById(R.id.personal_settings);

        tv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
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
                    token = response.getAccessToken();
                    getUserProfile();
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

    public void getUserProfile() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                //Background work here
                doInBackground("https://api.spotify.com/v1/me");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        //onPostExecute(result);
                    }
                });
            }
        });
    }

    public String doInBackground(String... strings){

        return "";
    }

}