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

import com.example.MakeITNatural.R;

public class foodwaste extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_waste);

        // Assuming you have buttons in food_waste.xml
        Button btnmakeityourself = findViewById(R.id.btnMakeItYourself);
        Button btnbookagent = findViewById(R.id.btnBookAgent);

        // Add click listener for the "Book Agent" button
        btnbookagent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog.Builder and set the message and button
                AlertDialog.Builder builder = new AlertDialog.Builder(foodwaste.this);
                builder.setMessage("Note:\n" +
                                "1) No Non-veg waste(egg shells are acceptable)\n" +
                                "2)No Spoiled food\n" +
                                "3)Kindly give it to us before it gets Spoiled.")
                        .setPositiveButton("Agree", (dialog, id) -> {
                            Intent intent = new Intent(foodwaste.this, UploadPhotosActivity.class);
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
                String phoneNumber = "918939970472";
                String message = "I have some food waste, connect me to an associate";

                Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

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
