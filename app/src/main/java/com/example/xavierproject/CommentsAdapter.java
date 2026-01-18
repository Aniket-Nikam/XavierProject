package com.example.xavierproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;

    public CommentsAdapter(Context context) {
        this.context = context;
        this.comments = new ArrayList<>();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.userNameTextView.setText(comment.getUserName());
        holder.contentTextView.setText(comment.getContent());

        // Format timestamp
        String timeAgo = getTimeAgo(comment.getTimestamp());
        holder.timeTextView.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return comments.size();
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

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, contentTextView, timeTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.commentUserNameTextView);
            contentTextView = itemView.findViewById(R.id.commentContentTextView);
            timeTextView = itemView.findViewById(R.id.commentTimeTextView);
        }
    }
}