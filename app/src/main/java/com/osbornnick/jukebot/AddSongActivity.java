package com.osbornnick.jukebot;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class AddSongActivity extends AppCompatActivity {
    private static final String TAG = "AddSongActivity";
    private String SESSION_ID = "sessionTest1";
    private String SESSION_NAME = "sessionTest1";
    private boolean admin = true;
    private String spotifyAuthToken;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        asAdapter.setSongQueue(songQueueIDs);
        song_rv.setAdapter(asAdapter);


    }

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        //TODO: Update leave session functionality
        leaveSession.setOnClickListener(v -> onBackPressed());
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
}