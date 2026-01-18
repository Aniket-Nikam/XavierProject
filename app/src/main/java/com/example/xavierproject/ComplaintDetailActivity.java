package com.example.xavierproject;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ComplaintDetailActivity extends AppCompatActivity {

    private static final String TAG = "ComplaintDetail";
    private TextView titleTextView, categoryTextView, descriptionTextView, locationTextView, userEmailTextView;
    private ImageView complaintImageView;
    private Button btnPending, btnAcknowledged, btnOngoing, btnResolved, btnViewOnMap;
    private ProgressBar imageProgressBar;

    private String reportId;
    private String currentStatus;
    private String userId;
    private String userEmail;
    private String userName;
    private String complaintTitle;
    private String complaintDescription;
    private String locationText;
    private double latitude;
    private double longitude;
    private DatabaseReference reportRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Complaint Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initializeViews();
        loadComplaintData();
        setupStatusButtons();
    }

    private void initializeViews() {
        titleTextView = findViewById(R.id.detailTitleTextView);
        categoryTextView = findViewById(R.id.detailCategoryTextView);
        descriptionTextView = findViewById(R.id.detailDescriptionTextView);
        locationTextView = findViewById(R.id.detailLocationTextView);
        userEmailTextView = findViewById(R.id.detailUserEmailTextView);
        complaintImageView = findViewById(R.id.detailImageView);
        imageProgressBar = findViewById(R.id.imageProgressBar);

        btnPending = findViewById(R.id.btnPending);
        btnAcknowledged = findViewById(R.id.btnAcknowledged);
        btnOngoing = findViewById(R.id.btnOngoing);
        btnResolved = findViewById(R.id.btnResolved);
        btnViewOnMap = findViewById(R.id.btnViewOnMap);
    }

    private void loadComplaintData() {
        // Get data from intent
        reportId = getIntent().getStringExtra("REPORT_ID");
        complaintTitle = getIntent().getStringExtra("TITLE");
        String category = getIntent().getStringExtra("CATEGORY");
        complaintDescription = getIntent().getStringExtra("DESCRIPTION");
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        currentStatus = getIntent().getStringExtra("STATUS");
        userId = getIntent().getStringExtra("USER_ID"); // Get userId instead of email
        userName = getIntent().getStringExtra("USER_NAME");
        latitude = getIntent().getDoubleExtra("LATITUDE", 0.0);
        longitude = getIntent().getDoubleExtra("LONGITUDE", 0.0);

        // Set data to views
        titleTextView.setText(complaintTitle != null ? complaintTitle : "Untitled");
        categoryTextView.setText(category != null ? category : "N/A");
        descriptionTextView.setText(complaintDescription != null ? complaintDescription : "No description available");

        // Fetch user email from Firebase Auth Helper
        if (userId != null && !userId.isEmpty()) {
            fetchUserEmail(userId);
        } else {
            userEmailTextView.setVisibility(View.GONE);
        }

        // Get location from coordinates
        if (latitude != 0.0 && longitude != 0.0) {
            getAddressFromCoordinates(latitude, longitude);
            btnViewOnMap.setEnabled(true);
        } else {
            locationText = "Location not available";
            locationTextView.setText(locationText);
            btnViewOnMap.setEnabled(false);
        }

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageProgressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(complaintImageView);
            imageProgressBar.setVisibility(View.GONE);
        } else {
            complaintImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // Initialize Firebase reference
        reportRef = FirebaseDatabase.getInstance().getReference("reports").child(reportId);

        // Highlight current status
        updateStatusButtons(currentStatus);
    }

    private void fetchUserEmail(String userId) {
        userEmailTextView.setVisibility(View.VISIBLE);
        userEmailTextView.setText("Fetching user email...");

        FirebaseAuthHelper.getUserEmailByUserId(userId, new FirebaseAuthHelper.EmailCallback() {
            @Override
            public void onEmailFetched(String email) {
                userEmail = email;
                userEmailTextView.setText("User: " + email);
                Log.d(TAG, "User email fetched: " + email);
            }

            @Override
            public void onEmailNotFound() {
                userEmail = null;
                userEmailTextView.setText("User email not available");
                userEmailTextView.setVisibility(View.GONE);
                Log.w(TAG, "User email not found for userId: " + userId);
            }
        });
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        // Show loading state
        locationTextView.setText("Loading location...");

        // Run geocoding in background thread
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    // Build location string
                    StringBuilder locationBuilder = new StringBuilder();

                    // Add address line
                    if (address.getSubLocality() != null) {
                        locationBuilder.append(address.getSubLocality()).append(", ");
                    } else if (address.getLocality() != null) {
                        locationBuilder.append(address.getLocality()).append(", ");
                    }

                    // Add city
                    if (address.getLocality() != null && address.getSubLocality() != null) {
                        locationBuilder.append(address.getLocality()).append(", ");
                    }

                    // Add state
                    if (address.getAdminArea() != null) {
                        locationBuilder.append(address.getAdminArea());
                    }

                    locationText = locationBuilder.toString();
                    if (locationText.isEmpty()) {
                        locationText = latitude + ", " + longitude;
                    }

                    // Update UI on main thread
                    runOnUiThread(() -> locationTextView.setText(locationText));

                } else {
                    // If geocoding fails, show coordinates
                    locationText = latitude + ", " + longitude;
                    runOnUiThread(() -> locationTextView.setText(locationText));
                }

            } catch (IOException e) {
                Log.e(TAG, "Geocoding error: " + e.getMessage());
                // Show coordinates if geocoding fails
                locationText = latitude + ", " + longitude;
                runOnUiThread(() -> locationTextView.setText(locationText));
            }
        }).start();
    }

    private void setupStatusButtons() {
        btnPending.setOnClickListener(v -> confirmStatusUpdate("pending"));
        btnAcknowledged.setOnClickListener(v -> confirmStatusUpdate("acknowledged"));
        btnOngoing.setOnClickListener(v -> confirmStatusUpdate("ongoing"));
        btnResolved.setOnClickListener(v -> confirmStatusUpdate("resolved"));

        // View on Map button
        btnViewOnMap.setOnClickListener(v -> openLocationInMaps());
    }

    private void confirmStatusUpdate(String newStatus) {
        if (newStatus.equals(currentStatus)) {
            Toast.makeText(this, "Status is already " + newStatus, Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Update complaint status to " + newStatus.toUpperCase() + "?";
        if (userEmail != null && !userEmail.isEmpty()) {
            message += "\n\nAn email notification will be sent to the user.";
        } else {
            message += "\n\nNote: User email not available, no notification will be sent.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setMessage(message)
                .setPositiveButton("Update", (dialog, which) -> updateStatus(newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

// Replace your existing openLocationInMaps() method with this:

    private void openLocationInMaps() {
        if (latitude != 0.0 && longitude != 0.0) {
            // Create intent to open MapViewActivity
            Intent intent = new Intent(this, MapViewActivity.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Location coordinates not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(String newStatus) {
        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setMessage("Updating status" + (userEmail != null ? " and sending email..." : "..."))
                .setCancelable(false)
                .create();
        progressDialog.show();

        String oldStatus = currentStatus;

        // Update status in Firebase
        reportRef.child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    currentStatus = newStatus;
                    updateStatusButtons(newStatus);

                    // Send email notification if user email is available
                    if (userEmail != null && !userEmail.isEmpty()) {
                        sendEmailNotification(oldStatus, newStatus, progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ComplaintDetailActivity.this,
                                "Status updated to: " + newStatus + "\n(No email sent - user email not available)",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ComplaintDetailActivity.this,
                            "Failed to update status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEmailNotification(String oldStatus, String newStatus, AlertDialog progressDialog) {
        String userNameToSend = userName != null ? userName : "User";
        String locationToSend = locationText != null ? locationText : "Location not specified";

        MailgunEmailService.sendStatusUpdateEmail(
                userEmail,
                userNameToSend,
                complaintTitle,
                complaintDescription,
                locationToSend,
                oldStatus,
                newStatus,
                new MailgunEmailService.EmailCallback() {
                    @Override
                    public void onSuccess(String message) {
                        progressDialog.dismiss();
                        Toast.makeText(ComplaintDetailActivity.this,
                                "Status updated and email sent successfully!",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Email notification sent to: " + userEmail);
                    }

                    @Override
                    public void onFailure(String error) {
                        progressDialog.dismiss();
                        Toast.makeText(ComplaintDetailActivity.this,
                                "Status updated but email failed: " + error,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to send email: " + error);
                    }
                }
        );
    }

    private void updateStatusButtons(String status) {
        // Reset all buttons to default state
        btnPending.setBackgroundTintList(getColorStateList(R.color.button_default));
        btnAcknowledged.setBackgroundTintList(getColorStateList(R.color.button_default));
        btnOngoing.setBackgroundTintList(getColorStateList(R.color.button_default));
        btnResolved.setBackgroundTintList(getColorStateList(R.color.button_default));

        // Highlight selected status
        if (status != null) {
            switch (status.toLowerCase()) {
                case "pending":
                    btnPending.setBackgroundTintList(getColorStateList(R.color.status_pending));
                    break;
                case "acknowledged":
                    btnAcknowledged.setBackgroundTintList(getColorStateList(R.color.accent));
                    break;
                case "ongoing":
                    btnOngoing.setBackgroundTintList(getColorStateList(R.color.status_in_progress));
                    break;
                case "resolved":
                    btnResolved.setBackgroundTintList(getColorStateList(R.color.status_resolved));
                    break;
            }
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