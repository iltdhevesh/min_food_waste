package com.example.MakeITNatural;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.MakeITNatural.databinding.ActivityMaps2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMaps2Binding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private PlacesClient placesClient;
    private LatLng lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaps2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button bookButton = findViewById(R.id.bookButton);

        // Set up the "Book" button click listener
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if lastKnownLocation is available
                if (lastKnownLocation != null) {
                    // Get the location coordinates
                    double latitude = lastKnownLocation.latitude;
                    double longitude = lastKnownLocation.longitude;

                    // Retrieve information from the previous activity
                    String storedName = getIntent().getStringExtra("name");
                    String storedNumber = getIntent().getStringExtra("number");
                    String selectedFoodType = getIntent().getStringExtra("foodType");
                    String imageLink1 = getIntent().getStringExtra("imageLink1");
                    String imageLink2 = getIntent().getStringExtra("imageLink2");
                    String imageLink3 = getIntent().getStringExtra("imageLink3");
                    String imageLink4 = getIntent().getStringExtra("imageLink4");

                    ArrayList<Uri> selectedImages = getIntent().getParcelableArrayListExtra("selectedImages");

                    // Create an intent to start ConfirmBook activity
                    Intent confirmBookIntent = new Intent(MapsActivity2.this, ConfirmBook.class);
                    confirmBookIntent.putExtra("latitude", latitude);
                    confirmBookIntent.putExtra("longitude", longitude);
                    confirmBookIntent.putExtra("name", storedName);
                    confirmBookIntent.putExtra("number", storedNumber);
                    confirmBookIntent.putExtra("foodType", selectedFoodType);
                    confirmBookIntent.putExtra("imageLink1", imageLink1);
                    confirmBookIntent.putExtra("imageLink2", imageLink2);
                    confirmBookIntent.putExtra("imageLink3", imageLink3);
                    confirmBookIntent.putExtra("imageLink4", imageLink4);

                    confirmBookIntent.putParcelableArrayListExtra("selectedImages", selectedImages);

                    // Start ConfirmBook activity
                    startActivity(confirmBookIntent);
                } else {
                    // Handle the case when lastKnownLocation is not available
                    Toast.makeText(MapsActivity2.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize the Places API client
        Places.initialize(getApplicationContext(), "AIzaSyDstfeKPv45OX0DZjJnzMFJBVjqtkklPVw"); // Replace with your actual API key
        placesClient = Places.createClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void fetchPlaceSuggestions(String query) {
        // Define the autocomplete request
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        // Fetch and display place suggestions
        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((response) -> {
                    // Handle successful response and update the suggestions
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    // Update your UI with the list of place predictions
                })
                .addOnFailureListener((exception) -> {
                    // Handle error
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        // Enable the "My Location" layer on the map
        mMap.setMyLocationEnabled(true);

        // Initialize location callback to update the camera when the location changes
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    lastKnownLocation = new LatLng(
                            locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()
                    );

                    // Add a marker at the current location and move the camera
                    mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("My Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation));

                    // Stop location updates after the first update
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        // Request location updates
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Handle the result of the location permission request
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, call onMapReady again to initialize the map
                onMapReady(mMap);
            }
        }
    }
}
