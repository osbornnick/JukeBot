package com.osbornnick.jukebot;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    ImageView songIcon;
    TextView songSearchTitle, songDescription;
    ImageButton addSong;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public AddSongHolder(@NonNull View itemView) {
        super(itemView);
        songIcon = itemView.findViewById(R.id.songIcon);
        songSearchTitle = itemView.findViewById(R.id.songSearchTitle);
        songDescription = itemView.findViewById(R.id.songDescription);
        addSong = itemView.findViewById(R.id.addSong);
    }

    @Override
    public void bindThisData(Song songToBind) {
        sessionID = songToBind.session_id;

//        songIcon.setImageBitmap("");
        songSearchTitle.setText(songToBind.getName());
        songDescription.setText(songToBind.getArtist());


        addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String songId = "song10Id";
                Map<String, Object> songMap = new HashMap<>();
                songMap.put("albumImageURL", "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228");
                songMap.put("artist", "Lumineers");
                songMap.put("uri", "spotify:track:" + songId);
                songMap.put("suggestedBy", "userTest");
                songMap.put("score", 1);
                songMap.put("deleted", false);
                songMap.put("played", false);
                songMap.put("playing", false);

                //add song to FireStore queue
                db.collection("Session").document(sessionID).collection("queue").document(songId)
                        .set(songMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

            }
        });
    }
}
