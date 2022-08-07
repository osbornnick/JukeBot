package com.osbornnick.jukebot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder> {
    private Context context;
    private ArrayList<Session> list;

    public SessionRecyclerViewAdapter(Context context, ArrayList<Session> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.session_item, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        holder.mSessionName.setText(list.get(position).getmSessionName());
        holder.mSessionHost.setText(list.get(position).getmSessionHost());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SessionViewHolder extends RecyclerView.ViewHolder{
        TextView mSessionName, mSessionHost;
        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            mSessionName = itemView.findViewById(R.id.sessionName);
            mSessionHost = itemView.findViewById(R.id.hostedBy);
        }
    }
}
