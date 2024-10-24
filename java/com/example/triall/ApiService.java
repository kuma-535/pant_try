package com.example.triall;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.GET;
import java.util.List;
import retrofit2.http.Path;

public interface ApiService {

    @GET("/crops/")
    Call<List<Crop>> getCrops();

    @GET("varieties/{cropId}/")
    Call<List<Variety>> getVarieties(@Path("cropId") int cropId);

    @GET("/get_languages/")
    Call<List<Language>> getLanguages();




    // Define the API endpoint for uploading the image and metadata
    @Multipart
    @POST("/upload/")
    Call<UploadResponse> uploadImage(
            @Part("creation_date") RequestBody creationDate,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("selectedCropId") RequestBody selectedCropId,
            @Part("selectedVarietyId") RequestBody selectedVarietyId,
            @Part("selectedSowingDate") RequestBody selectedSowingDate,
            //@Field("selectedSowingDate") RequestBody selectedSowingDate,
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("/SowingDateResponse/") // Replace with your actual endpoint
    Call<SowingDateResponse> sendSowingDate(
            @Field("sowingDate") long sowingDate
    );
}
