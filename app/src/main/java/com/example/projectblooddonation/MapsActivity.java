package com.example.projectblooddonation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.projectblooddonation.databinding.ActivityMapsBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog dialog;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        db = FirebaseFirestore.getInstance();
        // Fetch donation sites from Firestore
        db.collection("donation_sites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String siteId = doc.getId(); // Get the document ID (site ID)
                        String name = doc.getString("name");
                        String address = doc.getString("address");
                        GeoPoint location = doc.getGeoPoint("location");

                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Marker marker =mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .snippet(address));
                            if (marker != null) {
                                // Set the site ID as a tag to the marker
                                marker.setTag(siteId);
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MapsActivity.this, "Failed to fetch sites: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        getCurrentLocation();
        mMap.setOnMarkerClickListener(marker -> {
            String siteId = (String) marker.getTag(); // Assuming you set site ID as a tag to the marker.
            showSitePopup(siteId);
            return true;
        });
    }

    private void showSitePopup(String siteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_site_details, null);

        TextView nameTextView = view.findViewById(R.id.siteNameText);
        TextView addressTextView = view.findViewById(R.id.siteAddressText);
        TextView durationTextView = view.findViewById(R.id.siteDurationText);
        TextView dateTextView = view.findViewById(R.id.siteDateText);
        Button registerSelfButton = view.findViewById(R.id.registerSelfButton);
        Button registerOtherButton = view.findViewById(R.id.registerOtherButton);

        // Check if user is already registered.
        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            // Fetch current user's blood type
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            db.collection("donation_sites").document(siteId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // Fill site details.
                                            nameTextView.setText(documentSnapshot.getString("name"));
                                            addressTextView.setText(documentSnapshot.getString("address"));
                                            durationTextView.setText(documentSnapshot.getString("startTime") + " - " + documentSnapshot.getString("endTime"));
                                            dateTextView.setText(documentSnapshot.getString("date"));

                                            // Access the registeredDonors field as a list of strings
                                            List<String> registeredDonors = (List<String>) documentSnapshot.get("registeredDonors");
                                                // User's blood type is eligible
                                                if (registeredDonors != null && registeredDonors.contains(currentUserId)) {
                                                    // User is already registered
                                                    registerSelfButton.setText("Cancel registration");
                                                    registerSelfButton.setOnClickListener(v -> cancelRegistration(siteId, currentUserId));
                                                } else {
                                                    // User is not registered
                                                    registerSelfButton.setText("Register myself");
                                                    registerSelfButton.setOnClickListener(v -> registerSelf(siteId, currentUserId));
                                                }


                                        } else {
                                            Toast.makeText(MapsActivity.this, "Site does not exist!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(MapsActivity.this, "Error fetching site details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "User does not exist!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error fetching user details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        registerOtherButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterForOthersActivity.class);
            intent.putExtra("siteId", siteId); // Pass siteId
            intent.putExtra("address", addressTextView.getText().toString()); // Pass address
            startActivity(intent);
            finish();
        });

        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    private void registerSelf(String siteId, String userId) {
        db.collection("donation_sites").document(siteId)
                .update("registeredDonors", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        dialog.dismiss();
    }

    private void cancelRegistration(String siteId, String userId) {
        db.collection("donation_sites").document(siteId)
                .update("registeredDonors", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Registration cancelled!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to cancel registration: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Retrieve latitude and longitude
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

//                        // Create LatLng and add marker
//                        LatLng currentLocation = new LatLng(latitude, longitude);

                        // Create LatLng and add marker
                        LatLng currentLocation = new LatLng(10.7291501, 106.6958129);

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Zoom level
                    } else {
                        Toast.makeText(MapsActivity.this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
                        Log.e("LocationError", "Location is null");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapsActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    Log.e("LocationError", "Error retrieving location", e);
                });
    }

    public void backToLastActivity(View view) {
        finish();
    }
}