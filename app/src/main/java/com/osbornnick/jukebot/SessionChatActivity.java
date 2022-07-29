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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseAppLifecycleListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class SessionChatActivity extends AppCompatActivity {

    private static int REQUEST_CODE = 1;
    private FirebaseRecyclerAdapter<Message,ChatViewHolder> mAdapter;
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
                showChatMessage();
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
        mRecyclerView = (RecyclerView) findViewById(R.id.stickerRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        query = FirebaseDatabase.getInstance().getReference();
        options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(query,Message.class)
                .build();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText chatTextInput = (EditText)findViewById(R.id.chatTextInput);
                FirebaseDatabase.getInstance().getReference().push().setValue(new Message(chatTextInput.getText().toString()),FirebaseAuth.getInstance().getCurrentUser().getEmail());
                chatTextInput.setText("");
            }
        });


        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), REQUEST_CODE);
        } else {
            Snackbar.make(mSessionChatActivity,FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            showChatMessage();
        }

        //displaychat

    }

    private void showChatMessage() {
        mAdapter = new FirebaseRecyclerAdapter<Message, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Message model) {
                holder.messageText.setText(model.getMessageText());
                holder.messageUser.setText(model.getMessageUser());
                holder.messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ChatViewHolder(LayoutInflater.from(SessionChatActivity.this)
                        .inflate(R.layout.chat_item,parent,false));
            }
        };

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    private class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageUser, messageTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.userChat);
            messageUser = (TextView) itemView.findViewById(R.id.user);
            messageTime = (TextView) itemView.findViewById(R.id.time);
        }
    }

}