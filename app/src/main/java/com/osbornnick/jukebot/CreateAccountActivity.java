package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView usernameTV;
    private TextView emailTV;
    private TextView passwordTV;
    private TextView passwordConfirmTV;
    private TextView statusTV;

    FirebaseAuth mAuth;
    Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        progressBar = findViewById(R.id.progressBar2);
        usernameTV = findViewById(R.id.username);
        emailTV = findViewById(R.id.emailFill);
        passwordTV = findViewById(R.id.pw1);
        passwordConfirmTV = findViewById(R.id.pw2);
        statusTV = findViewById(R.id.statusMsg);
        mAuth = FirebaseAuth.getInstance();
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public void handleSignup(View view) {
        if (!validate()) return;
        progressBar.setVisibility(View.VISIBLE);
        String email = emailTV.getText().toString();
        String password = passwordTV.getText().toString();
        Log.d("handleSignup", email + " " + password);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser newUser = mAuth.getCurrentUser();
                String uid = newUser.getUid();
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", usernameTV.getText().toString());
                userData.put("dateCreated", FieldValue.serverTimestamp());
                FirebaseFirestore.getInstance().collection("users").document(uid).set(userData);
                this.onBackPressed();
            } else {
                uiHandler.post(() -> {
                    statusTV.setText(task.getException().getLocalizedMessage());
                    progressBar.setVisibility(View.INVISIBLE);
                });
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        if (isEmpty(emailTV)) {
            emailTV.setError("Required.");
            valid = false;
        }
        if (isEmpty(passwordTV)) {
            passwordTV.setError("Required.");
            valid = false;
        }

        if (isEmpty(passwordConfirmTV)) {
            passwordConfirmTV.setError("Please confirm your password.");
            valid = false;
        }

        if (isEmpty(usernameTV)) {
            usernameTV.setError("Required.");
            valid = false;
        }

        if (passwordConfirmTV.getText() != null && passwordTV.getText() != null) {
            String pw1 = passwordTV.getText().toString();
            String pw2 = passwordConfirmTV.getText().toString();
            if (!pw1.equals(pw2)) {
                passwordConfirmTV.setError("Passwords must match");
                valid = false;
            }
        }
        return valid;
    }

    private boolean isEmpty(TextView tv) {
        return (tv.getText() == null || tv.getText().toString().equals(""));
    }
}