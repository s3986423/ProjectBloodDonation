package com.example.projectblooddonation;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.projectblooddonation.databinding.ActivityMapsBinding;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST_CODE = 1001;
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch donation sites from Firestore
        db.collection("donation_sites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String address = doc.getString("address");
                        GeoPoint location = doc.getGeoPoint("location");

                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .snippet(address));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MapsActivity.this, "Failed to fetch sites: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        getCurrentLocation();

    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Retrieve latitude and longitude
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Create LatLng and add marker
                        LatLng currentLocation = new LatLng(latitude, longitude);
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
}