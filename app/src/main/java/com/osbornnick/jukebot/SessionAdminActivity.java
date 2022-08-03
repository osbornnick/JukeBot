package com.osbornnick.jukebot;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

//Spotify SDK Imports
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firestore.v1.WriteResult;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
//        loggedInUser = preferences.getString("login", "");

        setContentView(R.layout.activity_session);
        Intent i = getIntent();
//        sessionAdminUser = i.getStringExtra("sessionAdminUser");
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

    private void songTransition() {

    }

    private void songQueueUpdate() {
        //grab song queue from Firebase
        final DocumentReference docRef = db.collection("songQueue").document(SESSION_ID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> document = (Map<String, Object>) snapshot.getData();
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    songList.clear();
                    assert document != null;
                    for(String songURI : document.keySet()) {
                        Map<String, Object> songDetails = (Map<String, Object>) document.get(songURI);
                        String songTitle = (String) songDetails.get("songTitle");
                        String songArtist = (String) songDetails.get("songArtist");
                        String suggestedBy = (String) songDetails.get("suggestedBy");
                        boolean anonymous = (boolean) songDetails.get("anonymous");
                        long duration = (long) songDetails.get("duration");
                        int score = (int) songDetails.get("score");

                        //get album cover
                        ImageUri albumImageURI = new ImageUri((String) songDetails.get("albumCover"));
                        CallResult<Bitmap> albumImageCall = mSpotifyAppRemote.getImagesApi().getImage(albumImageURI, Image.Dimension.MEDIUM);
                        Result<Bitmap> albumImageResult = albumImageCall.await(10, TimeUnit.SECONDS);
                        Bitmap albumImage = null;
                        if (albumImageResult.isSuccessful()) {
                            albumImage = albumImageResult.getData();
                            Log.d(TAG, "onEvent: album image could recovered successfully");

                        } else {
                            Log.d(TAG, "onEvent: album image could not be recovered");
                        }

                        Song addSong = new Song(songURI, songTitle, songArtist, suggestedBy, albumImage, duration, score, anonymous);
                        songList.add(addSong);
                    }
                    //all songs added. now sort list
                    songList.sort((o1, o2) -> Integer.compare(o1.getScore(), o2.getScore()));
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

        if(loggedInUser == sessionAdminUser) {
            ConnectionParams connectionParams =
                    new ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build();
            SpotifyAppRemote.connect(this, connectionParams,
                    new Connector.ConnectionListener() {

                        @RequiresApi(api = Build.VERSION_CODES.N)
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
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void connected() {
//         Play a playlist
//        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState to check for a song ending
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d(TAG, track.name + " by " + track.artist.name);
                    }

                    handleTrackEnded();
                });
    }

    private boolean trackWasStarted = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleTrackEnded() {
        CallResult<PlayerState> playerStateCall = mSpotifyAppRemote.getPlayerApi().getPlayerState();
        Result<PlayerState> playerStateResult = playerStateCall.await(10, TimeUnit.SECONDS);
        PlayerState playerState = null;
        if (playerStateResult.isSuccessful()) {
            playerState = playerStateResult.getData();
            setTrackWasStarted(playerState);
            boolean isPaused = playerState.isPaused;
            long position = playerState.playbackPosition;
            boolean hasEnded = trackWasStarted && isPaused && position == 0;

            if (hasEnded) {
                trackWasStarted = false;
                //TODO: remove the ended track, start the next track
                songList.sort((o1, o2) -> Integer.compare(o1.getScore(), o2.getScore()));
                songList.remove(0);
                mSpotifyAppRemote.getPlayerApi().play(songList.get(0).getKey()); //starts the next track in the list with the highest score
                //set the current song score to "infinity"
                songList.get(0).setScore(Integer.MAX_VALUE);
                //TODO: update score in Firestore
                DocumentReference docRef = db.collection("songQueue").document(SESSION_ID);
                docRef.update(songList.get(0).getKey(), true);
            }
        } else {
            Throwable error = playerStateResult.getError();
        }
    }

    private void setTrackWasStarted(PlayerState playerState) {
        if(playerState == null) return;
        CallResult<PlayerState> currState = mSpotifyAppRemote.getPlayerApi().getPlayerState();
        long position = playerState.playbackPosition;
        long duration = playerState.track.duration;
        boolean isPlaying = !playerState.isPaused;

        if (!trackWasStarted && position > 0 && duration > 0 && isPlaying) {
            trackWasStarted = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(loggedInUser == sessionAdminUser) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        }
    }
}