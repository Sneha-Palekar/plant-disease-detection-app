package com.sneha.homepage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sneha.homepage.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmail, mPass, username, phone;
    private Button signUpBtn, testRegisterBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        mEmail = findViewById(R.id.reditTextEmail);
        mPass = findViewById(R.id.reditTextPassword);
        username = findViewById(R.id.reditTextName);
        phone = findViewById(R.id.reditTextMobile);
        signUpBtn = findViewById(R.id.cirRegisterButton);

        // Add test button in your layout first, then uncomment:
        // testRegisterBtn = findViewById(R.id.testRegisterBtn);

        mAuth = FirebaseAuth.getInstance();

        // Debug Firebase setup
        Log.d("FIREBASE", "=== DEBUG FIREBASE SETUP ===");
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d("FIREBASE", "FirebaseApp name: " + app.getName());
            Log.d("FIREBASE", "Project ID: " + app.getOptions().getProjectId());
        } catch (Exception e) {
            Log.e("FIREBASE", "Firebase not initialized: " + e.getMessage());
            e.printStackTrace();
        }

        // Check internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d("FIREBASE", "Internet connected: " + isConnected);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newuser();
            }
        });

        // Optional: Test button to bypass Firebase
        /*
        testRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testLocalRegistration();
            }
        });
        */

        changeStatusBarColor();
    }

    public void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void newuser() {
        String email = mEmail.getText().toString().trim();
        String pass = mPass.getText().toString();
        String name = username.getText().toString();
        String mobile = phone.getText().toString();

        // Validation
        if (name.isEmpty()) {
            username.setError("Enter the full name");
            username.requestFocus();
            return;
        }

        if (mobile.isEmpty()) {
            phone.setError("Enter the mobile number");
            phone.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            mEmail.setError("Empty Fields Are not Allowed");
            mEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Please enter the Email Correctly!!");
            mEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            mPass.setError("Empty Fields Are not Allowed");
            mPass.requestFocus();
            return;
        }

        if (pass.length() < 6) {
            mPass.setError("Enter password of at least 6 characters");
            mPass.requestFocus();
            return;
        }

        Log.d("FIREBASE", "Attempting to register: " + email);

        // Firebase Registration with detailed error handling
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("FIREBASE", "Auth successful! User ID: " + mAuth.getCurrentUser().getUid());

                            User user = new User(name, mobile, email);

                            FirebaseDatabase.getInstance().getReference("User")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("FIREBASE", "User data saved to database");
                                                Toast.makeText(RegisterActivity.this, "Registered Successfully !!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                // Database error
                                                String errorMsg = "Database Error";
                                                if (task.getException() != null) {
                                                    errorMsg = task.getException().getMessage();
                                                    Log.e("FIREBASE_DB", "Database error: " + errorMsg);
                                                    task.getException().printStackTrace();
                                                }
                                                Toast.makeText(RegisterActivity.this, "Database Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            // Authentication error
                            String errorMsg = "Registration Failed";
                            if (task.getException() != null) {
                                errorMsg = task.getException().getMessage();
                                Log.e("FIREBASE_AUTH", "Auth error: " + errorMsg);
                                task.getException().printStackTrace();

                                // Common errors and fixes
                                if (errorMsg.contains("CONFIGURATION_NOT_FOUND")) {
                                    errorMsg += "\nFix: Add SHA-1 to Firebase & enable Email/Password auth";
                                } else if (errorMsg.contains("network error") || errorMsg.contains("timeout")) {
                                    errorMsg += "\nFix: Check internet connection";
                                } else if (errorMsg.contains("already in use")) {
                                    errorMsg += "\nEmail already registered";
                                }
                            }
                            Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Alternative: Local registration without Firebase
    private void testLocalRegistration() {
        String email = mEmail.getText().toString().trim();
        String pass = mPass.getText().toString();
        String name = username.getText().toString();
        String mobile = phone.getText().toString();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences (local storage)
        SharedPreferences prefs = getSharedPreferences("local_users", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("name", name);
        editor.putString("phone", mobile);
        editor.putString("password", pass); // Note: In real app, encrypt this!
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        Log.d("LOCAL_AUTH", "User saved locally: " + email);
        Toast.makeText(this, "Registered locally! (Firebase bypassed)", Toast.LENGTH_SHORT).show();

        // Go to login
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}