package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
//Spotify SDK Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SessionAdminActivity extends AppCompatActivity {
    private static final String TAG = "SessionAdminActivity";
    private String SESSION_ID;
    private String SESSION_NAME;

    RecyclerView songQueue;
    TextView songTitle, songArtist, queueLabel, disconnectedText, sessionTitle, noSongText;
    ImageButton playButton, pauseButton, back, leaveSession, sessionChat, sessionSettings, addFriend;
    ImageView coverArt;
    FloatingActionButton addSongFAB;
    ProgressBar loader;
    Handler uiHandler = new Handler(Looper.getMainLooper());

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SongQueueAdapter sqAdapter;
    Song currentSong;

    //Music playback service properties
    private SongPlaybackService playerService;
    private boolean musicPlaying = false;
    private boolean songLoaded = false;
    private boolean playerServiceBound = false;
    Intent serviceIntent;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SongPlaybackService.SongPlaybackBinder binder = (SongPlaybackService.SongPlaybackBinder) service;
            playerService = binder.getService();
            playerServiceBound = true;
            Log.d(TAG, "SongPlaybackService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            playerServiceBound = false;
            Log.d(TAG, "SongPlaybackService disconnected");
        }
    };
    private ServiceUpdateReceiver serviceUpdateReceiver;

//    private SpotifyAppRemote mSpotifyAppRemote;
//    private static final String CLIENT_ID = "fe144966828b41b5ab78f844e0630286";
//    private static final String REDIRECT_URI = "com.osbornnick.jukebot://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_admin);
        Intent i = getIntent();
        SESSION_ID = i.getStringExtra("session_id");
        SESSION_NAME = i.getStringExtra("session_name");
        //start Playback service as STICKY so it doesn't close when activity is pause
        serviceIntent = new Intent(this, SongPlaybackService.class);
        startService(serviceIntent);


        songQueue = findViewById(R.id.songQueue);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        queueLabel = findViewById(R.id.queueLabel);
        disconnectedText = findViewById(R.id.disconnectedText);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        sessionChat = findViewById(R.id.sessionChat);
        sessionSettings = findViewById(R.id.sessionSettings);
        addFriend = findViewById(R.id.addFriend);
        coverArt = findViewById(R.id.coverArt);
        addSongFAB = findViewById(R.id.addSongFAB);
        loader = findViewById(R.id.loader);
        sessionTitle = findViewById(R.id.sessionTitle);
        noSongText = findViewById(R.id.noSongText);

        //Set Tool Bar On clicks
        initToolbar();

        //Set Spotify Controls
        initSongControls();

        //initialize the recycle view with empty list
        songQueue.setLayoutManager(new LinearLayoutManager(this));
        sqAdapter = new SongQueueAdapter();
        songQueue.setAdapter(sqAdapter);

        songQueue.addItemDecoration(new DividerItemDecoration(songQueue.getContext(), ((LinearLayoutManager)songQueue.getLayoutManager()).getOrientation()));

        listenToSongQueue();

        //onClickListener for FAB
        addSongFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SessionAdminActivity.this, AddSongActivity.class);
                intent.putExtra("session_id", SESSION_ID);
                intent.putExtra("session_name", SESSION_NAME);
                intent.putExtra("admin", true);
                startActivity(intent);
            }
        });
    }

    private void initToolbar() {
        back.setOnClickListener(v -> onBackPressed());

        sessionTitle.setText(SESSION_NAME);

        leaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SessionAdminActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        sessionChat.setOnClickListener(v -> {
            Intent intent = new Intent(SessionAdminActivity.this, SessionChatActivity.class);
            intent.putExtra("session_id", SESSION_ID);
            startActivity(intent);
        });

        //maybe change from Activity to AlertDialog
        sessionSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SessionAdminActivity.this, SessionSettingsActivity.class);
            intent.putExtra("session_id", SESSION_ID);
            startActivity(intent);
        });

        addFriend.setOnClickListener(v -> {
            Intent intent = new Intent(SessionAdminActivity.this, InviteFriendsActivity.class);
            intent.putExtra("session_id", SESSION_ID);
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
                List<String> upVotes = (List<String>) data.get("upVotes");
                List<String> downVotes = (List<String>) data.get("downVotes");
                if (upVotes != null && upVotes.contains(currentUser.getUid())) s.voted = "UP";
                if (downVotes != null && downVotes.contains(currentUser.getUid())) s.voted = "DOWN";
                // update song info from spotify?
                Log.d("Song update heard", s.toString());
                if (s.played) sqAdapter.remove(s);
                else if (s.deleted) sqAdapter.remove(s);
                else if (s.playing) sqAdapter.remove(s);
                else {
                    s.session_id = SESSION_ID;
                    sqAdapter.add(s);
                }
                Log.d(TAG, s.toString());
            });
        });
    }

    private void initSongControls() {
        //show spotify controls only for admin user
            //set appropriate onClickListeners utilizing Spotify SDK
            playButton.setOnClickListener(v -> {
                if(!musicPlaying) {
                    if(songLoaded) {    //resume song if one isn't set
                        playerService.resume();
                    } else {
                        String nextSong = getNextFromQueue();
                        if(nextSong != null) {
                            playerService.setSession(SESSION_ID);
                            playerService.setDataSource(nextSong);
                            songLoaded = true;
                        } else {
                            Log.d(TAG, "Queue is empty");
                            Toast.makeText(this, "Add more songs to the queue to continue your session!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                    musicPlaying = true;
                }
            });

            pauseButton.setOnClickListener(v -> {
                if(musicPlaying) {
                    playerService.pause();
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.INVISIBLE);
                    musicPlaying = false;
                }
            });
    }

    private void playNextSong() {
        //stop current song if playing
        if(musicPlaying) {
            playerService.pause();
            musicPlaying = false;
            Log.d(TAG, "playNextSong: last song paused");
        };

        //update currentSong in firestore
        db.collection("Session").document(SESSION_ID).collection("queue").document(currentSong.getKey()).update("playing", false);
        Log.d(TAG, "playNextSong: playing set to false");
        db.collection("Session").document(SESSION_ID).collection("queue").document(currentSong.getKey()).update("played", true);
        Log.d(TAG, "playNextSong: played set to true");

        //play next song in queue
        String nextSong = getNextFromQueue();
        Log.d(TAG, "playNextSong: next song found");
        if(nextSong != null) {
            playerService.setSession(SESSION_ID);
            playerService.setDataSource(nextSong);

            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            songLoaded = true;
            musicPlaying = true;
            Log.d(TAG, "playNextSong: next song played successfully");
        } else {
            clearCurrentSong();
            Toast.makeText(this, "Add more songs to the queue to continue your session!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "playNextSong: queue was empty");
        }
    }

    private String getNextFromQueue() {
        // update currentSong from playing to played
        if (currentSong != null) {
            Map<String, Object> endSong = new HashMap<String, Object>() {{
                put("playing", false);
                put("played", true);
            }};
            db.collection("Session").document(SESSION_ID).collection("queue").document(currentSong.getKey()).update(endSong);
        }

        //update nextSong to playing
        Song nextSong = sqAdapter.getFirst();

        // update nextSong to be played
        if (nextSong == null) {
            return null;
        } else {
            db.collection("Session").document(SESSION_ID).collection("queue").document(nextSong.getKey()).update("playing", true);
            updateCurrentSong(nextSong);
            return nextSong.getPreviewURL();
        }
    }

    private void updateCurrentSong(Song s) {
        if(s == null) return;
        //change out current song view items
        songArtist.setVisibility(View.VISIBLE);
        songTitle.setVisibility(View.VISIBLE);
        coverArt.setVisibility(View.VISIBLE);
        noSongText.setVisibility(View.GONE);

        currentSong = s;
        songArtist.setText(s.getArtist());
        songTitle.setText(s.getName());
        new Thread(() -> {
            Bitmap albumImage = s.getAlbumImage();
            uiHandler.post(() -> {
                if (albumImage != null) coverArt.setImageBitmap(albumImage);
                else coverArt.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_cancel_24, null));
            });
        }).start();
    }

    private void clearCurrentSong() {
        currentSong = null;
        musicPlaying = false;
        songLoaded = false;
        songArtist.setVisibility(View.GONE);
        songTitle.setVisibility(View.GONE);
        coverArt.setVisibility(View.GONE);
        noSongText.setVisibility(View.VISIBLE);
        Log.d(TAG, "clearCurrentSong: current song is cleared out");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SongPlaybackService.class));
        if(serviceUpdateReceiver != null) {
            unregisterReceiver(serviceUpdateReceiver);
            Log.d(TAG, "onResume: broadcast receiver unregistered");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        playerServiceBound = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        playerServiceBound = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(serviceUpdateReceiver == null) {
            serviceUpdateReceiver = new ServiceUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter("com.osbornnick.jukebot||" + SESSION_ID + "||completed");
            registerReceiver(serviceUpdateReceiver, intentFilter);
            Log.d(TAG, "onResume: broadcast receiver registered");
        }
    }

    private class ServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.osbornnick.jukebot||" + SESSION_ID + "||completed")) {
                //play next song in queue
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                musicPlaying = false;
                songLoaded = false;
                Log.d(TAG, "onReceive: song completed broadcast received");

                //start next song
                playNextSong();
            }
        }
    }
}