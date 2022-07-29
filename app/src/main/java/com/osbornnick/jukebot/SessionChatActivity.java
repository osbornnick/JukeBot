package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseAppLifecycleListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SessionChatActivity extends AppCompatActivity {

    private static final String TAG = "SessionChatActivity";
    private static int REQUEST_CODE = 1;
//    private FirebaseRecyclerAdapter<Message,ChatViewHolder> mAdapter;
    MessageRecyclerViewAdapter mAdapter;
    ArrayList<Message> mList;
    TextInputLayout mMessage;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;

    ConstraintLayout mSessionChatActivity;
    ImageButton mButton;
    RecyclerView mRecyclerView;
    FirebaseRecyclerOptions<Message> options;
    Query query;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){

            if (resultCode == RESULT_OK){
                Snackbar.make(mSessionChatActivity, "signed in", Snackbar.LENGTH_SHORT).show();
                //showChatMessage();
            } else {
                Snackbar.make(mSessionChatActivity, "couldn't sign you in", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // options menu to sign you out
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(mSessionChatActivity, "signed out", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    // create options menu on top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_chat);

        mSessionChatActivity = (ConstraintLayout) findViewById(R.id.activity_session_chat);
        mButton = (ImageButton) findViewById(R.id.submitButton);
        mMessage = findViewById(R.id.chatInputLayout);
        mRecyclerView = findViewById(R.id.stickerRecyclerView);
        mList = new ArrayList<>();

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), REQUEST_CODE);

        } else {
            Snackbar.make(mSessionChatActivity,FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "onCreate: " + user);
            //showChatMessage();
        }


        db = FirebaseFirestore.getInstance();
        String uId = user.getUid();
        String uEmail = user.getEmail();
        String timeStamp = new SimpleDateFormat("MM-dd-yy HH:mma").format(Calendar.getInstance().getTime());


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mMessage.getEditText().getText().toString();

                db.collection("Messages").add(new Message(msg,uEmail,timeStamp)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        mMessage.getEditText().setText("");
                    }
                });
                //FirebaseDatabase.getInstance().getReference().push().setValue(new Message(chatTextInput.getText().toString()),FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });

        mAdapter = new MessageRecyclerViewAdapter(this, mList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL, true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //displaychat

    }

    @Override
    protected void onStart(){
        super.onStart();
        receiveMessages();
    }

    private void receiveMessages(){
        db.collection("Messages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    mList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Message message = document.toObject(Message.class);
                        mList.add(message);
                        mAdapter.notifyDataSetChanged();

                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

}