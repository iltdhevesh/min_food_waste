package com.example.MakeITNatural;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity2 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Button btnFoodWaste = findViewById(R.id.btnFoodWaste);
        Button btnExcessFood = findViewById(R.id.btnExcessFood);
        Button btnBuyFertilizer = findViewById(R.id.btnBuyFertilizer);

        // Example: Handling click on btnFoodWaste
        btnFoodWaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add logic for handling Food Waste button click
                showToast("Food Waste button clicked!");
                Intent intent = new Intent(MainActivity2.this, foodwaste.class);
                startActivity(intent);
            }
        });

        btnExcessFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add logic for handling Food Waste button click
                showToast("Excess Food button clicked!");
                Intent intent = new Intent(MainActivity2.this, excessfood.class);
                startActivity(intent);
            }
        });
        btnBuyFertilizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add logic for handling Food Waste button click
                showToast("Buy Fertilizer button clicked!");
                Intent intent = new Intent(MainActivity2.this, buyfertilizer.class);
                startActivity(intent);
            }
        });
        // Add similar click listeners for other buttons
    }

    // You can reuse the showToast method if needed
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        // Override onBackPressed to close the app when in MainActivity2
        finishAffinity();
    }
}
