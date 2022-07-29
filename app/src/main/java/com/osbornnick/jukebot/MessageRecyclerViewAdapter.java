package com.osbornnick.jukebot;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Message> list;
    private static final String TAG = "MessageRecyclerViewAdap";

    public MessageRecyclerViewAdapter(Context context, ArrayList<Message> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MessageRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.username.setText(list.get(position).getMessageUser());
        holder.message.setText(list.get(position).getMessageText());
        holder.dateTime.setText(list.get(position).getMessageTime());
//        Log.d(TAG, "onBindViewHolder: text " +list.get(position).getMessageText() );
//        Log.d(TAG, "onBindViewHolder: getTime " +list.get(position).getMessageTime() );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, message, dateTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.userChat);
            username = (TextView) itemView.findViewById(R.id.user);
            dateTime = (TextView) itemView.findViewById(R.id.time);
        }
    }
}
