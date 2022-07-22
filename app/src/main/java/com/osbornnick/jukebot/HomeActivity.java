package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;
    private String token = null;
    private URL mUrl = null;
    private HttpURLConnection conn = null;
    private InputStream mInputStream = null;
    private String error = null;
    private String result = null;
    private String display_name = null;
    private TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView tv_play = (TextView) findViewById(R.id.start_session);
        ImageView img_settings = (ImageView) findViewById(R.id.personal_settings);
        mName = (TextView) findViewById(R.id.txt_displayName);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("Name", "");
        if(!name.equalsIgnoreCase(""))
        {
            name = "Hello " + name;
        }
        Log.d(TAG, "onCreate: displayname" + display_name);
        mName.setText(name);

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
        builder.setScopes(new String[]{"user-read-email","user-read-private"});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
        //AuthorizationClient.clearCookies(context);
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
                    editor = getSharedPreferences("Spotify",0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
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

    public void getUserProfile(){
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
                        onPostExecute(result);
                    }
                });
            }
        });
    }

    public String doInBackground(String... strings){
        try {
            mUrl = new URL(strings[0]);
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoInput(true);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                error = "Server returned HTTP " + conn.getResponseCode() + " " + conn.getResponseMessage();
                Log.d(TAG, "doInBackground: " + "Server returned HTTP " + conn.getResponseCode() + " " + conn.getResponseMessage());
                return null;
            }
            mInputStream = conn.getInputStream();
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
                String len;
                while ((len = bufferedReader.readLine()) != null) {
                    sb.append(len);
                }
                bufferedReader.close();
                result = sb.toString().replace(",", ",\n");
                Log.d(TAG, "doInBackground: " + result);
                conn.disconnect();
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            conn.disconnect();
            return "";

        } catch (Exception e){
            return e.toString();
        }
    }

    public void onPostExecute(String result) {

        if (result == null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject obj = new JSONObject(result);
            Log.d(TAG, "onPostExecute: " + obj);
            display_name = obj.getString("display_name");
            String email = obj.getString("email");
            String country = obj.getString("country");
            Log.d(TAG, "onPostExecute: display_name " + display_name );
            Log.d(TAG, "onPostExecute: email " + email );
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Name",display_name);
            editor.putString("Email",email);
            editor.putString("Country",country);
            editor.apply();
            Intent intent = new Intent(HomeActivity.this, StartSessionActivity.class);
            intent.putExtra("display_name",display_name);
            intent.putExtra("email", email);
            startActivity(intent);


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().remove("Name").apply();
                AuthorizationClient.clearCookies(HomeActivity.this);
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}