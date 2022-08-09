package com.osbornnick.jukebot;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AddSongActivity extends AppCompatActivity {
    private static final String TAG = "AddSongActivity";
    private String SESSION_ID = "sessionTest1";
    private String SESSION_NAME = "sessionTest1";
    private boolean admin = true;
    private String authToken, clientID, clientSecret, authURL;
    LocalDateTime authExpiration;

    ImageButton back, leaveSession;
    TextView sessionTitle;
    Button cancelSearch;
    RecyclerView song_rv;
    SearchView searchView;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    AddSongAdapter asAdapter;
    Handler handler;

    HashSet<String> songQueueIDs;
    ArrayList<Song> searchSongList;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        setContentView(R.layout.activity_session_admin);
        Intent i = getIntent();
        SESSION_ID = i.getStringExtra("session_id");
        SESSION_NAME = i.getStringExtra("session_name");
        admin = i.getBooleanExtra("admin", false);
        handler = new Handler(Looper.getMainLooper());

        //find UI items
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        sessionTitle = findViewById(R.id.sessionTitle);
        cancelSearch = findViewById(R.id.cancelSearch);
        song_rv = findViewById(R.id.song_rv);
        searchView = findViewById(R.id.searchView);

        //get clientID and clientSecret
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

        setSpotifyAuthToken();

        //init toolbar
        initToolbar();

        //get songs already in queue
        songQueueIDs = new HashSet<>();
        getSongsInQueue();

        //initialize the recycle view with empty list
        searchSongList = new ArrayList<>();
        song_rv.setLayoutManager(new LinearLayoutManager(this));
        asAdapter = new AddSongAdapter(searchSongList);
        asAdapter.admin = this.admin;
        asAdapter.SESSION_ID = this.SESSION_ID;
        asAdapter.setSongQueue(songQueueIDs);
        song_rv.setAdapter(asAdapter);

        //set cancelSearch onClick
        cancelSearch.setOnClickListener(v -> onBackPressed());

        //update recycler view based on search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                spotifySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        leaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddSongActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getSongsInQueue() {
        db.collection("Session").document(SESSION_ID).collection("queue").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "listen:error", error);
                return;
            }
            if (value == null) return;
            value.getDocumentChanges().forEach(dc -> {
                songQueueIDs.add(dc.getDocument().getId());
                Map<String, Object> data = dc.getDocument().getData();
                Song s = new Song(data);
                s.key = dc.getDocument().getId();
                // update song info from spotify?
                if (s.played) songQueueIDs.remove(s.getKey());
                else if (s.deleted) songQueueIDs.remove(s.getKey());
                else if (s.playing) songQueueIDs.remove(s.getKey());
                else {
                    songQueueIDs.add(s.getKey());
                }

                //update songQueue stored in Adapter
                asAdapter.setSongQueue(songQueueIDs);
                asAdapter.notifyDataSetChanged();

                //log success
                Log.d(TAG, "song queue updated");
            });
        });
    }

    //API Methods
    @RequiresApi(api = Build.VERSION_CODES.O)
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
        searchSongList.clear();

        new Thread(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
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
                        JSONArray respJSON = new JSONObject(resp).getJSONObject("tracks").getJSONArray("items");
                        for(int i = 0; i < respJSON.length(); i++) {
                            JSONObject songJSON = respJSON.getJSONObject(i);
                            /*
                            String songUri = songJSON.getString("uri");
                            String songTitle = songJSON.getString("name");
                            String songArtist = songJSON.getJSONArray("artists").getJSONObject(0).getString("name");
                            String previewURL = songJSON.getString("preview_url");
                            String albumImageURL = songJSON.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                            String albumIconImageURL = songJSON.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url");
                             */

                            Map<String, Object> map = new HashMap<>();
                            map.put("key", songJSON.getString("id"));
                            map.put("name", songJSON.getString("name"));
                            map.put("uri", songJSON.getString("uri"));
                            map.put("artist", songJSON.getJSONArray("artists").getJSONObject(0).getString("name"));
                            map.put("albumImageURL", songJSON.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url"));
                            map.put("albumIconImageURL", songJSON.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url"));
                            Song s = new Song(map);
                            s.session_id = SESSION_ID;

                            searchSongList.add(s);
                        }

                        //update the adapter with new search terms
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                asAdapter.resetSearchResults(searchSongList);
                                asAdapter.notifyDataSetChanged();
                            }
                        });
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