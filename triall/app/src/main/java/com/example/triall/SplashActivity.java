package com.example.triall;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1 second (1000 milliseconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SplashActivity", "SplashActivity started");
        setContentView(R.layout.activity_splash);  // Make sure you're using the correct layout

        // Delay for 1 second
        new Handler().postDelayed(() -> {
            // Check if language is already selected
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String selectedLanguage = sharedPreferences.getString("selectedLanguage", null);
            applySavedLanguage(selectedLanguage);

            if (selectedLanguage != null) {
                // Language already selected, go to SecondActivity
                Intent intent = new Intent(SplashActivity.this, SecondActivity.class);
                startActivity(intent);
            } else {
                // No language selected, go to MainActivity to select a language
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
            finish(); // Close SplashActivity
        }, SPLASH_DELAY); // 1 second delay
    }

    // Method to apply saved language from SharedPreferences on app start
    private void applySavedLanguage(String selectedLanguage) {
        if (selectedLanguage == null) {
            Log.d("LanguageSelection", "No language selected; skipping language application.");
            return; // Skip if no language has been selected
        }
        Locale locale = new Locale(selectedLanguage);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale); // Set layout direction for RTL languages if needed
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}

