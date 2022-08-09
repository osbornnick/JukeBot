package com.osbornnick.jukebot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class SongQueueAdapter extends RecyclerView.Adapter<SongItemHolder>  {
    private SortedList<Song> songQueue;

    public SongQueueAdapter() {
        this.songQueue = new SortedList<>(Song.class, new SortedList.Callback<Song>() {

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public int compare(Song o1, Song o2) {
                return Long.compare(o2.getScore(), o1.getScore());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Song oldItem, Song newItem) {
                return Objects.equals(oldItem.getUri(), newItem.getUri()) && oldItem.getScore() == newItem.getScore();
            }

            @Override
            public boolean areItemsTheSame(Song item1, Song item2) {
                return Objects.equals(item1.getKey(), item2.getKey());
            }
        });
    }

    @NonNull
    @Override
    public SongItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_queue_item, parent, false);
        return new SongQueueHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemHolder holder, int position) {
        holder.bindThisData(songQueue.get(position));
    }

    @Override
    public int getItemCount() {
        return songQueue.size();
    }

    public int add(Song s) {
        return songQueue.add(s);
    }

    public boolean remove(Song s) {
        return songQueue.remove(s);
    }

    public Song getFirst() {
        return songQueue.get(0);
    }

    public void clear() {
        songQueue.beginBatchedUpdates();
        while (songQueue.size() > 0) {
            songQueue.removeItemAt(songQueue.size() - 1);
        }
        songQueue.endBatchedUpdates();
    }
}
