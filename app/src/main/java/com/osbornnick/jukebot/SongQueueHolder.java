package com.osbornnick.jukebot;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongQueueHolder extends SongItemHolder {
    TextView songTitle, songArtist, suggestedBy, score;
    Button voteUp, voteDown, delete;

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

    @Override
    public void bindThisData(Song songToBind) {
        this.itemSong = songToBind;
        songTitle.setText(itemSong.getName());
        songArtist.setText(itemSong.getArtist());
        if(itemSong.isAnonymous()) {
            suggestedBy.setText("Suggested By: Anonymous");
        } else {
            suggestedBy.setText("Suggested By: " + itemSong.getSuggestedBy());
        }
        if(itemSong.getScore() > 0) {
            score.setText("+ " + itemSong.getScore());
        } else if(itemSong.getScore() < 0) {
            score.setText("- " + itemSong.getScore());
        } else {
            score.setText("0");
        }

        voteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemSong.setScore(itemSong.getScore() + 1);
                if(itemSong.getScore() > 0) {
                    score.setText("+ " + itemSong.getScore());
                } else if(itemSong.getScore() < 0) {
                    score.setText("- " + itemSong.getScore());
                } else {
                    score.setText("0");
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //update firebase songQueue
                    }
                }).start();
            }
        });

        voteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemSong.setScore(itemSong.getScore() - 1);
                if(itemSong.getScore() > 0) {
                    score.setText("+ " + itemSong.getScore());
                } else if(itemSong.getScore() < 0) {
                    score.setText("- " + itemSong.getScore());
                } else {
                    score.setText("0");
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //update firebase songQueue
                    }
                }).start();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //delete this song from Firebase Session Queue
                        //In turn, the UI should respond to delete this item
                    }
                }).start();
            }
        });
    }
}
