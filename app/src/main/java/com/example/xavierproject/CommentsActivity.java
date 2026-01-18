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

public class CommentsActivity extends AppCompatActivity {

    private TextView postContentTextView, emptyTextView;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private EditText commentEditText;
    private ImageButton sendCommentButton;
    private ProgressBar progressBar;

    private String postId;
    private DatabaseReference commentsRef;
    private DatabaseReference postRef;
    private FirebaseAuth mAuth;
    private ValueEventListener commentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Comments");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        postId = getIntent().getStringExtra("POST_ID");
        String postContent = getIntent().getStringExtra("POST_CONTENT");

        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postId);
        postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        initializeViews();
        if (postContent != null) {
            postContentTextView.setText(postContent);
        }
        loadComments();
        setupCommentButton();
    }

    private void initializeViews() {
        postContentTextView = findViewById(R.id.postContentTextView);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentEditText = findViewById(R.id.commentEditText);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter(this);
        commentsRecyclerView.setAdapter(commentsAdapter);
    }

    private void setupCommentButton() {
        sendCommentButton.setOnClickListener(v -> {
            String content = commentEditText.getText().toString().trim();
            if (!content.isEmpty()) {
                createComment(content);
            } else {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createComment(String content) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = commentsRef.push().getKey();
        if (commentId == null) return;

        String userName = user.getDisplayName() != null ? user.getDisplayName() : "Anonymous";
        long timestamp = System.currentTimeMillis();

        Comment comment = new Comment(commentId, postId, user.getUid(), userName, content, timestamp);

        commentsRef.child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    commentEditText.setText("");
                    // Update comment count
                    updateCommentCount();
                    Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCommentCount() {
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                postRef.child("commentsCount").setValue(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        commentsRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();

                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        comments.add(comment);
                    }
                }

                // Sort by timestamp (oldest first for comments)
                Collections.sort(comments, (c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()));

                progressBar.setVisibility(View.GONE);

                if (comments.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    commentsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    commentsRecyclerView.setVisibility(View.VISIBLE);
                    commentsAdapter.setComments(comments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CommentsActivity.this, "Error loading comments", Toast.LENGTH_SHORT).show();
            }
        };

        commentsRef.addValueEventListener(commentsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsRef != null && commentsListener != null) {
            commentsRef.removeEventListener(commentsListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}