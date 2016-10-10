package com.example.jellio.flowerpower;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;


    private TextView txtPlantHealthiness;
    private TextView txtAnalyzing;
    private ImageView prvwImage;
    private Button btnTakePicture;
    private Button btnUseDefault;
    private Button btnAnalyzePicture;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*** Handle user permissions ***/
        // Request camera permission if the user hasn't already granted it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }
        // Request write permission if the user hasn't already granted it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        /*** Initialize views ***/
        txtPlantHealthiness = (TextView) findViewById(R.id.txt_plant_healthiness);
        txtAnalyzing        = (TextView) findViewById(R.id.txt_analyzing);
        prvwImage = (ImageView) findViewById(R.id.prvwImage);
        btnTakePicture    = (Button) findViewById(R.id.btn_take_picture);
        btnUseDefault     = (Button) findViewById(R.id.btn_use_default);
        btnAnalyzePicture = (Button) findViewById(R.id.btn_analyze_picture);

        /*** Set Button onClickListeners ***/
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        btnUseDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rand = new Random();
                if (rand.nextInt(2) == 0) {
                    prvwImage.setImageResource(R.drawable.plant_healthy);
                } else {
                    prvwImage.setImageResource(R.drawable.plant_unhealthy);
                }
            }
        });
        btnAnalyzePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the bitmap from the image preview
                Bitmap bitmap = ((BitmapDrawable) prvwImage.getDrawable()).getBitmap();

                // (Optional) Scale down the image.
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 5, bitmap.getHeight() / 5, false);

                // Create a new instance of the Plant Health Analyzer
                PlantHealthAnalyzer pha = new PlantHealthAnalyzer(bitmap);
                pha.setImageDestination(prvwImage);
                pha.setTextViewDestination(txtPlantHealthiness);
                pha.setTextViewToggle(txtAnalyzing);

                // Run the background process.
                pha.execute();
                txtAnalyzing.setText(R.string.btn_analyze_running);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == Activity.RESULT_OK) {
                    // We have taken a picture, let's use it!
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    try {
                        Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
                        prvwImage.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        Log.e("Camera", e.toString());
                    }
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                } else {
                    // Permission denied.
                }
            }
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                } else {
                    // Permission denied.
                }
            }
        }
    }

}
