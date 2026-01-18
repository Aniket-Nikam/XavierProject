package com.example.xavierproject;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class LoginTypeActivity extends AppCompatActivity {

    private MaterialCardView userLoginCard, adminLoginCard, govLoginCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_type);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        userLoginCard = findViewById(R.id.userLoginCard);
        adminLoginCard = findViewById(R.id.adminLoginCard);
        govLoginCard = findViewById(R.id.govLoginCard);
    }

    private void setupClickListeners() {
        // User Login
        userLoginCard.setOnClickListener(v -> {
            Intent intent = new Intent(LoginTypeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Admin Login
        adminLoginCard.setOnClickListener(v -> {
            Intent intent = new Intent(LoginTypeActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        // Government Login
        govLoginCard.setOnClickListener(v -> {
            Intent intent = new Intent(LoginTypeActivity.this, GovLoginActivity.class);
            startActivity(intent);
        });
    }
}