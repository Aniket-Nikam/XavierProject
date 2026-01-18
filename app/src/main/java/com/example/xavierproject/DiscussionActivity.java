package com.example.xavierproject;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscussionActivity extends AppCompatActivity {

    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private EditText newPostEditText;
    private ImageButton sendPostButton;

    private DatabaseReference postsRef;
    private FirebaseAuth mAuth;
    private ValueEventListener postsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Community Discussion");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        initializeViews();
        loadPosts();
        setupPostButton();
    }

    private void initializeViews() {
        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        newPostEditText = findViewById(R.id.newPostEditText);
        sendPostButton = findViewById(R.id.sendPostButton);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(this, mAuth.getCurrentUser());
        postsRecyclerView.setAdapter(postsAdapter);
    }

    private void setupPostButton() {
        sendPostButton.setOnClickListener(v -> {
            String content = newPostEditText.getText().toString().trim();
            if (!content.isEmpty()) {
                createPost(content);
            } else {
                Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPost(String content) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String postId = postsRef.push().getKey();
        if (postId == null) return;

        String userName = user.getDisplayName() != null ? user.getDisplayName() : "Anonymous";
        long timestamp = System.currentTimeMillis();

        Post post = new Post(postId, user.getUid(), userName, content, timestamp);

        postsRef.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    newPostEditText.setText("");
                    Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        postsRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> posts = new ArrayList<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        posts.add(post);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(posts, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

                progressBar.setVisibility(View.GONE);

                if (posts.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    postsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    postsRecyclerView.setVisibility(View.VISIBLE);
                    postsAdapter.setPosts(posts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DiscussionActivity.this, "Error loading posts", Toast.LENGTH_SHORT).show();
            }
        };

        postsRef.addValueEventListener(postsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postsRef != null && postsListener != null) {
            postsRef.removeEventListener(postsListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}