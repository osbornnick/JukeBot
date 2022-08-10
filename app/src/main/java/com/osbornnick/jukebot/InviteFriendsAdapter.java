package com.osbornnick.jukebot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Locale;
import java.util.logging.LogRecord;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsHolder> {
    private SortedList<String> userList;
    private ArrayList<String> userListAll;
    public String sessionID;
    Filter filter;

    public InviteFriendsAdapter(ArrayList<String> users, String sessionID) {
        this.userList = new SortedList<>(String.class, new SortedList.Callback<String>() {

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
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(String oldItem, String newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(String item1, String item2) {
                return item1.equals(item2);
            }
        });

        userList.addAll(users);
        userListAll = users;
        this.sessionID = sessionID;

        filter = new Filter() {
            //runs on background thread
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<String> filterList = new ArrayList<>();
                if(constraint.toString().isEmpty()) {
                    filterList.addAll(userListAll);
                } else {
                    for(String user : userListAll) {
                        if(user.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filterList.add(user);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filterList;
                return filterResults;
            }
            //runs on UI thread
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                userList.clear();
                userList.addAll((Collection<String>) results.values);
            }
        };
    }


    @NonNull
    @Override
    public InviteFriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_search_item, parent, false);
        return new InviteFriendsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteFriendsHolder holder, int position) {
        holder.bindThisData(userList.get(position), sessionID);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public Filter getFilter() {
        return filter;
    }
}
