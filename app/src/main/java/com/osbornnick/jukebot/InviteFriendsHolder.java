package com.osbornnick.jukebot;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
    Context view;

    public InviteFriendsHolder(@NonNull View itemView, Context view) {
        super(itemView);

        username = itemView.findViewById(R.id.username);
        addUser = itemView.findViewById(R.id.addUser);
        view = view;
    }

    public void bindThisData(String user, String userID, String sessionID) {
        this.sessionID = sessionID;
        username.setText(user);
        //addUser onClick Listener
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.runTransaction((Transaction.Function<Void>) transaction -> {
                    db.collection("users").document(userID).update("connectedSession", FieldValue.arrayUnion(sessionID));
                    return null;
                })
                        .addOnSuccessListener(Void -> {
                            Log.d("InviteFriendsHolder", "onClick: user added to queue");
                            Toast.makeText(view, user + "was addded to the session.", Toast.LENGTH_SHORT);
                            //TODO: Send a push notification
                        })
                        .addOnFailureListener(e -> Log.e("InviteFriendsHolder", "Failure to add user", e));

            }
        });
    }
}
