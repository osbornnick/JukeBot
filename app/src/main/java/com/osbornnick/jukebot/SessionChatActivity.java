package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    String uEmail = null;
    String timeStamp = null;
    TextInputLayout message;
    FloatingActionButton send;

    //ConstraintLayout mSessionChatActivity;
    ImageButton mButton;
    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_chat);
        send = findViewById(R.id.fab_send);
        message = findViewById(R.id.message);

        //mSessionChatActivity = (ConstraintLayout) findViewById(R.id.activity_session_chat);
        //mButton = (ImageButton) findViewById(R.id.submitButton);
        //mMessage = findViewById(R.id.chatInputLayout);
        mRecyclerView = findViewById(R.id.recyclerView);
        scrollToBot();
        mList = new ArrayList<>();
        try {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), REQUEST_CODE);

            } else {
                //Snackbar.make(mSessionChatActivity, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
                user = FirebaseAuth.getInstance().getCurrentUser();
                uEmail = user.getEmail();
                Log.d(TAG, "onCreate: " + user);
                //showChatMessage();
            }
        } catch (Exception e){
            Log.d(TAG, "onCreate: " + e);
        }

        db = FirebaseFirestore.getInstance();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToBot();
                String msg = message.getEditText().getText().toString();
                timeStamp = new SimpleDateFormat("MM-dd-yy HH:mm:ssa").format(Calendar.getInstance().getTime());
                db.collection("Messages").add(new Message(msg,uEmail,timeStamp)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        receiveMessages();
                        hideKeyboard(SessionChatActivity.this);
                        //scrollToBottom(SessionChatActivity.this);
                        message.getEditText().setText("");
                    }
                });
            }
        });

        update();
    }

    public void update(){
        mAdapter = new MessageRecyclerViewAdapter(SessionChatActivity.this, mList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SessionChatActivity.this,RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        receiveMessages();
    }

    @Override
    protected void onStart(){
        super.onStart();
        receiveMessages();
    }

    private void receiveMessages(){
        db.collection("Messages").orderBy("messageTime").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    mList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());

                        Message message = document.toObject(Message.class);
                        mAdapter.addMessage(message);
//                     mList.add(message);
//                     mAdapter.notifyDataSetChanged();


                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


//        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int heightDiff = finalView.getRootView().getHeight() - finalView.getHeight();
//                if (heightDiff > 100) {
//                    if (mAdapter.getItemCount() > 0)
//                        mRecyclerView.smoothScrollToPosition(mList.size() - 1);
//                }
//            }
//        });
    }

    public void scrollToBottom(Activity activity){
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        View finalView = view;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = finalView.getRootView().getHeight() - finalView.getHeight();
                if (heightDiff > 100) {
                    if (mAdapter.getItemCount() > 0)
                        mRecyclerView.smoothScrollToPosition(mList.size() - 1);
                }
            }
        });
    }

    public void scrollToBot(){
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        }, 1000);
    }


}