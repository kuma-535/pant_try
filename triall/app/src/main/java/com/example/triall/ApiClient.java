package com.example.triall;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            // Create a logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log the body of requests and responses

            // Create OkHttpClient and add the logging interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            retrofit = new Retrofit.Builder()
                    //.baseUrl("http://192.168.133.249:8000/") // Use your machine's IP
                    //.baseUrl("http://192.168.29.127:8000/") // Laptop's home IP address
                   .baseUrl("http://10.11.2.156:8000/")  // Use your office machine's IP
                    //.baseUrl("http://192.168.47.99:8000/")

                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
