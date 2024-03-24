package com.example.MakeITNatural;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;


public class ImageViewerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = findViewById(R.id.imageView);

        // Retrieve the image resource or URI from the intent
        int imageResource = getIntent().getIntExtra("IMAGE_RESOURCE", 0);

        // Set the image resource to the ImageView
        imageView.setImageResource(imageResource);
    }
}