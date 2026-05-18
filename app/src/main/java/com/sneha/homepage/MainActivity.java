package com.sneha.homepage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sneha.homepage.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Fixed: Using if-else instead of switch
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                // Using if-else to fix the constant expression error
                if (id == R.id.menu) {
                    startActivity(new Intent(getApplicationContext(), menupage.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.home) {
                    // Already on home page
                    return true;
                } else if (id == R.id.you) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });

        ImageView treeLogo = findViewById(R.id.treeLogo);
        TextView plant_Disease = findViewById(R.id.plant_Disease);
        TextView pest_Detection = findViewById(R.id.pest_Detection);
        TextView soilDetection = findViewById(R.id.soilDetection);

        treeLogo.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "I am your Friend", Toast.LENGTH_SHORT).show()
        );

        // Plant Disease Detection - WORKING
        plant_Disease.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, CamActive.class);
            startActivity(i);
        });

        // Pest Detection - TEMPORARILY DISABLED with Toast message
        pest_Detection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "🐛 Pest Detection\nFeature Coming Soon!\nWorking on Disease Detection first.",
                    Toast.LENGTH_LONG).show();
            // Commented out to prevent crashes:
            // Intent pest = new Intent(MainActivity.this, pest_Detection.class);
            // startActivity(pest);
        });

        // Soil Detection - Redirect to Disease Detection for now
        soilDetection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "🌱 Soil Detection\nFeature Coming Soon!\nUsing Disease Detection instead.",
                    Toast.LENGTH_LONG).show();
            Intent soil = new Intent(MainActivity.this, CamActive.class);
            startActivity(soil);
        });
    }
}