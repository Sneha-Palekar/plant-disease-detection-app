package com.sneha.homepage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.IOException;
import java.util.List;

public class CamActive extends AppCompatActivity {

    private static final String TAG = "CamActive";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private ImageView imageView;
    private TextView resultText;
    private TextView demoText;
    private TextView clickHere;

    private YoloV8Detector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_active);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.result);
        demoText = findViewById(R.id.demoText);
//        clickHere = findViewById(R.id.click_here);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button cameraButton = findViewById(R.id.cameraButton);

        // Initialize detector
        detector = new YoloV8Detector(this);

        if (detector.isInitialized()) {
            demoText.setText("Ready to scan");
            Toast.makeText(this, "Model loaded! Detects Corn, Potato, Tomato, Grape", Toast.LENGTH_LONG).show();
        } else {
            demoText.setText("Model failed to load");
            Toast.makeText(this, "Model failed to load", Toast.LENGTH_LONG).show();
        }

        // Setup buttons
        uploadButton.setOnClickListener(v -> openGallery());
        cameraButton.setOnClickListener(v -> checkCameraPermission());

        if (clickHere != null) {
            clickHere.setVisibility(View.GONE);
        }
    }

    private void openGallery() {
        ImagePicker.with(this).galleryOnly().start(GALLERY_REQUEST_CODE);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void openCamera() {
        ImagePicker.with(this).cameraOnly().start(GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                runDetection(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Replace your runDetection method with this simplified version

    private void runDetection(Bitmap bitmap) {
        if (!detector.isInitialized()) {
            Toast.makeText(this, "Model not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        demoText.setText("Analyzing...");

        List<YoloV8Detector.Detection> detections = detector.detect(bitmap);

        if (detections != null && !detections.isEmpty()) {
            // Show the image
            imageView.setImageBitmap(bitmap);

            // Show top detection
            YoloV8Detector.Detection top = detections.get(0);
            String resultStr = top.getClassName() + " (" +
                    String.format("%.1f", top.getConfidence() * 100) + "%)";
            resultText.setText(resultStr);
            demoText.setText("Found: " + top.getClassName());

            if (clickHere != null) {
                clickHere.setVisibility(View.VISIBLE);
            }

            resultText.setOnClickListener(v -> showCureInfo(top.getClassName()));

            // Log all detections
            for (YoloV8Detector.Detection d : detections) {
                Log.d(TAG, "Detection: " + d.getClassName() + " - " + String.format("%.1f", d.getConfidence() * 100) + "%");
            }

        } else {
            resultText.setText("No disease detected");
            demoText.setText("Try another image");
            if (clickHere != null) {
                clickHere.setVisibility(View.GONE);
            }
            Log.d(TAG, "No detections found");
        }
    }

    private void showCureInfo(String diseaseName) {
        String cureInfo = getCureInfo(diseaseName);
        new android.app.AlertDialog.Builder(this)
                .setTitle("🌿 Treatment for " + diseaseName)
                .setMessage(cureInfo)
                .setPositiveButton("OK", null)
                .show();
    }

    // Update the getCureInfo method in CamActive.java with these treatments

    private String getCureInfo(String diseaseName) {
        String lower = diseaseName.toLowerCase();

        // ============ CORN DISEASES ============
        if (lower.contains("corn gray leaf spot")) {
            return "🌽 CORN GRAY LEAF SPOT\n\n" +
                    "Symptoms:\n• Small, rectangular lesions parallel to veins\n• Lesions turn gray to tan\n\n" +
                    "Treatment:\n✓ Use resistant hybrids\n✓ Apply fungicides (Azoxystrobin)\n✓ Crop rotation\n✓ Tillage to bury residue";
        }
        else if (lower.contains("corn leaf blight")) {
            return "🌽 CORN LEAF BLIGHT\n\n" +
                    "Symptoms:\n• Long, grayish-green lesions\n• Lesions turn brown with dark borders\n\n" +
                    "Treatment:\n✓ Use resistant varieties\n✓ Apply fungicides (Mancozeb)\n✓ Crop rotation\n✓ Remove infected debris";
        }
        else if (lower.contains("corn rust leaf")) {
            return "🌽 CORN RUST\n\n" +
                    "Symptoms:\n• Small, circular pustules on leaves\n• Pustules turn dark brown to black\n\n" +
                    "Treatment:\n✓ Apply fungicides (Azoxystrobin)\n✓ Plant resistant hybrids\n✓ Avoid late planting";
        }

        // ============ POTATO DISEASES ============
        else if (lower.contains("potato early blight")) {
            return "🥔 POTATO EARLY BLIGHT\n\n" +
                    "Symptoms:\n• Dark, concentric rings on leaves\n• Yellowing around lesions\n\n" +
                    "Treatment:\n✓ Remove infected leaves\n✓ Apply fungicides (Chlorothalonil)\n✓ Mulch around plants\n✓ Proper fertilization";
        }
        else if (lower.contains("potato late blight")) {
            return "🥔 POTATO LATE BLIGHT\n\n" +
                    "Symptoms:\n• Large, dark brown to black lesions\n• White fuzzy growth underside\n\n" +
                    "Treatment:\n✓ Remove infected plants immediately\n✓ Apply copper fungicides\n✓ Hill soil around plants\n✓ Destroy volunteer potatoes";
        }
        else if (lower.contains("potato leaf") && !lower.contains("early") && !lower.contains("late")) {
            return "🥔 POTATO LEAF\n\n" +
                    "Healthy potato leaf detected.\n\n" +
                    "Recommendation:\n✓ Continue regular monitoring\n✓ Maintain proper nutrition\n✓ Ensure adequate watering";
        }

        // ============ TOMATO DISEASES ============
        else if (lower.contains("tomato early blight")) {
            return "🍅 TOMATO EARLY BLIGHT\n\n" +
                    "Symptoms:\n• Dark spots with concentric rings\n• Yellowing around lesions\n\n" +
                    "Treatment:\n✓ Remove lower infected leaves\n✓ Apply fungicides (Chlorothalonil)\n✓ Mulch to prevent soil splash\n✓ Water at base of plant";
        }
        else if (lower.contains("tomato septoria leaf spot")) {
            return "🍅 TOMATO SEPTORIA LEAF SPOT\n\n" +
                    "Symptoms:\n• Small, circular spots with dark borders\n• Spots have gray centers\n\n" +
                    "Treatment:\n✓ Remove infected leaves\n✓ Apply fungicides\n✓ Avoid overhead watering\n✓ Improve air circulation";
        }
        else if (lower.contains("tomato leaf bacterial spot")) {
            return "🍅 TOMATO BACTERIAL SPOT\n\n" +
                    "Symptoms:\n• Dark, water-soaked spots on leaves\n• Spots may have yellow halos\n\n" +
                    "Treatment:\n✓ Remove infected plants\n✓ Apply copper-based bactericides\n✓ Avoid working in wet conditions\n✓ Rotate crops";
        }
        else if (lower.contains("tomato leaf late blight")) {
            return "🍅 TOMATO LATE BLIGHT\n\n" +
                    "Symptoms:\n• Large, dark brown lesions\n• White mold on undersides\n\n" +
                    "Treatment:\n✓ Remove infected plants immediately\n✓ Apply copper fungicides\n✓ Destroy infected material\n✓ Avoid overhead watering";
        }
        else if (lower.contains("tomato leaf mosaic virus")) {
            return "🍅 TOMATO MOSAIC VIRUS\n\n" +
                    "Symptoms:\n• Mottled light and dark green patches\n• Distorted leaves\n\n" +
                    "Treatment:\n✓ Remove infected plants\n✓ Control aphids\n✓ Use virus-free seeds\n✓ Disinfect tools";
        }
        else if (lower.contains("tomato leaf yellow virus")) {
            return "🍅 TOMATO YELLOW LEAF CURL VIRUS\n\n" +
                    "Symptoms:\n• Yellowing and curling of leaves\n• Stunted growth\n\n" +
                    "Treatment:\n✓ Remove infected plants\n✓ Control whiteflies\n✓ Use resistant varieties\n✓ Use reflective mulch";
        }
        else if (lower.contains("tomato mold leaf")) {
            return "🍅 TOMATO MOLD\n\n" +
                    "Symptoms:\n• Gray or white fuzzy growth on leaves\n• Leaf yellowing\n\n" +
                    "Treatment:\n✓ Improve air circulation\n✓ Apply fungicides\n✓ Remove infected leaves\n✓ Avoid overhead watering";
        }
        else if (lower.contains("tomato two spotted spider mites")) {
            return "🍅 SPIDER MITES\n\n" +
                    "Symptoms:\n• Tiny yellow or white spots on leaves\n• Fine webbing on leaves\n\n" +
                    "Treatment:\n✓ Use insecticidal soap or neem oil\n✓ Increase humidity\n✓ Remove affected leaves\n✓ Introduce predatory mites";
        }
        else if (lower.contains("tomato leaf") && !lower.contains("early") && !lower.contains("late") && !lower.contains("spot") && !lower.contains("bacterial") && !lower.contains("mosaic") && !lower.contains("yellow") && !lower.contains("mold") && !lower.contains("spider")) {
            return "🍅 TOMATO LEAF\n\n" +
                    "Healthy tomato leaf detected.\n\n" +
                    "Recommendation:\n✓ Continue regular monitoring\n✓ Maintain good cultural practices\n✓ Check for pests regularly";
        }

        // ============ GRAPE DISEASES ============
        else if (lower.contains("grape leaf black rot")) {
            return "🍇 GRAPE BLACK ROT\n\n" +
                    "Symptoms:\n• Brown spots on leaves\n• Black, mummified berries\n\n" +
                    "Treatment:\n✓ Remove mummified berries\n✓ Prune for air circulation\n✓ Apply fungicides (Myclobutanil)\n✓ Remove infected leaves";
        }
        else if (lower.contains("grape leaf") && !lower.contains("black rot")) {
            return "🍇 GRAPE LEAF\n\n" +
                    "Healthy grape leaf detected.\n\n" +
                    "Recommendation:\n✓ Regular inspection needed\n✓ Maintain good air circulation\n✓ Proper pruning";
        }

        // ============ DEFAULT ============
        else {
            return "🌿 " + diseaseName + "\n\n" +
                    "General Recommendations:\n\n" +
                    "• Consult your local agricultural extension office\n" +
                    "• Remove and destroy infected plant parts\n" +
                    "• Apply appropriate fungicides\n" +
                    "• Practice crop rotation\n" +
                    "• Water at the base of plants, avoiding leaves";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detector != null) {
            detector.close();
        }
    }
}