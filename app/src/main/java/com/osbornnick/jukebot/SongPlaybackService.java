package com.osbornnick.jukebot;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

public class SongPlaybackService extends Service {

    private static final String TAG = "SongPlaybackService";
    private MediaPlayer player;
    private final IBinder binder = new SongPlaybackBinder();
    private boolean playing = false;
    private boolean completed = true;
    private String sessionID;

    public SongPlaybackService() {
//        player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setDataSource(String url) {
        if(completed) {
            player.reset();
            try {
                player.setDataSource(url);
                player.prepare();
                player.start();
                playing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Error: Can't reset player while song is still playing a song");
        }
    }

    public boolean isCompleted() { return completed; };

    public boolean isPlaying() { return player.isPlaying(); }

    public void setSession(String sessionID) { this.sessionID = sessionID; }

    public void resume() {
        player.start();
        playing = true;
    }

    public void pause() {
        player.pause();
        playing = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "Media player is prepared");
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "Music Player song completed");
                completed = true;
                Intent i = new Intent();
                i.setAction("com.osbornnick.jukebot||" + sessionID + "||completed");
                sendBroadcast(i);
            }
        });

        player.setOnErrorListener((mp, what, extra) -> {
            switch(what) {
                case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                    Log.d(TAG, "onError: MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.d(TAG, "onError: MEDIA_ERROR_SERVER_DIED");
                    break;
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.d(TAG, "onError: MEDIA_ERROR_UNKNOWN");
                    break;
            }
            return false;
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player != null) {
            if(player.isPlaying()) {
                player.stop();
            }
            player.release();
        }
    }

    public class SongPlaybackBinder extends Binder {
        public SongPlaybackService getService() { return SongPlaybackService.this; }
    }
}