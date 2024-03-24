package com.example.MakeITNatural;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class excessfood extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excess_food);

        // Assuming you have buttons in food_waste.xml
        Button btnmakeityourself = findViewById(R.id.btnMakeItYourself);
        Button btnbookagent = findViewById(R.id.btnBookAgent);

        // Add click listener for the "Book Agent" button
        btnbookagent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog.Builder and set the message and button
                AlertDialog.Builder builder = new AlertDialog.Builder(excessfood.this);
                builder.setMessage("Note:\n" +
                                "1) Food should be avilable more than 10 serves")
                        .setPositiveButton("Agree", (dialog, id) -> {
                            Intent intent = new Intent(excessfood.this, UploadPhotosActivity.class);
                            startActivity(intent);
                            dialog.dismiss(); // Dismiss the dialog
                        });

                // Create the AlertDialog object and show it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        btnmakeityourself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog.Builder and set the message and button
                AlertDialog.Builder builder = new AlertDialog.Builder(excessfood.this);
                builder.setMessage("Note:\n" +
                                "This guides to your Google maps,\n"+
                                "with nearby homes on it")
                        .setPositiveButton("Agree", (dialog, id) -> {
                            // Launch Google Maps with a search query for nearby orphanage homes
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q=near by orphanage");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(mapIntent);
                            } else {
                                // Handle the case where Google Maps is not installed
                                Toast.makeText(excessfood.this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
                            }
                        });

                // Create the AlertDialog object and show it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // Add similar click listeners for other buttons
    }

    // Override onOptionsItemSelected to handle back button click

    // You can reuse the showToast method if needed
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
