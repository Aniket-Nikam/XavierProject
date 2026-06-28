package com.example.xavierproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private TextView tvCoordinates;
    private ImageButton btnBack;
    private Button btnOpenGoogleMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Get coordinates from intent
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);

        // Initialize views
        tvCoordinates = findViewById(R.id.tv_coordinates);
        btnBack = findViewById(R.id.btn_back);
        btnOpenGoogleMaps = findViewById(R.id.btn_open_google_maps);

        // Set coordinates text
        tvCoordinates.setText(String.format("Lat: %.6f, Lng: %.6f", latitude, longitude));

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Open in Google Maps button
        btnOpenGoogleMaps.setOnClickListener(v -> openInGoogleMaps());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (latitude != 0.0 && longitude != 0.0) {
            // Add marker at the location
            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Selected Location"));

            // Move camera to the location with zoom
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));

            // Enable zoom controls
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);

        } else {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show();
        }
    }

    private void openInGoogleMaps() {
        if (latitude != 0.0 && longitude != 0.0) {
            // Create URI for Google Maps
            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // If Google Maps not installed, open in browser
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(this, "Location coordinates not available", Toast.LENGTH_SHORT).show();
        }
    }
}