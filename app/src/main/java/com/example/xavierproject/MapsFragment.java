package com.example.xavierproject;

import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapsFragment";
    private GoogleMap mMap;
    private String cityName;
    private DatabaseReference reportsRef;
    private ProgressBar progressBar;
    private List<LatLng> reportLocations;
    private ValueEventListener valueEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_official_maps, container, false);

        // Initialize Firebase
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");
        reportLocations = new ArrayList<>();

        // Initialize progress bar
        progressBar = view.findViewById(R.id.progressBar);

        // Get city name from arguments
        if (getArguments() != null) {
            cityName = getArguments().getString("CITY_NAME", "Mumbai");
        } else {
            cityName = "Mumbai";
        }

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
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
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                        // Validate coordinates
                        if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0) {
                            LatLng location = new LatLng(latitude, longitude);
                            reportLocations.add(location);

                            // Get custom marker icon based on category
                            BitmapDescriptor markerIcon = getCustomMarkerIcon(category);

                            // Create marker title and snippet
                            String title = category != null ? category : "Report";
                            String snippet = description != null ?
                                    (description.length() > 50 ? description.substring(0, 50) + "..." : description)
                                    : "No description";

                            // Add status to snippet if available
                            if (status != null && !status.isEmpty()) {
                                snippet = "Status: " + status + "\n" + snippet;
                            }

                            // Add marker to map
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(location)
                                    .title(title)
                                    .snippet(snippet)
                                    .icon(markerIcon);

                            mMap.addMarker(markerOptions);
                            markerCount++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                // Show count of markers
                if (getContext() != null && markerCount > 0) {
                    Toast.makeText(getContext(), markerCount + " reports loaded on map", Toast.LENGTH_SHORT).show();
                }

                // Adjust camera to show all markers
                if (!reportLocations.isEmpty()) {
                    adjustCameraToMarkers();
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "No reports found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load reports: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        reportsRef.addValueEventListener(valueEventListener);
    }

    // Create custom colored marker pins
    private BitmapDescriptor getCustomMarkerIcon(String category) {
        int color = getColorByCategory(category);

        // Create a colored marker
        int width = 80;
        int height = 100;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        // Draw pin shape (teardrop)
        float centerX = width / 2f;
        float centerY = height / 3f;
        float radius = width / 2.5f;

        // Draw circle (top of pin)
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Draw triangle (bottom of pin)
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(centerX - radius * 0.5f, centerY + radius * 0.7f);
        path.lineTo(centerX, height - 10);
        path.lineTo(centerX + radius * 0.5f, centerY + radius * 0.7f);
        path.close();
        canvas.drawPath(path, paint);

        // Draw white border
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Draw inner circle
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius * 0.4f, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Get color based on category
    private int getColorByCategory(String category) {
        if (category == null) {
            return Color.parseColor("#F44336"); // Red
        }

        switch (category.toLowerCase().trim()) {
            case "road":
                return Color.parseColor("#FF9800"); // Orange
            case "water":
                return Color.parseColor("#2196F3"); // Blue
            case "electricity":
                return Color.parseColor("#FFEB3B"); // Yellow
            case "waste":
                return Color.parseColor("#4CAF50"); // Green
            case "safety":
                return Color.parseColor("#F44336"); // Red
            case "public transport":
                return Color.parseColor("#9C27B0"); // Purple
            case "infrastructure":
                return Color.parseColor("#795548"); // Brown
            case "health":
                return Color.parseColor("#E91E63"); // Pink
            default:
                return Color.parseColor("#607D8B"); // Blue Grey
        }
    }

    private void adjustCameraToMarkers() {
        if (reportLocations.isEmpty() || mMap == null) {
            return;
        }

        // Use Handler to delay the camera animation slightly to ensure map is ready
        if (getView() != null) {
            getView().postDelayed(() -> {
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1000, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback: center on first marker
                    if (!reportLocations.isEmpty()) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(reportLocations.get(0), 12));
                    }
                }
            }, 300);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to prevent memory leaks
        if (reportsRef != null && valueEventListener != null) {
            reportsRef.removeEventListener(valueEventListener);
        }
    }
}