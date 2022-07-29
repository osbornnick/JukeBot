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

public class LoginActivity extends AppCompatActivity {

    TextView emailTV;
    TextView passwordTV;
    TextView statusTV;
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
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        uiHandler = new Handler(Looper.getMainLooper());
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

    public void handleLogin(View v) {
        if (!validate()) return;
        progressBar.setVisibility(View.VISIBLE);
        String email = emailTV.getText().toString();
        String password = passwordTV.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
           if (task.isSuccessful()) {
               this.onBackPressed();
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