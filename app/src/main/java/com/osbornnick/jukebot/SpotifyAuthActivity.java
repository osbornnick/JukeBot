package com.osbornnick.jukebot;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONObject;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.JsonParser;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SpotifyAuthActivity extends AppCompatActivity {
    private static final String TAG = "TestAuthActivity";

    Button button6, button7;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RequestQueue mRequestQueue;
    String clientID, clientSecret;
    String authURL;
    String authToken;
    LocalDateTime authExpiration;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_auth);

        mRequestQueue = Volley.newRequestQueue(this);
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
                        //String Request initialized
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, authURL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    authToken = json.getString("access_token");
                                    authExpiration = LocalDateTime.now().plusSeconds((long) json.getInt("expires_in"));
                                    Log.i(TAG, authToken + " ; Expires at " + authExpiration.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.i(TAG, e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("grant_type", "client_credentials");
                                params.put("client_id", clientID);
                                params.put("client_secret", clientSecret);
                                return params;

                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<String, String>();
                                headers.put("Accept", "application/json");
                                headers.put("Content-Type", "application/x-www-form-urlencoded");
                                return headers;
                            }
                        };

                        mRequestQueue.add(stringRequest);
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
                    //String Request initialized
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, searchURL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d(TAG, response);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i(TAG, e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.toString());
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("type", "track,artist,album");
                            params.put("limit", "15");
                            params.put("include_external", "audio");
                            params.put("query", query);
                            return params;

                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Log.d(TAG, "Authenticating with token: " + authToken);
                            Map<String, String> headers = new HashMap<String, String>();
                            headers.put("Authorization", "Bearer " + authToken);
                            return headers;
                        }
                    };

                    mRequestQueue.add(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
            }
        }).start();
    }

}
