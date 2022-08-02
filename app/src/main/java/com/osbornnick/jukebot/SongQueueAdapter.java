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

    public SongQueueAdapter(List<Song> songQueue) {
        this.songQueue = new SortedList<Song>(Song.class, new SortedList.Callback<Song>() {

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
                if(o1.getScore() > o2.getScore()) {
                    return 1;
                } else if(o1.getScore() < o2.getScore()) {
                    return -1;
                } else {
                    return 0;
                }
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Song oldItem, Song newItem) {
                boolean name = Objects.equals(oldItem.getName(), newItem.getName());
                boolean artist = Objects.equals(oldItem.getArtist(), newItem.getArtist());
                boolean suggestedBy = Objects.equals(oldItem.getSuggestedBy(), newItem.getSuggestedBy());
                boolean albumImage = Objects.equals(oldItem.getAlbumImage(), newItem.getAlbumImage());
                boolean duration = Objects.equals(oldItem.getDuration(), newItem.getDuration());
                return name && artist && suggestedBy && albumImage && duration;
            }

            @Override
            public boolean areItemsTheSame(Song item1, Song item2) {
                return Objects.equals(item1.getKey(), item2.getKey());
            }
        });

        //insert into sorted list
        for(Song s : songQueue) {
            this.songQueue.add(s);
        }
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


}
