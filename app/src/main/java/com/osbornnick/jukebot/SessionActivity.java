package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

//Spotify SDK Imports
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.osbornnick.jukebot.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SessionActivity extends AppCompatActivity {
    private static final String TAG = "SessionActivity";
    private String SESSION_ID;

    RecyclerView songQueue;
    TextView songTitle, songArtist, songLength, songLengthRemaining, queueLabel, disconnectedText;
    SeekBar songProgressBar;
    ImageButton skipPrevious, playButton, pauseButton, skipNext, back, leaveSession, sessionChat, sessionSettings, addFriend;
    ImageView coverArt;
    FloatingActionButton addSongFAB;
    ProgressBar loader;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    String spotifyAuthToken;
    String loggedInUser = "dhj36@cornell.edu";
    String sessionAdminUser = "dhj36@cornell.edu";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Handler handler;
    SongQueueAdapter sqAdapter;
    List<Song> songList;

    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String CLIENT_ID = "c810b4c8bcb7429db3df246adc2e50c0";
    private static final String REDIRECT_URI = "com.jukebot://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        spotifyAuthToken = preferences.getString("token", "");

        setContentView(R.layout.activity_session);
        handler = new Handler(Looper.getMainLooper());

        songQueue = findViewById(R.id.songQueue);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songLength = findViewById(R.id.songLength);
        songLengthRemaining = findViewById(R.id.songLengthRemaining);
        queueLabel = findViewById(R.id.queueLabel);
        disconnectedText = findViewById(R.id.disconnectedText);
        songProgressBar = findViewById(R.id.songProgressBar);
        skipPrevious = findViewById(R.id.skipPrevious);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        skipNext = findViewById(R.id.skipNext);
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        sessionChat = findViewById(R.id.sessionChat);
        sessionSettings = findViewById(R.id.sessionSettings);
        addFriend = findViewById(R.id.addFriend);
        coverArt = findViewById(R.id.coverArt);
        addSongFAB = findViewById(R.id.addSongFAB);
        loader = findViewById(R.id.loader);

        if(spotifyAuthToken == "") {
            songQueue.setVisibility(View.INVISIBLE);
            songTitle.setVisibility(View.INVISIBLE);
            songArtist.setVisibility(View.INVISIBLE);
            songLength.setVisibility(View.INVISIBLE);
            songLengthRemaining.setVisibility(View.INVISIBLE);
            queueLabel.setVisibility(View.INVISIBLE);
            songProgressBar.setVisibility(View.INVISIBLE);
            skipPrevious.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.INVISIBLE);
            skipNext.setVisibility(View.INVISIBLE);
            coverArt.setVisibility(View.INVISIBLE);
            addSongFAB.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.INVISIBLE);

            disconnectedText.setVisibility(View.VISIBLE);
        }

        //Set Tool Bar On clicks
        setToolBarOnClickListeners();

        //Set Spotify Controls
        spotifySongControls();

        //firebase read tester
        readData();

        //initialize the recycle view with empty list
        songList = new ArrayList<>();
        songQueue.setLayoutManager(new LinearLayoutManager(this));
        sqAdapter = new SongQueueAdapter(songList);
        songQueue.setAdapter(sqAdapter);

        //update song queue - also updates current song
        songQueueUpdate();
    }

    private void setToolBarOnClickListeners() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //TODO: Update leave session functionality
        leaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sessionChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SessionActivity.this, SessionChatActivity.class);
                intent.putExtra("session_id", SESSION_ID);
                startActivity(intent);
            }
        });

        //maybe change from Activity to AlertDialog
        sessionSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SessionActivity.this, SessionSettingsActivity.class);
                intent.putExtra("session_id", SESSION_ID);
                startActivity(intent);
            }
        });

        /*
        dialogBuilder = new AlertDialog.Builder(this);
        final View urlPopupView = getLayoutInflater().inflate(R.layout.link_popup, null);
        urlName = (EditText) urlPopupView.findViewById(R.id.urlName);
        newUrl = (EditText) urlPopupView.findViewById(R.id.newUrl);
        save = (Button) urlPopupView.findViewById(R.id.urlSubmitButton);
        cancel = (Button) urlPopupView.findViewById(R.id.urlCancelButton);

        dialogBuilder.setView(urlPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        Context thisContext = this;
         */

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SessionActivity.this, InviteFriendsActivity.class);
                intent.putExtra("session_id", SESSION_ID);
                startActivity(intent);
            }
        });
    }

    private void readData() {
        Log.d(TAG, "readData: starting");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DocumentReference docRef = db.collection("Session").document("sessionTest1");
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

                DocumentReference docRefQueue = db.collection("songQueue").document("sessionTest1");
                docRefQueue.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        }).start();
        Log.d(TAG, "readData: call complete");
    }

    private void songQueueUpdate() {
        List<Song> songList = new ArrayList<>();
        //grab song queue from Firebase
        final DocumentReference docRef = db.collection("songQueu").document(SESSION_ID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        //update songQueue Recycler View
        songQueue.setLayoutManager(new LinearLayoutManager(this));
        sqAdapter = new SongQueueAdapter(songList);
        songQueue.setAdapter(sqAdapter);

        //update Current Song player. set to play if not already playing (check the spotify player

    }

    private void spotifySongControls() {
        //show spotify controls only for admin user
        if(loggedInUser != sessionAdminUser) {
            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.INVISIBLE);
            skipPrevious.setVisibility(View.INVISIBLE);
            skipNext.setVisibility(View.INVISIBLE);
        } else {
            //set appropriate onClickListeners utilizing Spotify SDK
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //resume currently playing song in the Spotify player
                    mSpotifyAppRemote.getPlayerApi().resume();
                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                }
            });

            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //pause currently playing song in the Spotify player
                    mSpotifyAppRemote.getPlayerApi().pause();
                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                }
            });

            skipNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSpotifyAppRemote.getPlayerApi().skipNext();
                }
            });

            skipPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                }
            });
        }
    }


    /*
        onStart(), onStop(), connected() code sourced from Spotify's Quick start guide: https://developer.spotify.com/documentation/android/quick-start/
        Modifications may have been made compared to source code
     */
    @Override
    protected void onStart() {
        super.onStart();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d(TAG, "Connected to Spotify");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    private void connected() {
//         Play a playlist
//        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d(TAG, track.name + " by " + track.artist.name);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}