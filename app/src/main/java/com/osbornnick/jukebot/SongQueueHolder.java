package com.osbornnick.jukebot;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class SongQueueHolder extends SongItemHolder {
    public TextView songTitle, songArtist, suggestedBy, score;
    public ImageButton voteUp, voteDown, delete;
    DocumentReference songRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SongQueueHolder(@NonNull View itemView) {
        super(itemView);
        songTitle = itemView.findViewById(R.id.songTitle);
        songArtist = itemView.findViewById(R.id.songArtist);
        suggestedBy = itemView.findViewById(R.id.suggestedBy);
        score = itemView.findViewById(R.id.score);
        voteUp = itemView.findViewById(R.id.voteUp);
        voteDown = itemView.findViewById(R.id.voteDown);
        delete = itemView.findViewById(R.id.GoToSession);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindThisData(Song songToBind) {
        this.itemSong = songToBind;
        songRef = FirebaseFirestore.getInstance().collection("Session").document(songToBind.session_id).collection("queue").document(songToBind.getKey());
        songTitle.setText(itemSong.getName());
        songArtist.setText(itemSong.getArtist());
        suggestedBy.setText("Suggested By: " + itemSong.getSuggestedBy());
        score.setText(String.valueOf(itemSong.getScore()));

        voteUp.setOnClickListener(v -> {
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                songRef.update("score", FieldValue.increment(1));
                return null;
            })
                    .addOnSuccessListener(Void -> Log.d("SongQueueHolder", "score updated!"))
                    .addOnFailureListener(e -> Log.e("SongQueueHolder", "Failure", e));
        });

        voteDown.setOnClickListener(v -> {
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                        songRef.update("score", FieldValue.increment(-1));
                        return null;
                    })
                    .addOnSuccessListener(Void -> Log.d("SongQueueHolder", "score updated!"))
                    .addOnFailureListener(e -> Log.e("SongQueueHolder", "Failure", e));
        });

        delete.setOnClickListener(v -> {
            songRef.update("deleted", true);
        });

    }
}
