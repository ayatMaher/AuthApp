package com.example.authapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    EditText editTextEmail;
    EditText editTextPass;
    Button btnLog;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    TextView signbtn;
    String userAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editEmailLog);
        editTextPass = findViewById(R.id.editPassLog);
        btnLog = findViewById(R.id.btnLog);
        progressBar = findViewById(R.id.progressBarLog);
        signbtn = findViewById(R.id.sign);
        signbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
        btnLog.setOnClickListener(v -> getUser());
    }

    public void getUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = editTextEmail.getText().toString();
        String password = editTextPass.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Your Email is Empty , Fill it", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Your Password is Empty , Fill it", Toast.LENGTH_SHORT).show();
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        assert firebaseUser != null;
                        userAuth = firebaseUser.getUid();
                        Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("userAuth", userAuth);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}