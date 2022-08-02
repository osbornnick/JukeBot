package com.osbornnick.jukebot;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SongItemHolder extends RecyclerView.ViewHolder {
    Song itemSong;
    public SongItemHolder(@NonNull View itemView) {
        super(itemView);
    }

    protected void bindThisData(Song songToBind) {

    };
}
