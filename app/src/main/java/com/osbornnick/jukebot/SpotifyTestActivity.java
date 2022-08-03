package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

public class SpotifyTestActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "fe144966828b41b5ab78f844e0630286";
    private static final String REDIRECT_URI = "com.osbornnick.jukebot://callback";
    private SpotifyAppRemote mSpotifyAppRemote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_test);

    }

    @Override
    protected  void onStart() {
        super.onStart();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID).setRedirectUri(REDIRECT_URI).showAuthView(true).build();
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d("SpotifyTestActivity", "Connected!");
                connected();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e("SpotifyTestActivity", error.getLocalizedMessage(), error);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:3VZQshi4COChhXaz7cLP02");

        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            Log.d("playerStateSubscription", playerState.toString());
            Log.d("playerStateSubscription", playerState.isPaused + " " + playerState.playbackPosition);
            boolean isPaused = playerState.isPaused && (playerState.playbackPosition == 0);
        });
    }
}