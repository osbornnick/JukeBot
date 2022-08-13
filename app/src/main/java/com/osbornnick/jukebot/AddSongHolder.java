package com.osbornnick.jukebot;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class AddSongHolder extends SongItemHolder {
    private static final String TAG = "AddSongHolder";
    private String sessionID = "sessionTest1";

    TextView songSearchTitle, songDescription;
    ImageButton addSong;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences prefs;


    public AddSongHolder(@NonNull View itemView) {
        super(itemView);
        songSearchTitle = itemView.findViewById(R.id.songSearchTitle);
        songDescription = itemView.findViewById(R.id.songDescription);
        addSong = itemView.findViewById(R.id.addSong);
    }

    @Override
    public void bindThisData(Song songToBind) {
        sessionID = songToBind.session_id;

        songSearchTitle.setText(songToBind.getName());
        songDescription.setText(songToBind.getArtist());
        CollectionReference songQueueRef = FirebaseFirestore.getInstance().collection("Session").document(songToBind.session_id).collection("queue");
        String songId = songToBind.getKey();
        Map<String, Object> songMap = new HashMap<>();
        songMap.put("albumImageURL", songToBind.albumImageURL);
        songMap.put("albumIconImageURL", songToBind.albumIconImageURL);
        songMap.put("artist", songToBind.getArtist());
        songMap.put("uri", songToBind.getUri());
        songMap.put("name", songToBind.getName());
        songMap.put("preview_url", songToBind.previewURL);
        //TODO: Make sure we get username of active user to put in suggestedBy
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) songMap.put("suggestedBy", currentUser.getDisplayName());
        songMap.put("score", 1);
        songMap.put("deleted", false);
        songMap.put("played", false);
        songMap.put("playing", false);

        addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.runTransaction((Transaction.Function<Void>) transaction -> {
                    songQueueRef.document(songId).set(songMap);
                    return null;
                })
                    .addOnSuccessListener(Void -> Log.d(TAG, "song added to queue"))
                    .addOnFailureListener(e -> Log.e("SongQueueHolder", "Failure", e));
            }
        });
    }
}
