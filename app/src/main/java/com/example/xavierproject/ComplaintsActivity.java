package com.example.xavierproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintsActivity extends AppCompatActivity {

    private static final String TAG = "ComplaintsActivity";
    private RecyclerView complaintsRecyclerView;
    private ComplaintsAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private String cityName;

    private DatabaseReference reportsRef;
    private ValueEventListener reportsListener;

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
        loadComplaintsFromFirebase();
    }

    private void initializeViews() {
        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        complaintsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(this, complaint -> {
            // Handle item click - open detail activity
            Intent intent = new Intent(ComplaintsActivity.this, ComplaintDetailActivity.class);
            intent.putExtra("REPORT_ID", complaint.getReportId()); // Use the actual Firebase ID
            intent.putExtra("TITLE", complaint.getTitle());
            intent.putExtra("CATEGORY", complaint.getCategory());
            intent.putExtra("DESCRIPTION", complaint.getDescription());
            intent.putExtra("LATITUDE", complaint.getLatitude());
            intent.putExtra("LONGITUDE", complaint.getLongitude());
            intent.putExtra("IMAGE_URL", complaint.getImageUrl());
            intent.putExtra("STATUS", complaint.getStatus());
            startActivity(intent);
        });
        complaintsRecyclerView.setAdapter(adapter);
    }

    private void loadComplaintsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        complaintsRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        // Initialize Firebase Database reference
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");

        // Create listener for real-time updates
        reportsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Complaint> complaints = new ArrayList<>();

                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    try {

                        // Get the report ID (key) - this will be used for updating status
                        String actualReportId = reportSnapshot.getKey();
                        String reportId = reportSnapshot.getKey();

                        // Parse Firebase data
                        String category = reportSnapshot.child("category").getValue(String.class);
                        String description = reportSnapshot.child("description").getValue(String.class);
                        String imageUrl = reportSnapshot.child("imageUrl").getValue(String.class);
                        String status = reportSnapshot.child("status").getValue(String.class);
                        String title = reportSnapshot.child("title").getValue(String.class);
                        String userId = reportSnapshot.child("userId").getValue(String.class);
                        String userName = reportSnapshot.child("userName").getValue(String.class);

                        // Get numeric values
                        Double latitude = reportSnapshot.child("latitude").getValue(Double.class);
                        Double longitude = reportSnapshot.child("longitude").getValue(Double.class);
                        Long timestamp = reportSnapshot.child("timestamp").getValue(Long.class);

                        // Create Complaint object
                        Complaint complaint = new Complaint();
                        complaint.setReportId(actualReportId != null ? actualReportId : "N/A"); // Store actual ID for Firebase updates
                        complaint.setId(userName != null ? userName : "Anonymous"); // Display username instead
                        complaint.setTitle(title != null ? title : "Untitled");
                        complaint.setDescription(description != null ? description : "No description");
                        complaint.setStatus(status != null ? status : "pending");
                        complaint.setCategory(category != null ? category : "Other");
                        complaint.setImageUrl(imageUrl);
                        complaint.setUserId(userId);
                        complaint.setUserName(userName != null ? userName : "Anonymous");

                        if (latitude != null) complaint.setLatitude(latitude);
                        if (longitude != null) complaint.setLongitude(longitude);
                        if (timestamp != null) {
                            complaint.setTimestamp(timestamp);
                            // Convert timestamp to readable date
                            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(new Date(timestamp));
                            complaint.setDate(dateStr);
                        } else {
                            complaint.setDate("N/A");
                        }

                        // Set location based on category or coordinates
                        complaint.setLocation(category != null ? category : "Unknown Location");

                        complaints.add(complaint);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing complaint: " + e.getMessage());
                    }
                }

                // Update UI
                progressBar.setVisibility(View.GONE);

                if (complaints.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    complaintsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    complaintsRecyclerView.setVisibility(View.VISIBLE);
                    adapter.setComplaints(complaints);
                }

                Log.d(TAG, "Loaded " + complaints.size() + " complaints from Firebase");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Error loading complaints");

                Toast.makeText(ComplaintsActivity.this,
                        "Failed to load complaints: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();

                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        };

        // Attach the listener
        reportsRef.addValueEventListener(reportsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (reportsRef != null && reportsListener != null) {
            reportsRef.removeEventListener(reportsListener);
        }
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