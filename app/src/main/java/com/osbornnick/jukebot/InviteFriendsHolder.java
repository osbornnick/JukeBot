package com.osbornnick.jukebot;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class InviteFriendsHolder extends RecyclerView.ViewHolder{
    TextView username;
    ImageButton addUser;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String sessionID;

    public InviteFriendsHolder(@NonNull View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.username);
        addUser = itemView.findViewById(R.id.addUser);
    }

    public void bindThisData(String user, String sessionID) {
        this.sessionID = sessionID;
        username.setText(user);
        //addUser onClick Listener
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    db.collection("Session").document(sessionID).collection("users")

            }
        });
    }
}
