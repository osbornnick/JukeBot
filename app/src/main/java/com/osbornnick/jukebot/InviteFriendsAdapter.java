package com.osbornnick.jukebot;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsHolder> {
    List<String> userList, alreadyAddedText;


    @NonNull
    @Override
    public InviteFriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull InviteFriendsHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
