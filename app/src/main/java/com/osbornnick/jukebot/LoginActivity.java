package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextView emailTV;
    TextView passwordTV;
    TextView statusTV;
    TextView anonymousTV;
    Button loginButton;
    Button createAccountButton;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailTV = findViewById(R.id.emailAddress);
        passwordTV = findViewById(R.id.password);
        statusTV = findViewById(R.id.statusText);
        anonymousTV = findViewById(R.id.anonymousTV);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        uiHandler = new Handler(Looper.getMainLooper());
        anonymousTV.setOnClickListener(view -> handleAnonymousLogin());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // show they are logged in
            this.onBackPressed();
        }
    }

    public void handleAnonymousLogin() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser newUser = mAuth.getCurrentUser();
                String uid = newUser.getUid();
                String username = "anon" + uid.substring(uid.length() - 3);
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                newUser.updateProfile(profileUpdate);
                Map<String, Object> userData = new HashMap<String, Object>() {{
                    put("username", username);
                    put("dateCreated", FieldValue.serverTimestamp());
                }};
                FirebaseFirestore.getInstance().collection("users").document(uid).set(userData);
                this.onBackPressed();
            } else {
                uiHandler.post(() -> statusTV.setText(task.getException().getLocalizedMessage()));
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void handleLogin(View v) {
        if (!validate()) return;
        progressBar.setVisibility(View.VISIBLE);
        String email = emailTV.getText().toString();
        String password = passwordTV.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
           if (task.isSuccessful()) {
               FirebaseUser loggedInUser = mAuth.getCurrentUser();
               if (loggedInUser.getDisplayName() == null) {
                   FirebaseFirestore.getInstance().collection("users").document(loggedInUser.getUid()).get().addOnSuccessListener(snap -> {
                      String username = snap.get("username", String.class);
                      UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                      loggedInUser.updateProfile(profileUpdate);
                   });
               }
               Intent intent = new Intent(this, MainActivity.class);
               startActivity(intent);
           } else {
               uiHandler.post(() -> {
                   statusTV.setText(task.getException().getLocalizedMessage());
                   progressBar.setVisibility(View.INVISIBLE);
               });
           }
        });
    }

    public void handleSignup(View v) {
        Intent i = new Intent(this, CreateAccountActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        this.finish();
    }

    private boolean validate() {
        boolean valid = true;

        if (emailTV.getText() == null || emailTV.getText().toString().equals("")) {
            emailTV.setError("Required.");
            valid = false;
        }
        if (passwordTV.getText() == null || passwordTV.getText().toString().equals("")) {
            passwordTV.setError("Required.");
            valid = false;
        }
        return valid;
    }

}