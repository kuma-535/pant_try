package com.example.triall;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1 second (1000 milliseconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedLanguage();
        setContentView(R.layout.activity_splash);  // Make sure you're using the correct layout

        // Delay for 1 second
        new Handler().postDelayed(() -> {
            // Check if language is already selected
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String selectedLanguage = sharedPreferences.getString("selectedLanguage", null);

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
    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String languageCode = prefs.getString("app_language", "en"); // Default to English if not set
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale); // Set layout direction for RTL languages if needed
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        // Reload the activity to apply the new locale
        //recreate();


    }
}

