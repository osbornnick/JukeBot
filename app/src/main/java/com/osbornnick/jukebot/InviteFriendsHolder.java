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
    TextView username, alreadyAddedText;
    ImageButton addUser;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String sessionID;

    public InviteFriendsHolder(@NonNull View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.username);
        alreadyAddedText = itemView.findViewById(R.id.alreadyAddedText);
        addUser = itemView.findViewById(R.id.addUser);
    }

    public void bindThisData(String user, boolean inSession, String sessionID) {
        this.sessionID = sessionID;
        username.setText(user);
        if(!inSession) {
            alreadyAddedText.setVisibility(View.GONE);
            addUser.setVisibility(View.VISIBLE);

            //addUser onClick Listener
            addUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    db.collection("Session").document(sessionID).collection("users")

                }
            });
        } else {
            alreadyAddedText.setVisibility(View.VISIBLE);
            addUser.setVisibility(View.GONE);
        }
    }
}
