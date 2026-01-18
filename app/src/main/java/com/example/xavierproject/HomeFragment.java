package com.example.xavierproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView welcomeTextView, subtitleTextView;
    private CircleImageView profileImageView;
    private MaterialCardView discussionCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadUserData();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        subtitleTextView = view.findViewById(R.id.subtitleTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
        discussionCard = view.findViewById(R.id.discussionCard);
    }

    private void setupClickListeners() {
        discussionCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DiscussionActivity.class);
            startActivity(intent);
        });
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