package com.example.xavierproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    private View rootView;
    private String loginType;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get login type from intent
        loginType = getIntent().getStringExtra("LOGIN_TYPE");
        if (loginType == null) {
            loginType = "user";
        }

        // Set the appropriate layout based on login type
        setLayout();

        initializeViews();
        setupFirebase();
        setupClickListeners();
    }

    private void setLayout() {
        switch (loginType) {
            case "admin":
                setContentView(R.layout.activity_admin_login);
                break;
            case "government":
                setContentView(R.layout.activity_gov_login);
                break;
            default:
                setContentView(R.layout.activity_login);
                break;
        }
    }

    private void initializeViews() {
        rootView = findViewById(android.R.id.content);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> loginWithEmail());
        forgotPasswordTextView.setOnClickListener(v -> resetPassword());
    }

    private void loginWithEmail() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (!user.isEmailVerified()) {
                                Snackbar.make(rootView,
                                                "Please verify your email for full access",
                                                Snackbar.LENGTH_LONG)
                                        .setAction("Resend", v -> resendVerificationEmail(user))
                                        .show();
                            }
                            navigateToMainActivity();
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        handleLoginError(task.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage = "Login failed. Please check your credentials.";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email. Please sign up.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Incorrect password. Please try again.";
        }

        Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG).show();
    }

    private void resendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter your email");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter valid email");
            emailEditText.requestFocus();
            return;
        }

        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Snackbar.make(rootView, "Password reset email sent to " + email,
                                Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(rootView, "Failed to send reset email. Check if email exists.",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter valid email");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
    }

    private void navigateToMainActivity() {
        Intent intent;

        // Navigate to different MainActivity based on login type
        switch (loginType) {
            case "admin":
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                break;
            case "government":
                intent = new Intent(LoginActivity.this, GovMainActivity.class);
                break;
            default:
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}