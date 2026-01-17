package com.example.xavierproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView welcomeTextView, subtitleTextView;
    private CircleImageView profileImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        subtitleTextView = view.findViewById(R.id.subtitleTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                welcomeTextView.setText("Hello, " + displayName + "!");
            } else {
                welcomeTextView.setText("Hello!");
            }

            String email = user.getEmail();
            if (email != null) {
                subtitleTextView.setText(email);
            }
        }
    }
}