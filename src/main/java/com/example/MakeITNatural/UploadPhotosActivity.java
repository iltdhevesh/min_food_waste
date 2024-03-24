package com.example.MakeITNatural;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class UploadPhotosActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final int PICK_IMAGES_REQUEST_CODE = 456;
    private static final int MAX_SELECTED_IMAGES = 4;
    private ProgressDialog progressDialog;
    StorageReference storageReference;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private EditText editTextName;
    private EditText editTextNumber;
    private String storedName;
    private String storedNumber;
    private DataManager dataManager;
    private Spinner spinnerFoodType;
    private ArrayAdapter<CharSequence> foodTypeAdapter;
    private String selectedFoodType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photos);

        dataManager = new DataManager(this);
        editTextName = findViewById(R.id.editTextName);
        editTextNumber = findViewById(R.id.editTextNumber);
        spinnerFoodType = findViewById(R.id.spinnerFoodType);
        foodTypeAdapter = ArrayAdapter.createFromResource(this, R.array.food_types, android.R.layout.simple_spinner_item);
        foodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFoodType.setAdapter(foodTypeAdapter);
        storedName = dataManager.getUserName();
        storedNumber = dataManager.getUserPhoneNumber();
        editTextName.setText(storedName);
        editTextNumber.setText(storedNumber);
        spinnerFoodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedFoodType = (String) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing here
            }
        });

        Button btnLocation = findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the map view and pass data
                openMapView();
            }
        });

        Button btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open gallery or file picker
                openImagePicker();
            }
        });
    }

    private void openMapView() {
        // Check if exactly four images are selected
        if (selectedImageUris.size() != MAX_SELECTED_IMAGES) {
            Toast.makeText(this, "Please select exactly 4 images before choosing the location", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait, images are uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().getReference().child("images");

        List<String> imageUrls = new ArrayList<>();

        // Upload each selected image to Firebase Storage
        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            String imageName = "image_" + System.currentTimeMillis(); // Unique name for each image

            // Create a reference to the image location in Firebase Storage
            StorageReference imageRef = storageReference.child(imageName);

            // Upload the image
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            imageUrls.add(imageUrl);

                            // Check if all four images are uploaded
                            if (imageUrls.size() == MAX_SELECTED_IMAGES) {
                                // All images are uploaded, pass the image URLs to MapsActivity2
                                passDataToMapActivity(imageUrls);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors during the upload
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void passDataToMapActivity(List<String> imageUrls) {
        if (imageUrls.size() != MAX_SELECTED_IMAGES) {
            // Handle the case where not all images are uploaded
            Toast.makeText(this, "Please wait for all images to upload", Toast.LENGTH_SHORT).show();
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Intent mapIntent = new Intent(this, MapsActivity2.class);
        mapIntent.putExtra("name", storedName);
        mapIntent.putExtra("number", storedNumber);
        mapIntent.putExtra("foodType", selectedFoodType);

        // Attach the image URLs to the intent
        mapIntent.putExtra("imageLink1", imageUrls.get(0));
        mapIntent.putExtra("imageLink2", imageUrls.get(1));
        mapIntent.putExtra("imageLink3", imageUrls.get(2));
        mapIntent.putExtra("imageLink4", imageUrls.get(3));

        mapIntent.putParcelableArrayListExtra("selectedImages", new ArrayList<>(selectedImageUris));

        startActivity(mapIntent);
    }




    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Handle selected images
            int selectedImageCount = 0;

            if (data.getClipData() != null) {
                // Multiple images are selected
                selectedImageCount = data.getClipData().getItemCount();
            } else if (data.getData() != null) {
                // Single image is selected
                selectedImageCount = 1;
            }

            if (selectedImageCount <= MAX_SELECTED_IMAGES) {
                // Process the selected images
                selectedImageUris.clear(); // Clear previous selections

                if (data.getClipData() != null) {
                    // Multiple images are selected
                    for (int i = 0; i < selectedImageCount; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImageUris.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    // Single image is selected
                    Uri imageUri = data.getData();
                    selectedImageUris.add(imageUri);
                }

                // TODO: Add your logic for handling selected images
            } else {
                // Notify the user that they can't select more than 5 photos
                Toast.makeText(this, "You can select up to 4 photos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}