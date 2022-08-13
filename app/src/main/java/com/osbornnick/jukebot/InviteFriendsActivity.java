package com.osbornnick.jukebot;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class InviteFriendsActivity extends AppCompatActivity {
    private static final String TAG = "InviteFriendsActivity";
    private String SESSION_ID = "sessionTest1";
    private String SESSION_NAME = "Session 1";

    TextView sessionTitle;
    ImageButton back, leaveSession;
    Button cancelSearch;
    SearchView searchView;
    RecyclerView user_rv;
    InviteFriendsAdapter adapter;

    boolean initialized = false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        Intent i = getIntent();
        SESSION_ID = i.getStringExtra("session_id");
        SESSION_NAME = i.getStringExtra("session_name");

        sessionTitle = findViewById(R.id.sessionTitle);
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        cancelSearch = findViewById(R.id.cancelSearch);
        searchView = findViewById(R.id.searchView);
        user_rv = findViewById(R.id.user_rv);

        //init toolbar controls
        initToolbar();

        //init user recycler view
        user_rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InviteFriendsAdapter(SESSION_ID);
        user_rv.setAdapter(adapter);
        user_rv.addItemDecoration(new DividerItemDecoration(user_rv.getContext(), ((LinearLayoutManager)user_rv.getLayoutManager()).getOrientation()));
        getUserList();

        //set filtering listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                closeKeyboard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        //cancel button listener
        cancelSearch.setOnClickListener(v -> onBackPressed());
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        leaveSession.setOnClickListener(v -> {
            Intent i = new Intent(InviteFriendsActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getUserList() {
        db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "listen:error", error);
                return;
            }
            if (value == null) return;
            value.getDocumentChanges().forEach(dc -> {
                Map<String, Object> data = dc.getDocument().getData();
                ArrayList<String> connectedSessions = (ArrayList<String>) data.get("connectedSession");
                String userID = dc.getDocument().getId();
                if (connectedSessions != null) {
                    HashSet<String> sessions = new HashSet<>(connectedSessions);
                    if(!sessions.contains(this.SESSION_ID)) {
                        adapter.add((String) data.get("username"), userID);
                        Log.d(TAG, data.get("username") + " found");
                    } else {
                        if(initialized) {
                            adapter.remove((String) data.get("username"));
                            Log.d(TAG, data.get("username") + " removed from recycler view");
//                            Toast.makeText(this, data.get("username") + " was addded to the session.", Toast.LENGTH_SHORT).show();
//                            onBackPressed();

                            //TODO: Send a push notification
                        }
                    }
                } else {
                    adapter.add((String) data.get("username"), userID);
                    Log.d(TAG, data.get("username") + " found");
                }
            });
        });
        initialized = true;
    }
}