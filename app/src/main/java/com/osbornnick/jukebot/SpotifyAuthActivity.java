package com.osbornnick.jukebot;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.JsonParser;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SpotifyAuthActivity extends AppCompatActivity {
    private static final String TAG = "TestAuthActivity";

    Button button6, button7;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String clientID, clientSecret;
    String authURL;
    String authToken;
    LocalDateTime authExpiration;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_auth);

        button6 = findViewById(R.id.button6);
        button6.setOnClickListener(v -> {
            setSpotifyAuthToken();
        });

        button7 = findViewById(R.id.button7);
        button7.setOnClickListener(v -> {
            spotifySearch("flower");
        });


        db.collection("meta").document("adminUser").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    Log.d(TAG, "onEvent: " + error.toString());
                    return;
                }

                if (value == null) return;
                Map<String, Object> data = value.getData();
                clientID = (String) data.get("clientID");
                clientSecret = (String) data.get("clientSecret");
                authURL = (String) data.get("authURL");

                Log.d(TAG, "client params retrieved successfully");
            }
        });
    }

    public void setSpotifyAuthToken() {
        if (authToken == null || authExpiration == null || LocalDateTime.now().isBefore(authExpiration)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Uri.Builder builder = new Uri.Builder()
                                .appendQueryParameter("grant_type", "client_credentials")
                                .appendQueryParameter("client_id", clientID)
                                .appendQueryParameter("client_secret", clientSecret);
                        String q = builder.build().getQuery();

                        URL url = new URL(authURL + "?" + q);
                        Log.d(TAG, "run: " + url.toString());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty ("Accept", "application/json");
                        conn.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
                        conn.setDoInput(true);

                        // Read response.
                        int responseCode = conn.getResponseCode();
                        String resp;
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream = conn.getInputStream();
                            resp = convertStreamToString(inputStream);
                            try {
                                JSONObject json = new JSONObject(resp);
                                authToken = json.getString("access_token");
                                authExpiration = LocalDateTime.now().plusSeconds((long) json.getInt("expires_in"));
                                Log.i(TAG, authToken + " ; Expires at " + authExpiration.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i(TAG, e.toString());
                            }
                        } else {
                            resp = null;
                        }

                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, e.toString());
                    }
                }
            }).start();
        }
    }

    public void spotifySearch(String query) {
        String searchURL = "https://api.spotify.com/v1/search";
//        setSpotifyAuthToken();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("type", "track")
                            .appendQueryParameter("limit", "15")
                            .appendQueryParameter("include_external", "audio")
                            .appendQueryParameter("query", query);
                    String q = builder.build().getQuery();

                    URL url = new URL(searchURL + "?" + q);
                    Log.d(TAG, "run: " + url.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty ("Authorization", "Bearer " + authToken);
                    conn.setDoInput(true);

                    // Read response.
                    int responseCode = conn.getResponseCode();
                    String resp;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = conn.getInputStream();
                        resp = convertStreamToString(inputStream);
                        Log.d(TAG, resp);
                    } else {
                        resp = null;
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
            }
        }).start();
    }

    public static String convertStreamToString(InputStream inputStream){
        StringBuilder stringBuilder=new StringBuilder();
        try {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String len;
            while((len=bufferedReader.readLine())!=null){
                stringBuilder.append(len);
            }
            bufferedReader.close();
            return stringBuilder.toString().replace(",", ",\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
