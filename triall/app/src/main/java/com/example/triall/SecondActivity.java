package com.example.triall;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SecondActivity extends AppCompatActivity {

    private Spinner cropSpinner;
    private List<Crop> cropList = new ArrayList<>();
    private Spinner varietySpinner;
    private TextView tvDatePicker;

    private int selectedCropId = -1; // To store the selected crop ID
    private int selectedVarietyId= -1; // To store the selected variety ID

    private long selectedSowingDate;

    private ImageButton settingsButton;


    private List<Variety> varietyList = new ArrayList<>();
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;
    private ImageView imageView;
    private Retrofit retrofit;
    private ApiService apiService;
    private Uri selectedImageUri;
    private String creationDate;
    private String receivedLanguageCode;
    private double latitude;
    private double longitude;
    private File photoFile; // File to store captured image

    // Register the activity result launcher for capturing an image
    private final ActivityResultLauncher<Uri> captureImageLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    selectedImageUri = Uri.fromFile(photoFile);
                    displayImage(selectedImageUri); // Display the image in ImageView
                    extractImageMetadata(selectedImageUri); // Extract metadata from the captured image
                }
            }
    );

    // Register the activity result launcher for picking an image from the gallery
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    selectedImageUri = result;
                    Log.d("SelectedImageUri", "Uri: " + selectedImageUri);
                    displayImage(result);  // Display the image
                    extractImageMetadata(result);  // Extract metadata from the image
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the language code passed from MainActivity
        receivedLanguageCode = getIntent().getStringExtra("selectedLanguage");

        // Fallback to SharedPreferences if the language code is null
        if (receivedLanguageCode == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            receivedLanguageCode = sharedPreferences.getString("selectedLanguage", null);
        }

        // Log the received language code to check if it's correct
        Log.d("SecondActivity", "Received Language Code: " + receivedLanguageCode);
        setContentView(R.layout.activity_second);

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            // Open SettingsActivity when the button is clicked
            Intent intent = new Intent(SecondActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Initialize Retrofit

        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log the body of requests and responses

        // Create OkHttpClient and add the logging interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        retrofit = new Retrofit.Builder()
                //.baseUrl("http://192.168.29.127:8000/")  // Use your personal machine's IP
                //.baseUrl("http://10.11.2.153:8000/")  // Use your office machine's IP
                //.baseUrl("http://10.11.2.156:8000/")// Use MoM-CAT machine's IP
                .baseUrl("http://14.139.229.39:8000/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        Button btnUpload = findViewById(R.id.btnUpload);

        Button btnCaptureImage = findViewById(R.id.btnCaptureImage);  // New capture button
        imageView = findViewById(R.id.imageView);

        btnUpload.setOnClickListener(v -> {
            if (checkPermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        cropSpinner = findViewById(R.id.cropSpinner);
        varietySpinner = findViewById(R.id.varietySpinner); // Initialize varietySpinner

        tvDatePicker = findViewById(R.id.tvDatePicker);
        tvDatePicker.setOnClickListener(v -> showDatePickerDialog());




        btnCaptureImage.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                captureImage();
            } else {
                requestLocationPermission();
            }
        });

        fetchCrops(); // Fetch the crops when the activity starts
        varietySpinner.setEnabled(false); // Initially disable the variety spinner

        // Listener for crop spinner selection
        cropSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected Crop object
                Crop selectedCrop = (Crop) parent.getItemAtPosition(position);

                if (selectedCrop != null) {
                    // Fetch the varieties for the selected crop using its ID
                    fetchVarieties(selectedCrop.getId()); // Make sure to pass the crop ID
                } else {
                    // Show a Toast if no crop is selected
                    Toast.makeText(SecondActivity.this, "Please select a crop first", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        Button btnSendData = findViewById(R.id.btnSendData);
        btnSendData.setOnClickListener(v -> {
            if (selectedImageUri != null && creationDate != null) {
                if (latitude != 0.0 && longitude != 0.0) {
                    sendImageMetadata();  // Proceed with sending the data
                } else {
                    Toast.makeText(this, "Cannot send data. Location data is missing or invalid.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please select an image with metadata first.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchCrops() {
        String languageCode = receivedLanguageCode; // Get the language code sent from MainActivity

        // Pass the language code to the API call
        apiService.getCrops(languageCode).enqueue(new Callback<List<Crop>>() {
            @Override
            public void onResponse(Call<List<Crop>> call, Response<List<Crop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cropList = response.body();
                    cropList.add(0, new Crop("Select Your Crop")); // Add default item
                    ArrayAdapter<Crop> adapter = new ArrayAdapter<>(SecondActivity.this,
                            R.layout.custom_spinner_item, cropList);
                    adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                    cropSpinner.setAdapter(adapter);

                    cropSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Crop selectedCrop = (Crop) parent.getItemAtPosition(position);
                            if (position >= 0) { // Exclude the default item
                                selectedCropId = selectedCrop.getId(); // Store the selected crop ID
                                fetchVarieties(selectedCropId); // Call to fetch varieties based on selected crop
                            } else {
                                selectedCropId = -1; // Reset if no valid crop is selected
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });
                } else {
                    Toast.makeText(SecondActivity.this, "Failed to fetch crops", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Crop>> call, Throwable t) {
                Toast.makeText(SecondActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchVarieties(int cropId) {
        apiService.getVarieties(cropId, receivedLanguageCode).enqueue(new Callback<List<Variety>>() {
            @Override
            public void onResponse(Call<List<Variety>> call, Response<List<Variety>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    varietyList = response.body();

                    ArrayAdapter<Variety> adapter = new ArrayAdapter<>(SecondActivity.this,
                            R.layout.custom_spinner_item, varietyList);
                    adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                    varietySpinner.setAdapter(adapter);
                    varietySpinner.setEnabled(true); // Enable variety spinner

                    varietySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Log.e("Position", "Selection position"+position);
                            Variety selectedVariety = (Variety) parent.getItemAtPosition(position);
                            if (position >= 0) { // Exclude the default item
                                selectedVarietyId = selectedVariety.getId(); // Store the selected variety ID
                                Log.e("selectedVarietyId", "selectedVarietyId is "+selectedVariety.getId());
                            } else {
                                selectedVarietyId = -1; // Reset if no valid variety is selected
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Variety>> call, Throwable t) {
                Toast.makeText(SecondActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


        private void showDatePickerDialog() {
            // Get the current date
            final Calendar calendar = Calendar.getInstance();

            // Create a DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                        selectedSowingDate = selectedCalendar.getTimeInMillis(); // Store date as long (milliseconds)

                        Log.d("SowingDate", "Sowing Date in Millis: " + selectedSowingDate);
                        // Update the TextView with the formatted date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        tvDatePicker.setText(dateFormat.format(selectedCalendar.getTime()));
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            // Show the DatePickerDialog
            datePickerDialog.show();
        }


    // Method to capture image using the camera
    private void captureImage() {
        if (checkCameraPermission()) {  // Check camera permission first
            if (checkLocationPermission()) {  // Check location permission
                // Create the file to store the image
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoUri = FileProvider.getUriForFile(this, "com.example.triall.fileprovider", photoFile);
                    captureImageLauncher.launch(photoUri);  // Launch camera to capture image
                }
            } else {
                requestLocationPermission();  // Request location permission if not granted
            }
        } else {
            requestCameraPermission();  // Request camera permission if not granted
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();  // Retry capturing image after permission is granted
            } else {
                Toast.makeText(this, "Location permission is required to capture images with location data.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();  // Retry capturing image after camera permission is granted
            } else {
                Toast.makeText(this, "Camera permission is required to capture images.", Toast.LENGTH_LONG).show();
            }
        }
    }


    // Create a file to store the captured image
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(null);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e("CaptureImage", "Error creating image file", e);
            return null;
        }
    }

    // Check permission for accessing storage
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // check camera permission
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // request camera permission

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    // Check if location permission is granted
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permissions
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
    }
    // Open the gallery to select an image
    private void openGallery() {
        pickImageLauncher.launch("image/*");  // Ensure only images are shown
    }

    // Display the image in the ImageView
    private void displayImage(Uri imageUri) {
        imageView.setImageURI(imageUri);
    }

    // Extract creation date and GPS data from the image
    private void extractImageMetadata(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
                ExifSubIFDDirectory subIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

                // Check if the creation date is available
                if (subIFDDirectory != null) {
                    creationDate = subIFDDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    Log.d("ImageMetadata", "Creation Date: " + creationDate);
                } else {
                    Log.d("ImageMetadata", "Creation Date: Not available");
                }

                // Check if GPS data is available
                if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                    latitude = gpsDirectory.getGeoLocation().getLatitude();
                    longitude = gpsDirectory.getGeoLocation().getLongitude();
                    Log.d("ImageMetadata", "Latitude: " + latitude);
                    Log.d("ImageMetadata", "Longitude: " + longitude);

                    if (latitude == 0.0 && longitude == 0.0) {
                        Toast.makeText(this, "No GPS data available. Please use Browse option.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("ImageMetadata", "Location: Not available");
                    Toast.makeText(this, "Please ensure location was ON while taking picture.", Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "OR use Capture Image option.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("ImageMetadata", "Error extracting metadata", e);
        }
    }

    private void sendImageMetadata() {

        if (selectedSowingDate == 0||selectedCropId ==-1||selectedVarietyId==-1) {
            Log.e("SowingDateError", "Sowing date is empty before sending");
            Log.e("CropIdError", "CropId"+selectedCropId);
            Log.e("CropIdError", "CropId"+selectedVarietyId);
            Toast.makeText(SecondActivity.this, "Please select a sowing date", Toast.LENGTH_SHORT).show();
            return; // Stop sending if the date is not selected
        }

        // Create and show an AlertDialog with a spinning wheel
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sending data to server... Please wait.")
                .setCancelable(false);

        // Add a spinner (ProgressBar) inside the dialog
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        builder.setView(spinner);

        AlertDialog dialog = builder.create();
        dialog.show();



        // Log the values being sent
        Log.d("sendImageMetadata", "Creation Date: " + creationDate);
        Log.d("sendImageMetadata", "Latitude: " + latitude);
        Log.d("sendImageMetadata", "Longitude: " + longitude);
        Log.d(  // Send image and metadata to Django server
    "sendImageMetadata", "Selected Crop ID: " + selectedCropId);
        Log.d("sendImageMetadata", "Selected Variety ID: " + selectedVarietyId);
        Log.d("sendImageMetadata", "Selected Sowing Date: " + selectedSowingDate);
        Log.d("sendImageMetadata", "Image URI: " + selectedImageUri);


        RequestBody creationDateBody = RequestBody.create(creationDate, MultipartBody.FORM);
        RequestBody latitudeBody = RequestBody.create(String.valueOf(latitude), MultipartBody.FORM);
        RequestBody longitudeBody = RequestBody.create(String.valueOf(longitude), MultipartBody.FORM);
        RequestBody cropIdBody = RequestBody.create(String.valueOf(selectedCropId), MultipartBody.FORM); // Add crop ID
        RequestBody varietyIdBody = RequestBody.create(String.valueOf(selectedVarietyId), MultipartBody.FORM); // Add variety ID
        RequestBody sowingDateBody = RequestBody.create(String.valueOf(selectedSowingDate), MultipartBody.FORM); // Add sowing date
        RequestBody languageCodeBody = RequestBody.create(String.valueOf(receivedLanguageCode), MultipartBody.FORM);
        MultipartBody.Part imagePart = prepareFilePart("image", selectedImageUri);


        Call<UploadResponse> call = apiService.uploadImage(creationDateBody, latitudeBody, longitudeBody,cropIdBody,varietyIdBody,sowingDateBody,imagePart,languageCodeBody);

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                dialog.dismiss(); // Dismiss the dialog on response
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SecondActivity.this, "Data sent successfully!", Toast.LENGTH_SHORT).show();
                    navigateToThirdActivity(response.body().getMessage());
                } else {
                    Toast.makeText(SecondActivity.this, "Failed to send data", Toast.LENGTH_SHORT).show();
                }

            }



            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                dialog.dismiss(); // Dismiss the dialog on failure
                Toast.makeText(SecondActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadError", t.getMessage());
            }
        });
    }

    // Prepare file part for upload
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        String filePath = getFilePathFromUri(fileUri);
        if (filePath != null) {
            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } else {
            Toast.makeText(this, "Could not prepare file for upload.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Navigate to ThirdActivity
    private void navigateToThirdActivity(String serverMessage) {
        Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
        intent.putExtra("serverMessage", serverMessage); // Pass server message if needed
        startActivity(intent);
        finish(); // Close SecondActivity
    }

    // Get file path from URI
    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            if (contentUri != null) {
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }

    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
