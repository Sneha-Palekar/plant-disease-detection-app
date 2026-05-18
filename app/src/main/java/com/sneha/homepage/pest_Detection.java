package com.sneha.homepage;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class pest_Detection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pest_detection);

        Toast.makeText(this,
                "Pest Detection Feature\nComing Soon!\nFocusing on Disease Detection first.",
                Toast.LENGTH_LONG).show();

        // Automatically go back after 3 seconds
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }
}