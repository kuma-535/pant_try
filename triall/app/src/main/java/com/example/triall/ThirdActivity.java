package com.example.triall;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ThirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        // Fetch the disease message from the Django server
        fetchDiseaseMessage();
    }

    private void fetchDiseaseMessage() {
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.11.2.156:8000/") // Django server base URL
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

                    // Display the message in a Toast
                    Toast.makeText(ThirdActivity.this, "Message: " + diseaseMessage, Toast.LENGTH_LONG).show();
                } else {
                    // In case of error, show a message
                    Toast.makeText(ThirdActivity.this, "Error: Unable to retrieve the message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DiseasePredictionResponse> call, Throwable t) {
                // Handle failure (e.g., no network connection)
                Toast.makeText(ThirdActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

