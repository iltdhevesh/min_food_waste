package com.example.MakeITNatural;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.MakeITNatural.databinding.ActivityMaps4Binding;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.util.List;

public class MapsActivity4 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMaps4Binding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String totalAmount = "";
    private PlacesClient placesClient;
    private LatLng lastKnownLocation;
    private boolean isPaymentMethodChosen = false;
    private DataManager dataManager;
    private String selectedPaymentMethod = "";
    private String paymentStatus = "";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = new DataManager(this);
        binding = ActivityMaps4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Button bookButton = findViewById(R.id.bookButton);
        Button payButton = findViewById(R.id.payButton);
        Intent intent = getIntent();
        totalAmount = intent.getStringExtra("TOTAL_AMOUNT");
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPaymentOptionsDialog();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        String storedName = dataManager.getUserName();
        String storedNumber = dataManager.getUserPhoneNumber();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait creating booking details....");
        progressDialog.setCancelable(false);
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPaymentMethodChosen) {

                    String firebaseStorageUrl = getIntent().getStringExtra("FIREBASE_STORAGE_URL");

                    double latitude = lastKnownLocation.latitude;
                    ;
                    double longitude = lastKnownLocation.longitude;
                    ;
                    String locationText = "https://maps.google.com/?q=" + latitude + "," + longitude;
                    String fileName = null;
                    try {

                        String setIdentifier = String.valueOf(System.currentTimeMillis());
                        fileName = "org_fertilizer" + setIdentifier + ".pdf";
                        File pdfFile = new File(getFilesDir(), fileName);
                        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfFile));
                        Document document = new Document(pdfDoc);
                        document.add(new Paragraph("Name: " + storedName));
                        document.add(new Paragraph("Number: " + storedNumber));
                        document.add(new Paragraph("Location:").add(new Link("Click here for location", PdfAction.createURI(locationText)).setUnderline().setFontColor(ColorConstants.BLUE)));

                        document.add(new Paragraph("Bill: ").add(new Link("Click here for the Bill", PdfAction.createURI(firebaseStorageUrl)).setUnderline().setFontColor(ColorConstants.BLUE)));

                        document.close();
                        Uri fileUri = Uri.fromFile(pdfFile);

                        // Define Firebase Storage references
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference fileRef = storageRef.child("food_pdf/" + fileName);

                        // Display a progress dialog


                        fileRef.putFile(fileUri)
                                .addOnSuccessListener(taskSnapshot -> {


                                    // Display a success Toast
                                    runOnUiThread(() -> Toast.makeText(MapsActivity4.this, "Booking done successfully", Toast.LENGTH_SHORT).show());

                                    // Additional logic...
                                })
                                .addOnFailureListener(e -> {

                                    runOnUiThread(() -> Toast.makeText(MapsActivity4.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String message = "Booking details:" + "https://firebasestorage.googleapis.com/v0/b/managefood2003.appspot.com/o/food_pdf%2F" +
                            fileName + "?alt=media&token=692bbefc-8833-4756-a03a-c001a58146b5" +"\n"+
                            paymentStatus;

                    String encodedMessage = Uri.encode(message);
                    progressDialog.dismiss();
                    // Create the WhatsApp message URI
                    Uri uri = Uri.parse("https://wa.me/918939970472?text=" + encodedMessage);

                    // Create an Intent to open the WhatsApp application with the pre-composed message
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                    finish();
                } else {
                    // Handle the case when payment method is not chosen
                    progressDialog.dismiss();
                    Toast.makeText(MapsActivity4.this, "Choose a payment method first", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // Initialize the Places API client
        Places.initialize(getApplicationContext(), "AIzaSyDstfeKPv45OX0DZjJnzMFJBVjqtkklPVw"); // Replace with your actual API key
        placesClient = Places.createClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    private void showPaymentOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Payment Method")
                .setItems(R.array.payment_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Cash on Delivery
                                selectedPaymentMethod = "Cash on Delivery";
                                paymentStatus = "payment: Cash on Delivery";
                                isPaymentMethodChosen = true;
                                break;
                            case 1: // UPI Payment
                                selectedPaymentMethod = "UPI Payment";
                                paymentStatus = "payment: Ask for proof";
                                isPaymentMethodChosen = true;
                                String upiId = "iltdhevesh@okicici";

                                // Replace "Test Transaction" with the description or note for the transaction
                                String transactionNote = "fertilizer";

                                // Replace "1" with the actual amount you want to send
                                String amount = totalAmount;

                                // Create a UPI payment URI
                                Uri uri = Uri.parse("upi://pay?pa=" + upiId + "&pn=TestMerchant&mc=123456&tid=123456" +
                                        "&tr=12345678&tn=" + transactionNote + "&am=" + amount + "&cu=INR");

                                // Create an Intent to launch Google Pay
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.setPackage("com.google.android.apps.nbu.paisa.user");

                                startActivity(intent);
                                break;
                        }

                        // Enable the "Book Now" button after the payment method is chosen
                        Button bookButton = findViewById(R.id.bookButton);
                        bookButton.setEnabled(isPaymentMethodChosen);
                    }
                });

        builder.create().show();
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
