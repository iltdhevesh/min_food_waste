package com.example.MakeITNatural;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ConfirmBook extends BaseActivity {

    private String storedName;
    private String storedNumber;
    private String selectedFoodType;
    private String imageLink1;
    private String imageLink2;
    private String imageLink3;
    private String imageLink4;
    private double latitude;
    private double longitude;
    private ProgressDialog progressDialog;
    private ArrayList<Uri> selectedImages;
    private Link googleMapsLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_book);

        // Retrieve the storedName and storedNumber from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            storedName = intent.getStringExtra("name");
            storedNumber = intent.getStringExtra("number");
            selectedFoodType = intent.getStringExtra("foodType");
            imageLink1 = intent.getStringExtra("imageLink1");
            imageLink2 = intent.getStringExtra("imageLink2");
            imageLink3 = intent.getStringExtra("imageLink3");
            imageLink4 = intent.getStringExtra("imageLink4");


            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
            selectedImages = getIntent().getParcelableArrayListExtra("selectedImages");


            // Now you can use these variables to display the information as needed
            // For example, you can set them to TextViews in your layout
            TextView textViewName = findViewById(R.id.textViewName);
            TextView textViewNumber = findViewById(R.id.textViewNumber);
            TextView textViewFood = findViewById(R.id.textViewFood);
            TextView textViewLocation = findViewById(R.id.textViewLocation);
            String locationText = "http://maps.google.com/maps?q=" + latitude + "," + longitude;

            SpannableString spannableString = new SpannableString("Click here to check location");
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    // Handle click action here
                    String locationText = "http://maps.google.com/maps?q=" + latitude + "," + longitude;
                    Uri gmmIntentUri = Uri.parse(locationText);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Google Maps app not installed", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            spannableString.setSpan(clickableSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewLocation.setText(spannableString);
            textViewLocation.setMovementMethod(LinkMovementMethod.getInstance());

            textViewName.setText("Name: " + storedName);
            textViewNumber.setText("Number: " + storedNumber);
            textViewFood.setText("Type: " + selectedFoodType);
            LinearLayout imageContainer = findViewById(R.id.imageContainer);
            for (Uri imageUri : selectedImages) {
                ImageView imageView = new ImageView(this);
                imageView.setImageURI(imageUri);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.image_width),
                        getResources().getDimensionPixelSize(R.dimen.image_height)
                ));
                imageContainer.addView(imageView);
            }

            // Add the Confirm Book button and its OnClickListener
            Button btnConfirmBook = findViewById(R.id.btnConfirmBook);
            // Inside the onClick method of btnConfirmBook
            btnConfirmBook.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ProgressDialog progressDialog = new ProgressDialog(ConfirmBook.this);
                    progressDialog.setMessage("Creating Booking details...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    try {

                        String setIdentifier = String.valueOf(System.currentTimeMillis());
                        String fileName = "food_manage_" + setIdentifier + ".pdf";
                        File pdfFile = new File(getFilesDir(), fileName);
                        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfFile));
                        Document document = new Document(pdfDoc);
                        document.add(new Paragraph("Name: " + storedName));
                        document.add(new Paragraph("Number: " + storedNumber));
                        document.add(new Paragraph("Type: " + selectedFoodType));
                        document.add(new Paragraph("Location:").add(new Link("Click here for location", PdfAction.createURI(locationText)).setUnderline().setFontColor(ColorConstants.BLUE)));
                        document.add(new Paragraph("Click the following to view images:"));
                        document.add(new Paragraph("1. ").add(new Link("Image 1", PdfAction.createURI(imageLink1)).setUnderline().setFontColor(ColorConstants.BLUE)));
                        document.add(new Paragraph("2. ").add(new Link("Image 2", PdfAction.createURI(imageLink2)).setUnderline().setFontColor(ColorConstants.BLUE)));
                        document.add(new Paragraph("3. ").add(new Link("Image 3", PdfAction.createURI(imageLink3)).setUnderline().setFontColor(ColorConstants.BLUE)));
                        document.add(new Paragraph("4. ").add(new Link("Image 4", PdfAction.createURI(imageLink4)).setUnderline().setFontColor(ColorConstants.BLUE)));

                        document.close();

                        uploadPdfToFirebase(fileName);

                        // Open WhatsApp with a pre-filled message
                        openWhatsAppWithMessage("Booking details: " + getGeneratedPdfLink(fileName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void uploadPdfToFirebase(String fileName) {
        File pdfFile = new File(getFilesDir(), fileName);
        Uri fileUri = Uri.fromFile(pdfFile);

        // Define Firebase Storage references
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("food_pdf/"+ fileName);
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {


                    // Display a success Toast
                    runOnUiThread(() -> Toast.makeText(ConfirmBook.this, "Booking done successfully", Toast.LENGTH_SHORT).show());

                    // Additional logic...
                })
                .addOnFailureListener(e -> {

                    runOnUiThread(() -> Toast.makeText(ConfirmBook.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private String getGeneratedPdfLink(String fileName) {
        // Replace this with the actual logic to generate the PDF link
        return "https://firebasestorage.googleapis.com/v0/b/managefood2003.appspot.com/o/food_pdf%2F" + fileName+ "?alt=media&token=692bbefc-8833-4756-a03a-c001a58146b5";
    }

    private void openWhatsAppWithMessage(String message) {
        // Create an Intent to open WhatsApp
        String encodedMessage = Uri.encode(message);

        // Create the WhatsApp message URI
        Uri uri = Uri.parse("https://wa.me/918939970472?text=" + encodedMessage);

        // Create an Intent to open the WhatsApp application with the pre-composed message
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(ConfirmBook.this, message, Toast.LENGTH_SHORT).show();
    }
}
