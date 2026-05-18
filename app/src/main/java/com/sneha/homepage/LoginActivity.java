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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.sneha.homepage.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPass;
    private TextView fpass;
    private Button signInBtn, testLoginBtn;
    private FirebaseAuth mAuth;

    ImageView google;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for changing status bar icon color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.leditTextEmail);
        mPass = findViewById(R.id.leditTextPassword);
        fpass = findViewById(R.id.forgot);
        signInBtn = findViewById(R.id.circularButton);

        // Add this button in your layout first
        // testLoginBtn = findViewById(R.id.testLoginBtn);

        mAuth = FirebaseAuth.getInstance();

        // Debug Firebase setup
        Log.d("FIREBASE_LOGIN", "=== DEBUG LOGIN FIREBASE SETUP ===");
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d("FIREBASE_LOGIN", "FirebaseApp name: " + app.getName());
            Log.d("FIREBASE_LOGIN", "Project ID: " + app.getOptions().getProjectId());
            Log.d("FIREBASE_LOGIN", "Current user: " + (mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "null"));
        } catch (Exception e) {
            Log.e("FIREBASE_LOGIN", "Firebase not initialized: " + e.getMessage());
            e.printStackTrace();
        }

        // Check internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d("FIREBASE_LOGIN", "Internet connected: " + isConnected);

        google = findViewById(R.id.google);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();

        gsc = GoogleSignIn.getClient(this, gso);

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Optional: Test local login button
        /*
        testLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserLocal();
            }
        });
        */

        fpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });
    }

    private void SignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                HomeActivity();
            } catch (ApiException e) {
                Log.e("GOOGLE_SIGNIN", "Google sign-in error: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Google Sign-In Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void HomeActivity() {
        finish();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    private void loginUser() {
        String email = mEmail.getText().toString();
        String pass = mPass.getText().toString();

        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!pass.isEmpty()) {
                Log.d("FIREBASE_LOGIN", "Attempting Firebase login for: " + email);

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d("FIREBASE_LOGIN", "Login successful! User: " + authResult.getUser().getEmail());
                                Toast.makeText(LoginActivity.this, "Login Successfully !!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String errorMsg = "Login Failed: " + e.getMessage();
                                Log.e("FIREBASE_LOGIN", errorMsg);
                                e.printStackTrace();

                                // Provide helpful error messages
                                if (e.getMessage().contains("password is invalid")) {
                                    errorMsg = "Invalid password";
                                } else if (e.getMessage().contains("no user record")) {
                                    errorMsg = "No account found with this email";
                                } else if (e.getMessage().contains("network error")) {
                                    errorMsg = "Network error. Check your internet";
                                } else if (e.getMessage().contains("CONFIGURATION_NOT_FOUND")) {
                                    errorMsg = "Firebase not configured. Try local login";
                                }

                                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                                // Suggest local login if Firebase fails
                                SharedPreferences prefs = getSharedPreferences("local_users", MODE_PRIVATE);
                                if (prefs.contains("email")) {
                                    Toast.makeText(LoginActivity.this, "Try 'Local Login' button", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                mPass.setError("Password cannot be empty");
            }
        } else if (email.isEmpty()) {
            mEmail.setError("Email cannot be empty");
        } else {
            mEmail.setError("Please enter a valid email");
        }
    }

    // Alternative: Local login without Firebase
    private void loginUserLocal() {
        String email = mEmail.getText().toString();
        String pass = mPass.getText().toString();

        if (email.isEmpty()) {
            mEmail.setError("Email cannot be empty");
            return;
        }

        if (pass.isEmpty()) {
            mPass.setError("Password cannot be empty");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Please enter a valid email");
            return;
        }

        // Check against locally stored users
        SharedPreferences prefs = getSharedPreferences("local_users", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "");
        String savedPass = prefs.getString("password", "");
        String savedName = prefs.getString("name", "User");

        Log.d("LOCAL_LOGIN", "Checking: " + email + " vs " + savedEmail);

        if (email.equals(savedEmail) && pass.equals(savedPass)) {
            Log.d("LOCAL_LOGIN", "Local login successful for: " + email);

            // Mark as logged in
            prefs.edit().putBoolean("is_logged_in", true).apply();

            Toast.makeText(LoginActivity.this, "Welcome back " + savedName + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();

            // Show what's stored (for debugging)
            Log.d("LOCAL_LOGIN", "Stored email: " + savedEmail);
            Log.d("LOCAL_LOGIN", "Stored password length: " + (savedPass != null ? savedPass.length() : 0));
        }
    }

    // Check if user is already logged in (on app start)
    private void checkExistingLogin() {
        // Check Firebase first
        if (mAuth.getCurrentUser() != null) {
            Log.d("LOGIN_CHECK", "Firebase user already logged in: " + mAuth.getCurrentUser().getEmail());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Check local login
        SharedPreferences prefs = getSharedPreferences("local_users", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            Log.d("LOGIN_CHECK", "Local user already logged in");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkExistingLogin();
    }
}