package com.example.triall;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Spinner languageSpinner;
    private Button selectButton;
    private Retrofit retrofit;
    private List<Language> languageList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private ApiService apiService;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "MainActivity started");
        setContentView(R.layout.activity_main);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttpClient and add the logging interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.11.2.156:8000/") // Use your server's IP
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        languageSpinner = findViewById(R.id.languageSpinner);
        selectButton = findViewById(R.id.selectLanguageButton);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Fetch languages from the server
        fetchLanguages();

        // Select button click listener
        selectButton.setOnClickListener(v -> {
            Language selectedLanguage = (Language) languageSpinner.getSelectedItem();
            if (selectedLanguage != null && !selectedLanguage.getCode().isEmpty()) {
                // Log the selected language name and code
                Log.d("LanguageSelection", "Selected Language: " + selectedLanguage.getName() + ", Code: " + selectedLanguage.getCode());
                // Save selected language code to SharedPreferences
                sharedPreferences.edit().putString("selectedLanguage", selectedLanguage.getCode()).apply();

                // Set the app locale based on the selected language
                setAppLocale(selectedLanguage.getCode());

                // Start the second activity with the selected language
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("selectedLanguage", selectedLanguage.getCode());
                startActivity(intent);
                finish(); // Optional: Prevent returning to MainActivity
            } else {
                Toast.makeText(MainActivity.this, "Please select a language", Toast.LENGTH_SHORT).show();
                // Log a message if no language is selected
                Log.d("LanguageSelection", "No language selected");
            }
        });
    }

    private void fetchLanguages() {
        apiService.getLanguages().enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, Response<List<Language>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    languageList = response.body();
                    languageList.add(0, new Language("Select Language", "")); // Add default item

                    // Set up the Spinner adapter
                    ArrayAdapter<Language> adapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_spinner_item, languageList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    languageSpinner.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch languages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to store the selected language and set the locale
    private void setAppLocale(String languageCode) {
        Log.d(TAG, "setAppLocale() called with: languageCode = [" + languageCode + "]");
        // Change the locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Reload the activity to apply the new locale
        recreate();
    }

}
