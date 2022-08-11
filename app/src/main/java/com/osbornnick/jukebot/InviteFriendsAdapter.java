package com.osbornnick.jukebot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsHolder> {
    private SortedList<String> userList;
    private ArrayList<String> userListAll;
    private HashMap<String, String> userMap;
    public String sessionID;
    Filter filter;

    public InviteFriendsAdapter(String sessionID) {
        this.userListAll = new ArrayList<>();
        this.userMap = new HashMap<>();
        this.sessionID = sessionID;
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
        holder.bindThisData(userList.get(position), userMap.get(userList.get(position)), sessionID);
    }

    public void add(String user, String userID) {
        if(user == null) return;
        userMap.put(user, userID);
        userList.add(user);
        userListAll.add(user);
    }

    public void remove(String user) {
        if(user == null) return;
        userMap.remove(user);
        userList.remove(user);
        userListAll.remove(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public Filter getFilter() {
        return filter;
    }
}
