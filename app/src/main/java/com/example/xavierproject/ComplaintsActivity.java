package com.example.xavierproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ComplaintsActivity extends AppCompatActivity {

    private RecyclerView complaintsRecyclerView;
    private ComplaintsAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compaints);

        cityName = getIntent().getStringExtra("CITY_NAME");
        if (cityName == null) {
            cityName = "Mumbai";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(cityName + " - Complaints");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initializeViews();
        loadComplaints();
    }

    private void initializeViews() {
        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        complaintsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(this);
        complaintsRecyclerView.setAdapter(adapter);
    }

    private void loadComplaints() {
        progressBar.setVisibility(View.VISIBLE);
        complaintsRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        // Simulate loading with dummy data
        new android.os.Handler().postDelayed(() -> {
            List<Complaint> complaints = getDummyComplaints();

            progressBar.setVisibility(View.GONE);

            if (complaints.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                complaintsRecyclerView.setVisibility(View.VISIBLE);
                adapter.setComplaints(complaints);
            }
        }, 1000);
    }

    private List<Complaint> getDummyComplaints() {
        List<Complaint> complaints = new ArrayList<>();

        complaints.add(new Complaint(
                "C001",
                "Road Damage",
                "Large pothole on SV Road near Station",
                "Pending",
                "2024-01-15",
                "Andheri West"
        ));

        complaints.add(new Complaint(
                "C002",
                "Street Light Not Working",
                "Multiple street lights are not working in the area",
                "In Progress",
                "2024-01-14",
                "Bandra East"
        ));

        complaints.add(new Complaint(
                "C003",
                "Garbage Collection Issue",
                "Garbage not collected for 3 days",
                "Resolved",
                "2024-01-13",
                "Powai"
        ));

        complaints.add(new Complaint(
                "C004",
                "Water Supply Problem",
                "No water supply since morning",
                "Pending",
                "2024-01-16",
                "Malad West"
        ));

        complaints.add(new Complaint(
                "C005",
                "Drainage Blockage",
                "Severe waterlogging due to blocked drain",
                "In Progress",
                "2024-01-15",
                "Goregaon"
        ));

        return complaints;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}