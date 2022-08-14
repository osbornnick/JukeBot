package com.osbornnick.jukebot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.ActionCodeUrl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class SongQueueHolder extends SongItemHolder {
    Song itemSong;
    public TextView songTitle, songArtist, suggestedBy, score;
    public ImageButton voteUp, voteDown, delete;
    public boolean admin = true;
    DocumentReference songRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public SongQueueHolder(@NonNull View itemView) {
        super(itemView);
        songTitle = itemView.findViewById(R.id.songTitle);
        songArtist = itemView.findViewById(R.id.songArtist);
        suggestedBy = itemView.findViewById(R.id.suggestedBy);
        score = itemView.findViewById(R.id.score);
        voteUp = itemView.findViewById(R.id.voteUp);
        voteDown = itemView.findViewById(R.id.voteDown);
        delete = itemView.findViewById(R.id.delete);
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

        voteUp.setOnClickListener(this::upVoteHandler);
        voteDown.setOnClickListener(this::downVoteHandler);

        if (songToBind.voted != null) {
            if (songToBind.voted.equals("UP")) {
                voteUp.setOnClickListener(null);
                voteUp.getDrawable().setColorFilter(Color.parseColor("green"), PorterDuff.Mode.MULTIPLY);
                voteDown.getDrawable().setColorFilter(null);
            } else if (songToBind.voted.equals("DOWN")) {
                voteDown.setOnClickListener(null);
                voteDown.getDrawable().setColorFilter(Color.parseColor("red"), PorterDuff.Mode.MULTIPLY);
                voteUp.getDrawable().setColorFilter(null);
            }
        }

        if(!admin) {
            delete.setVisibility(View.GONE);
        } else {
            delete.setOnClickListener(v -> {
                songRef.update("deleted", true);
            });
        }
    }

    public void upVoteHandler(View view) {
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    songRef.update("score", FieldValue.increment(1));
                    songRef.update("upVotes", FieldValue.arrayUnion(currentUser.getUid()));
                    songRef.update("downVotes", FieldValue.arrayRemove(currentUser.getUid()));
                    return null;
                })
                .addOnSuccessListener(Void -> Log.d("SongQueueHolder", "score updated!"))
                .addOnFailureListener(e -> Log.e("SongQueueHolder", "Failure", e));
    }

    public void downVoteHandler(View view) {
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    songRef.update("score", FieldValue.increment(-1));
                    songRef.update("downVotes", FieldValue.arrayUnion(currentUser.getUid()));
                    songRef.update("upVotes", FieldValue.arrayRemove(currentUser.getUid()));
                    return null;
                })
                .addOnSuccessListener(Void -> Log.d("SongQueueHolder", "score updated!"))
                .addOnFailureListener(e -> Log.e("SongQueueHolder", "Failure", e));
    }

    public void updateButtons() {
        if (itemSong.voted == null) {
            voteUp.getDrawable().setColorFilter(null);
            voteDown.getDrawable().setColorFilter(null);
        } else if (itemSong.voted.equals("UP")) {
            voteUp.getDrawable().setColorFilter(Color.parseColor("green"), PorterDuff.Mode.MULTIPLY);
            voteDown.getDrawable().setColorFilter(null);
        } else if (itemSong.voted.equals("DOWN")) {
            voteDown.getDrawable().setColorFilter(Color.parseColor("red"), PorterDuff.Mode.MULTIPLY);
            voteUp.getDrawable().setColorFilter(null);
        }
    }
}
