package com.osbornnick.jukebot;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder> {
    private Context context;
    private ArrayList<Session> list;
    private static final String TAG = "SessionRecyclerViewAdap";
    FirebaseUser user;

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "onBindViewHolder: " + list.get(position).getmSessionName());
            final String[] SessionName = new String[1];
            db.collection("Session")
                    .document(list.get(position).getmSessionName()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                SessionName[0] = task.getResult().getString("name");
                                Log.d(TAG, "onComplete: username " + SessionName[0]);

                                holder.mSessionName.setText(SessionName[0]);
                            }
                        }
                    });

        holder.session_id = list.get(position).getmSessionName();
        holder.mSessionHost.setText(list.get(position).getmSessionHost());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SessionViewHolder extends RecyclerView.ViewHolder{
        TextView mSessionName, mSessionHost;
        ImageButton imageButton;
        String session_id;
        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            mSessionName = itemView.findViewById(R.id.sessionName);
            mSessionHost = itemView.findViewById(R.id.hostedBy);
            imageButton = itemView.findViewById(R.id.GoToSession);
            itemView.setClickable(true);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = FirebaseAuth.getInstance().getCurrentUser();

                    // if you are the host. need host uid
                    Intent intent = new Intent(context, SessionAdminActivity.class);
                    Intent intent2 = new Intent(context, NonAdminSessionActivity.class);
                    intent.putExtra("session_id", session_id);
                    intent.putExtra("session_name", mSessionName.getText());
                    intent2.putExtra("session_id", session_id);
                    intent2.putExtra("session_name", mSessionName.getText());

                    if (user.getUid() == session_id){
                        context.startActivity(intent);
                    } else {
                        context.startActivity(intent2);
                    }

                }
            });

        }


    }
}
