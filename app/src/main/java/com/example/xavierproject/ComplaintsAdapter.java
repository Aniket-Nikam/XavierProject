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

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.ComplaintViewHolder> {

    private Context context;
    private List<Complaint> complaints;

    public ComplaintsAdapter(Context context) {
        this.context = context;
        this.complaints = new ArrayList<>();
    }

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaints.get(position);

        holder.idTextView.setText(complaint.getId());
        holder.titleTextView.setText(complaint.getTitle());
        holder.descriptionTextView.setText(complaint.getDescription());
        holder.statusTextView.setText(complaint.getStatus());
        holder.dateTextView.setText(complaint.getDate());
        holder.locationTextView.setText(complaint.getLocation());

        // Set status color
        int statusColor;
        switch (complaint.getStatus()) {
            case "Resolved":
                statusColor = context.getColor(R.color.status_resolved);
                break;
            case "In Progress":
                statusColor = context.getColor(R.color.status_in_progress);
                break;
            default: // Pending
                statusColor = context.getColor(R.color.status_pending);
                break;
        }
        holder.statusTextView.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView idTextView, titleTextView, descriptionTextView, statusTextView, dateTextView, locationTextView;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.complaintIdTextView);
            titleTextView = itemView.findViewById(R.id.complaintTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.complaintDescriptionTextView);
            statusTextView = itemView.findViewById(R.id.complaintStatusTextView);
            dateTextView = itemView.findViewById(R.id.complaintDateTextView);
            locationTextView = itemView.findViewById(R.id.complaintLocationTextView);
        }
    }
}