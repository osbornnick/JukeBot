package com.osbornnick.jukebot;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
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

    ArrayList<String> sessionUserList;
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
        sessionUserList = new ArrayList<>();
        getUserList();
        user_rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InviteFriendsAdapter(sessionUserList, SESSION_ID);
        user_rv.setAdapter(adapter);
        user_rv.addItemDecoration(new DividerItemDecoration(user_rv.getContext(), ((LinearLayoutManager)user_rv.getLayoutManager()).getOrientation()));

        //set filtering listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        leaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InviteFriendsActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
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
                String[] connectedSessions = (String[]) data.get("connectedSession");
                if (connectedSessions != null) {
                    HashSet<String> sessions = new HashSet<>(Arrays.asList(connectedSessions));
                    String userID = dc.getDocument().getId();
                    if(!sessions.contains(this.SESSION_ID)) {
                        this.sessionUserList.add((String) data.get("username"));

                        Log.d(TAG, (String) data.get("username") + " found");
                    }
                }
            });
        });
    }
}