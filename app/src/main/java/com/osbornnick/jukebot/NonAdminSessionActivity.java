package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NonAdminSessionActivity extends AppCompatActivity {
    private static final String TAG = "NonAdminSessionActivity";
    private String SESSION_ID = "sessionTest1";
    private String SESSION_NAME = "Session 1";

    RecyclerView songQueue;
    TextView songTitle, songArtist, queueLabel, disconnectedText, sessionTitle;
    ImageButton back, leaveSession, sessionChat, addFriend;
    ImageView coverArt;
    FloatingActionButton addSongFAB;
    ProgressBar loader;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Handler handler;
    SongQueueAdapter sqAdapter;
    Song currentSong;

//    private SpotifyAppRemote mSpotifyAppRemote;
//    private static final String CLIENT_ID = "fe144966828b41b5ab78f844e0630286";
//    private static final String REDIRECT_URI = "com.osbornnick.jukebot://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_non_admin_session);
        Intent i = getIntent();
        SESSION_ID = i.getStringExtra("session_id");
        SESSION_NAME = i.getStringExtra("session_name");

        handler = new Handler(Looper.getMainLooper());

        songQueue = findViewById(R.id.songQueue);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
//        songLength = findViewById(R.id.songLength);
//        songLengthRemaining = findViewById(R.id.songLengthRemaining);
//        songProgressBar = findViewById(R.id.songProgressBar);
        queueLabel = findViewById(R.id.queueLabel);
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        sessionChat = findViewById(R.id.sessionChat);
        addFriend = findViewById(R.id.addFriend);
        coverArt = findViewById(R.id.coverArt);
        addSongFAB = findViewById(R.id.addSongFAB);
        loader = findViewById(R.id.loader);
        disconnectedText = findViewById(R.id.disconnectedText);
        sessionTitle = findViewById(R.id.sessionTitle);

        //Set Tool Bar On clicks
        initToolbar();

        //initialize the recycle view with empty list
        songQueue.setLayoutManager(new LinearLayoutManager(this));
        sqAdapter = new SongQueueAdapter();
        sqAdapter.admin = false;
        songQueue.setAdapter(sqAdapter);

        listenToSongQueue();
    }

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        //TODO: Update leave session functionality
        leaveSession.setOnClickListener(v -> onBackPressed());

        sessionChat.setOnClickListener(v -> {
            Intent intent = new Intent(NonAdminSessionActivity.this, SessionChatActivity.class);
            intent.putExtra("session_id", SESSION_ID);
            intent.putExtra("session_name", SESSION_NAME);
            startActivity(intent);
        });

        addFriend.setOnClickListener(v -> {
            Intent intent = new Intent(NonAdminSessionActivity.this, InviteFriendsActivity.class);
            intent.putExtra("session_id", SESSION_ID);
            intent.putExtra("session_name", SESSION_NAME);
            startActivity(intent);
        });
    }

    @SuppressLint("NewApi")
    private void listenToSongQueue() {
        db.collection("Session").document(SESSION_ID).collection("queue").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "listen:error", error);
                return;
            }
            if (value == null) return;
            value.getDocumentChanges().forEach(dc -> {
                Map<String, Object> data = dc.getDocument().getData();
                Song s = new Song(data);
                s.key = dc.getDocument().getId();
                // update song info from spotify?
                if (s.played) sqAdapter.remove(s);
                else if (s.deleted) sqAdapter.remove(s);
                else if (s.playing) {
                    //update the current song
                    sqAdapter.remove(s);
                    updateCurrentSong(s);
                } else {
                    s.session_id = SESSION_ID;
                    sqAdapter.add(s);
                }
                Log.d(TAG, s.toString());
            });
        });
    }

    private void updateCurrentSong(Song s) {
        currentSong = s;
        songArtist.setText(s.getArtist());
        songTitle.setText(s.getName());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap albumImage = s.getAlbumImage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(albumImage != null) {
                            coverArt.setImageBitmap(albumImage);
                        } else {
                            coverArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel_24));
                        }
                    }
                });
            }
        }).start();
    }
}