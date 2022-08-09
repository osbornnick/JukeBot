package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InviteFriendsActivity extends AppCompatActivity {
    private static final String TAG = "InviteFriendsActivity";
    private String SESSION_ID = "sessionTest1";
    private String SESSION_NAME = "Session 1";

    TextView sessionTitle;
    ImageButton back, leaveSession;
    Button cancelSearch;
    SearchView searchView;
    RecyclerView user_rv;

    List<String> sessionUserList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        sessionTitle = findViewById(R.id.sessionTitle);
        back = findViewById(R.id.back);
        leaveSession = findViewById(R.id.leaveSession);
        cancelSearch = findViewById(R.id.cancelSearch);
        searchView = findViewById(R.id.searchView);
        user_rv = findViewById(R.id.user_rv);

        //init toolbar controls
        initToolbar();

        //init search functionality
        initUserSearch();

        //init user recycler view
        initFriendsRecyclerView();
    }

    private void initToolbar() {
        sessionTitle.setText(SESSION_NAME);

        back.setOnClickListener(v -> onBackPressed());

        //TODO: Update leave session functionality
//        leaveSession.setOnClickListener(v -> onBackPressed());
        leaveSession.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    public void initUserSearch() {

    }

    public void initFriendsRecyclerView() {

    }
}