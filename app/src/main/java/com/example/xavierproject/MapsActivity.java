package com.example.xavierproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private String cityName;
    private DatabaseReference reportsRef;
    private ProgressBar progressBar;
    private List<LatLng> reportLocations;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Firebase
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");
        reportLocations = new ArrayList<>();

        // Initialize progress bar
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Get city name from intent
        cityName = getIntent().getStringExtra("CITY_NAME");
        if (cityName == null) {
            cityName = "Mumbai";
        }

        // Set up action bar with back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(cityName + " - Reports Map");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready!");

        // Enable map controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        // Default Mumbai location
        LatLng defaultLocation = new LatLng(19.0760, 72.8777);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        // Load reports from Firebase
        loadReportsFromFirebase();
    }

    private void loadReportsFromFirebase() {
        Log.d(TAG, "Loading reports from Firebase...");

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Firebase data received. Children count: " + dataSnapshot.getChildrenCount());

                // Clear previous markers and locations
                if (mMap != null) {
                    mMap.clear();
                }
                reportLocations.clear();

                int markerCount = 0;

                // Loop through all reports
                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    try {
                        // Get report data
                        String reportId = reportSnapshot.getKey();
                        Double latitude = reportSnapshot.child("latitude").getValue(Double.class);
                        Double longitude = reportSnapshot.child("longitude").getValue(Double.class);
                        String category = reportSnapshot.child("category").getValue(String.class);
                        String description = reportSnapshot.child("description").getValue(String.class);
                        String status = reportSnapshot.child("status").getValue(String.class);
                        String title = reportSnapshot.child("title").getValue(String.class);

                        Log.d(TAG, "Report: " + reportId + " | Lat: " + latitude + " | Lng: " + longitude + " | Category: " + category);

                        // Validate coordinates
                        if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0) {
                            LatLng location = new LatLng(latitude, longitude);
                            reportLocations.add(location);

                            // Get marker color based on category
                            float markerColor = getMarkerColorByCategory(category);

                            // Create marker title
                            String markerTitle = title != null ? title : (category != null ? category : "Report");

                            // Create snippet
                            String snippet = "";
                            if (status != null && !status.isEmpty()) {
                                snippet = "Status: " + status;
                            }
                            if (description != null && !description.isEmpty()) {
                                if (!snippet.isEmpty()) {
                                    snippet += "\n";
                                }
                                snippet += description.length() > 50 ?
                                        description.substring(0, 50) + "..." : description;
                            }
                            if (snippet.isEmpty()) {
                                snippet = "No description";
                            }

                            // Add marker to map
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(location)
                                    .title(markerTitle)
                                    .snippet(snippet)
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

                            mMap.addMarker(markerOptions);
                            markerCount++;
                            Log.d(TAG, "Marker added: " + markerTitle + " at " + latitude + ", " + longitude);
                        } else {
                            Log.w(TAG, "Invalid coordinates for report: " + reportId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing report", e);
                        e.printStackTrace();
                    }
                }

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                Log.d(TAG, "Total markers added: " + markerCount);

                // Show count of markers
                if (markerCount > 0) {
                    Toast.makeText(MapsActivity.this, markerCount + " reports loaded", Toast.LENGTH_SHORT).show();

                    // Adjust camera to show all markers
                    adjustCameraToMarkers();
                } else {
                    Log.w(TAG, "No valid report locations found");
                    Toast.makeText(MapsActivity.this, "No reports found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase error: " + databaseError.getMessage());
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(MapsActivity.this,
                        "Failed to load reports: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        reportsRef.addValueEventListener(valueEventListener);
    }

    // Get marker color based on category
    private float getMarkerColorByCategory(String category) {
        if (category == null) {
            return BitmapDescriptorFactory.HUE_RED;
        }

        switch (category.toLowerCase().trim()) {
            case "road":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "water":
                return BitmapDescriptorFactory.HUE_AZURE;
            case "electricity":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "waste":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "safety":
                return BitmapDescriptorFactory.HUE_RED;
            case "public transport":
                return BitmapDescriptorFactory.HUE_VIOLET;
            case "infrastructure":
                return BitmapDescriptorFactory.HUE_ROSE;
            case "health":
                return BitmapDescriptorFactory.HUE_MAGENTA;
            default:
                return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void adjustCameraToMarkers() {
        if (reportLocations.isEmpty() || mMap == null) {
            return;
        }

        // Delay camera animation to ensure map is ready
        mMap.setOnMapLoadedCallback(() -> {
            try {
                if (reportLocations.size() == 1) {
                    // If only one marker, zoom to it
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(reportLocations.get(0), 15));
                } else {
                    // If multiple markers, fit all in view
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng location : reportLocations) {
                        builder.include(location);
                    }
                    LatLngBounds bounds = builder.build();

                    // Add padding (150 pixels) and animate camera
                    int padding = 150;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1500, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera", e);
                // Fallback: center on first marker
                if (!reportLocations.isEmpty()) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(reportLocations.get(0), 12));
                }
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to prevent memory leaks
        if (reportsRef != null && valueEventListener != null) {
            reportsRef.removeEventListener(valueEventListener);
        }
    }
}