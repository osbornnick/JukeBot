package com.osbornnick.jukebot;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;

public class AddSongAdapter extends RecyclerView.Adapter<SongItemHolder> {
    private static final String TAG = "AddSongAdapter";
    private static int TYPE_ADD = 1;
    private static int TYPE_QUEUE = 2;
    public String SESSION_ID;
    public boolean admin = true;

    HashSet<String> songQueue;
    ArrayList<Song> searchSongs;

    public AddSongAdapter(ArrayList<Song> searchSongList) {
        searchSongs = searchSongList;
    }

    @Override
    public int getItemViewType(int position) {
        if(songQueue.contains(searchSongs.get(position).getKey())) {
            return TYPE_QUEUE;
        } else {
            return TYPE_ADD;
        }
    }

    @NonNull
    @Override
    public SongItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_QUEUE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_queue_item, parent, false);
            return new SongQueueHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_search_item, parent, false);
            return new AddSongHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemHolder holder, int position) {
        //create Song object using values from API
        Song s = searchSongs.get(position);
        Log.d(TAG, "binding song " + s.toString());
        //if song in songQueue, inflate a SongQueueItem. else, inflate AddSongItem
        if(holder instanceof SongQueueHolder) {
            SongQueueHolder h = (SongQueueHolder) holder;
            h.bindThisData(s);
            if(!admin) {
                h.delete.setVisibility(View.GONE);
            }
        } else {
            AddSongHolder h = (AddSongHolder) holder;
            h.bindThisData(s);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setSongQueue(HashSet<String> songQueueIDs) {
        this.songQueue = songQueueIDs;
    }

    public void resetSearchResults(ArrayList<Song> searchSongList){
        this.searchSongs = searchSongList;
    }

}
