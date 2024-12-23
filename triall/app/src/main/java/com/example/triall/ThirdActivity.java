package com.example.triall;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ThirdActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        messageTextView = findViewById(R.id.messageTextView);

        // Set up the Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button (Up button) in the Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Fetch the disease message from the Django server
        fetchDiseaseMessage();
    }

    private void fetchDiseaseMessage() {
        // Show the ProgressBar while loading
        progressBar.setVisibility(View.VISIBLE);
        messageTextView.setVisibility(View.GONE);

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://10.11.2.156:8000/") // Django server base URL
                .baseUrl("http://14.139.229.39:8000/") // Django server base URL
                .addConverterFactory(GsonConverterFactory.create()) // Gson converter
                .build();

        // Create ApiService instance
        ApiService apiService = retrofit.create(ApiService.class);

        // Make the GET request to fetch the disease message
        Call<DiseasePredictionResponse> call = apiService.getDiseaseMessage();
        call.enqueue(new Callback<DiseasePredictionResponse>() {
            @Override
            public void onResponse(Call<DiseasePredictionResponse> call, Response<DiseasePredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Retrieve the generated message from the response body
                    String diseaseMessage = response.body().getMessage();
                    Log.d("ThirdActivity", "Disease Message: " + diseaseMessage);

                    // Hide the ProgressBar and show the message
                    progressBar.setVisibility(View.GONE);
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText(diseaseMessage);
                } else {
                    // In case of error, show a message
                    progressBar.setVisibility(View.GONE);
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText("Error: Unable to retrieve the message");
                    Log.e("ThirdActivity", "Error Response: " + response.errorBody()); // Log the error body
                }
            }

            @Override
            public void onFailure(Call<DiseasePredictionResponse> call, Throwable t) {
                // Handle failure (e.g., no network connection)
                progressBar.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText("API call failed");
                Log.e("ThirdActivity", "API call failed", t); // Log the throwable for debugging
            }
        });
    }
    // Use the new back press dispatcher method

    @Override
    public void onBackPressed() {
        // This will use the new OnBackPressedDispatcher for API 33+
        //getOnBackPressedDispatcher().onBackPressed();
        super.onBackPressed();
    }
    // Handle the back button click event
    @Override
    public boolean onSupportNavigateUp() {
        super.finish(); // Finish the activity to go back to SecondActivity
        return true;
    }
}

