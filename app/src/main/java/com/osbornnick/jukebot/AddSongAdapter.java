package com.osbornnick.jukebot;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;

public class AddSongAdapter extends RecyclerView.Adapter<SongItemHolder> {
    private static final String TAG = "AddSongAdapter";
    private static int TYPE_ADD = 1;
    private static int TYPE_QUEUE = 2;
    public String SESSION_ID;
    public boolean admin = true;

    HashMap<String, Song> songQueue;
    ArrayList<Song> searchSongs;

    public AddSongAdapter(ArrayList<Song> searchSongList, HashMap<String, Song> songQueue) {
        this.searchSongs = searchSongList;
        this.songQueue = songQueue;
    }

    @Override
    public int getItemViewType(int position) {
        if(songQueue.containsKey(searchSongs.get(position).getKey())) {
            return TYPE_QUEUE;
        } else {
            return TYPE_ADD;
        }
    }

    @NonNull
    @Override
    public SongItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_search_item, parent, false);
//        return new AddSongHolder(view);
        if(viewType == TYPE_QUEUE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_queue_item, parent, false);
            SongQueueHolder v = new SongQueueHolder(view);
            v.admin = this.admin;
            return v;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_search_item, parent, false);
            return new AddSongHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemHolder holder, int position) {
        //create Song object using values from API
        Song s = this.searchSongs.get(position);
        Log.d(TAG, "binding song " + s.getName() + " by " + s.getArtist());
        Log.d(TAG, "songToBind matches song in Queue " + this.songQueue.get(s.getKey()));
        //if song in songQueue, inflate a SongQueueItem. else, inflate AddSongItem
        if(holder instanceof SongQueueHolder) {
            SongQueueHolder h = (SongQueueHolder) holder;
            h.admin = admin;
            h.bindThisData(Objects.requireNonNull(this.songQueue.get(s.getKey())));
        } else {
            AddSongHolder h = (AddSongHolder) holder;
            h.bindThisData(s);
        }
//        holder.bindThisData(s);
    }

    @Override
    public int getItemCount() {
        return this.searchSongs.size();
    }

    public void setSongQueue(HashMap<String, Song> songQueueIDs) {
        this.songQueue = songQueueIDs;
//        for(String id : songQueueIDs.keySet()) {
//            Log.d(TAG, "setSongQueue: " + id);
//        }
    }

    public void resetSearchResults(ArrayList<Song> searchSongList){
        this.searchSongs = searchSongList;
    }

}
