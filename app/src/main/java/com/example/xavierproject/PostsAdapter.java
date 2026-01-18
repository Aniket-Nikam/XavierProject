package com.example.xavierproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private Context context;
    private List<Post> posts;
    private FirebaseUser currentUser;
    private DatabaseReference postsRef;

    public PostsAdapter(Context context, FirebaseUser currentUser) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.currentUser = currentUser;
        this.postsRef = FirebaseDatabase.getInstance().getReference("posts");
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.userNameTextView.setText(post.getUserName());
        holder.contentTextView.setText(post.getContent());
        holder.upvotesTextView.setText(String.valueOf(post.getUpvotes()));
        holder.commentsTextView.setText(String.valueOf(post.getCommentsCount()));

        // Format timestamp
        String timeAgo = getTimeAgo(post.getTimestamp());
        holder.timeTextView.setText(timeAgo);

        // Check if current user has upvoted
        boolean hasUpvoted = post.getUpvotedBy() != null &&
                currentUser != null &&
                post.getUpvotedBy().containsKey(currentUser.getUid());

        holder.upvoteButton.setImageResource(hasUpvoted ?
                R.drawable.ic_upvote_filled : R.drawable.ic_upvote);

        // Upvote button click
        holder.upvoteButton.setOnClickListener(v -> {
            if (currentUser == null) return;

            DatabaseReference postRef = postsRef.child(post.getPostId());
            DatabaseReference upvotedByRef = postRef.child("upvotedBy").child(currentUser.getUid());

            if (hasUpvoted) {
                // Remove upvote
                upvotedByRef.removeValue();
                postRef.child("upvotes").setValue(post.getUpvotes() - 1);
            } else {
                // Add upvote
                upvotedByRef.setValue(true);
                postRef.child("upvotes").setValue(post.getUpvotes() + 1);
            }
        });

        // Comment button click - open comments activity
        holder.commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("POST_ID", post.getPostId());
            intent.putExtra("POST_CONTENT", post.getContent());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "just now";
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, contentTextView, timeTextView, upvotesTextView, commentsTextView;
        ImageButton upvoteButton, commentButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.postUserNameTextView);
            contentTextView = itemView.findViewById(R.id.postContentTextView);
            timeTextView = itemView.findViewById(R.id.postTimeTextView);
            upvotesTextView = itemView.findViewById(R.id.upvotesTextView);
            commentsTextView = itemView.findViewById(R.id.commentsTextView);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            commentButton = itemView.findViewById(R.id.commentButton);
        }
    }
}