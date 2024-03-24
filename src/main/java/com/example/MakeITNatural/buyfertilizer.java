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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class buyfertilizer extends BaseActivity {

    private Spinner optionsSpinner;
    private Button addToCartButton;
    private RelativeLayout quantityLayout;
    private Button decrementButton;
    private TextView quantityText;
    private Button incrementButton;
    private TableLayout productsTable;
    private Button checkoutButton;
    EditText editTextName;
    EditText editTextNumber;
    private TextView totalAmt; // Added TextView for total amount
    private TextView totalQty; // Added TextView for total quantity
    private ProgressDialog progressDialog;
    private Map<String, Integer> quantityMap = new HashMap<>();
    private String selectedOption;
    private int quantity = 1;
    private DataManager dataManager;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    // Update the timestamp format based on your preference
    String timestamp = String.valueOf(System.currentTimeMillis());
    String fileName = "bill_" + timestamp + ".txt";

    private StorageReference storageReference = storage.getReference().child("bill/"+fileName);
    // Change the file name if needed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_fertilizer);
        editTextName = findViewById(R.id.editTextName);
        editTextNumber = findViewById(R.id.editTextNumber);
        dataManager = new DataManager(this);
        ImageView fertilizerImage = findViewById(R.id.fertilizerImage);
        fertilizerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(buyfertilizer.this, ImageViewerActivity.class);

                intent.putExtra("IMAGE_RESOURCE", R.drawable.your_fertilizer_image);
                startActivity(intent);
            }
        });
        optionsSpinner = findViewById(R.id.optionsSpinner);
        addToCartButton = findViewById(R.id.addToCartButton);
        quantityLayout = findViewById(R.id.quantityLayout);
        decrementButton = findViewById(R.id.decrementButton);
        quantityText = findViewById(R.id.quantityText);
        incrementButton = findViewById(R.id.incrementButton);
        productsTable = findViewById(R.id.productsTable);
        checkoutButton = findViewById(R.id.checkoutButton);
        totalAmt = findViewById(R.id.totalAmt); // Initialize totalAmt TextView
        totalQty = findViewById(R.id.totalQty); // Initialize totalQty TextView
        String storedName = dataManager.getUserName();
        String storedNumber = dataManager.getUserPhoneNumber();
        editTextName.setText(storedName);
        editTextNumber.setText(storedNumber);


        // Set up the Spinner with options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.fertilizer_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        optionsSpinner.setAdapter(adapter);

        // Set click listener for the Spinner item selection
        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Save the selected quantity for the previous spinner item
                saveQuantityForOption(selectedOption, quantity);

                // Update the selected option and quantity for the new spinner item
                selectedOption = optionsSpinner.getSelectedItem().toString();
                quantity = getQuantityForOption(selectedOption);

                // Update the UI based on the current state
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });


        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the quantity layout and hide the "Add to Cart" button
                quantityLayout.setVisibility(View.VISIBLE);
                addToCartButton.setVisibility(View.GONE);

                // Set the initial quantity
                quantityText.setText(String.valueOf(quantity));

                // Update the quantity in the corresponding TextView in the TableLayout
                updateQuantityInTable(selectedOption, quantity);
                // Update the amount and total amount
                updateAmountForOption(selectedOption, quantity);

                // Call the new method to update the total quantity
                updateTotalQuantity();
            }
        });

        // Set click listeners for the quantity layout buttons
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementQuantity();
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementQuantity();
            }
        });

        // Set click listener for the Checkout button
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCheckout();
            }
        });
    }

    private void saveQuantityForOption(String option, int quantity) {
        // Save the selected quantity for the given option
        quantityMap.put(option, quantity);
    }

    private int getQuantityForOption(String option) {
        // Retrieve the previously saved quantity for the given option
        return quantityMap.containsKey(option) ? quantityMap.get(option) : 1;
    }

    // ...

    private void decrementQuantity() {
        if (quantity > 0) {
            if (quantity > 1) {
                quantity--;
                updateQuantityText();
                updateQuantityInTable(selectedOption, quantity);
                updateAmountForOption(selectedOption, quantity);
            } else {
                // If quantity is 1, set it to 0
                quantity = 0;
                updateQuantityText();
                updateQuantityInTable(selectedOption, quantity);

                // Show the "Add to Cart" button and hide the quantity layout
                showAddToCartButton();
            }
        }
        updateAmountForOption(selectedOption, quantity);
        // Update the total quantity on decrement
        updateTotalQuantity();
    }

    private void incrementQuantity() {
        quantity++;
        updateQuantityText();
        updateQuantityInTable(selectedOption, quantity);
        updateAmountForOption(selectedOption, quantity);

        // Update the total quantity on increment
        updateTotalQuantity();
    }


    private void updateQuantityText() {
        quantityText.setText(String.valueOf(quantity));
    }

    private void showAddToCartButton() {
        addToCartButton.setVisibility(View.VISIBLE);
        quantityLayout.setVisibility(View.GONE);
    }

    private void updateUI() {
        if (quantityMap.containsKey(selectedOption)) {
            // If the selected option has a saved quantity, update the UI to show that quantity
            quantity = quantityMap.get(selectedOption);
            quantityText.setText(String.valueOf(quantity));

            // Update the quantity in the corresponding TextView in the TableLayout
            updateQuantityInTable(selectedOption, quantity);
            updateAmountForOption(selectedOption, quantity);

            showAddToCartButton();
        } else {
            // If the selected option does not have a saved quantity, show the "Add to Cart" button
            addToCartButton.setVisibility(View.VISIBLE);
            quantityLayout.setVisibility(View.GONE);
        }
    }

    private void updateQuantityInTable(String option, int quantity) {
        // Update the quantity in the corresponding TextView in the TableLayout
        switch (option) {
            case "500g - Rs 200":
                ((TextView) findViewById(R.id.qty500g)).setText(String.valueOf(quantity));
                break;
            case "1Kg - Rs 350":
                ((TextView) findViewById(R.id.qty1Kg)).setText(String.valueOf(quantity));
                break;
            case "2Kg - Rs 650":
                ((TextView) findViewById(R.id.qty2Kg)).setText(String.valueOf(quantity));
                break;
            case "5Kg - Rs 1300":
                ((TextView) findViewById(R.id.qty5Kg)).setText(String.valueOf(quantity));
                break;
        }
    }

    // Add a method to get the price per unit for the given option
    private int getPricePerUnit(String option) {
        switch (option) {
            case "500g - Rs 200":
                return 200;
            case "1Kg - Rs 350":
                return 350;
            case "2Kg - Rs 650":
                return 650;
            case "5Kg - Rs 1300":
                return 1300;
            default:
                return 0;
        }
    }

    private void updateAmountForOption(String option, int quantity) {
        int pricePerUnit = getPricePerUnit(option);
        int amount = quantity > 0 ? quantity * pricePerUnit : 0; // Set amount to 0 when quantity is 0
        updateAmountInTable(option, amount);
        updateTotalAmount();
    }


    private void updateAmountInTable(String option, int amount) {
        switch (option) {
            case "500g - Rs 200":
                ((TextView) findViewById(R.id.amt500g)).setText(String.valueOf(amount));
                break;
            case "1Kg - Rs 350":
                ((TextView) findViewById(R.id.amt1Kg)).setText(String.valueOf(amount));
                break;
            case "2Kg - Rs 650":
                ((TextView) findViewById(R.id.amt2Kg)).setText(String.valueOf(amount));
                break;
            case "5Kg - Rs 1300":
                ((TextView) findViewById(R.id.amt5Kg)).setText(String.valueOf(amount));
                break;
        }
    }

    private void updateTotalAmount() {
        int totalAmount = calculateTotalAmount();
        totalAmt.setText(String.valueOf(totalAmount));
    }

    private int calculateTotalAmount() {
        int totalAmount = 0;
        totalAmount += Integer.parseInt(((TextView) findViewById(R.id.amt500g)).getText().toString());
        totalAmount += Integer.parseInt(((TextView) findViewById(R.id.amt1Kg)).getText().toString());
        totalAmount += Integer.parseInt(((TextView) findViewById(R.id.amt2Kg)).getText().toString());
        totalAmount += Integer.parseInt(((TextView) findViewById(R.id.amt5Kg)).getText().toString());
        return totalAmount;
    }

    // Add a method to update the total quantity
    private void updateTotalQuantity() {
        int totalQuantity = calculateTotalQuantity();
        totalQty.setText(String.valueOf(totalQuantity));
    }

    // Add a method to calculate the total quantity
    private int calculateTotalQuantity() {
        int totalQuantity = 0;
        totalQuantity += Integer.parseInt(((TextView) findViewById(R.id.qty500g)).getText().toString());
        totalQuantity += Integer.parseInt(((TextView) findViewById(R.id.qty1Kg)).getText().toString());
        totalQuantity += Integer.parseInt(((TextView) findViewById(R.id.qty2Kg)).getText().toString());
        totalQuantity += Integer.parseInt(((TextView) findViewById(R.id.qty5Kg)).getText().toString());
        return totalQuantity;
    }
    private void handleCheckout() {
        // Create a StringBuilder to construct the table data
        StringBuilder tableData = new StringBuilder();


        // Append header row
        tableData.append("Products\tQty\tAmt\n");

        // Append data rows
        tableData.append("500g - Rs 200\t")
                .append(((TextView) findViewById(R.id.qty500g)).getText().toString()).append("\t")
                .append(((TextView) findViewById(R.id.amt500g)).getText().toString()).append("\n");

        tableData.append("1Kg - Rs 350\t")
                .append(((TextView) findViewById(R.id.qty1Kg)).getText().toString()).append("\t")
                .append(((TextView) findViewById(R.id.amt1Kg)).getText().toString()).append("\n");

        tableData.append("2Kg - Rs 650\t")
                .append(((TextView) findViewById(R.id.qty2Kg)).getText().toString()).append("\t")
                .append(((TextView) findViewById(R.id.amt2Kg)).getText().toString()).append("\n");

        tableData.append("5Kg - Rs 1300\t")
                .append(((TextView) findViewById(R.id.qty5Kg)).getText().toString()).append("\t")
                .append(((TextView) findViewById(R.id.amt5Kg)).getText().toString()).append("\n");
        tableData.append("Total\t")
                .append(((TextView) findViewById(R.id.totalQty)).getText().toString()).append("\t")
                .append(((TextView) findViewById(R.id.totalAmt)).getText().toString()).append("\n");

        // Upload data to Firebase Storage
        uploadToFirebaseStorage(tableData.toString());
    }

    private void uploadToFirebaseStorage(String data) {
        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        UploadTask uploadTask = storageReference.putStream(stream);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait, your bill is being generated...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // File successfully uploaded
                    // Now, you can pass the StorageReference to the next page using Intent or any other suitable method
                    StorageReference fileRef = storage.getReference().child("bill/"+"bill_" + timestamp + ".txt");
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            String totalAmtValue = totalAmt.getText().toString();
                            Intent intent = new Intent(buyfertilizer.this, MapsActivity4.class);
                            intent.putExtra("FIREBASE_STORAGE_URL", downloadUrl);
                            intent.putExtra("TOTAL_AMOUNT", totalAmtValue);
                            startActivity(intent);
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            progressDialog.dismiss();
                            // You might want to show an error message to the user
                        }
                    });
                } else {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
