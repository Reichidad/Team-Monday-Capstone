package com.example.newtattooandroid.network;

import com.example.newtattooandroid.model.MainItem;
import com.example.newtattooandroid.model.TattooistDto;
import com.example.newtattooandroid.model.TattooReviewItem;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.ArrayList;
import java.util.List;

public interface NetworkAPIs {
    @Multipart
    @POST("/upload")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file, @Part("name") RequestBody requestBody);

    @Multipart
    @POST("/test")
    Call<List<MainItem>> searchImage(@Part MultipartBody.Part file);

    @GET("/allpost")
    Call<List<MainItem>> getAllPost();

    @GET("/tattooistposts")
    Call<List<MainItem>> getTattooistPost(@Query("tattooistId") String tattooistId);

    @GET("/review")
    Call<List<TattooReviewItem>> getAllReviews(@Query("postId") int id);

    // TODO
    @GET("/user/review")
    Call<List<TattooReviewItem>> getUserReviews(@Query("userId") String userId);

    @GET("/tattooist")
    Call<TattooistDto> getTattoist(@Query("userId") String userId);

    @POST("/signup/tattooist")
    Call<TattooistDto> applyTattooist(@Body TattooistDto tattooistDto);

    @Multipart
    @POST("/write/post")
    Call<MainItem> writePost(@Part("post") RequestBody post, @Part ArrayList<MultipartBody.Part> tattoos, @Part MultipartBody.Part design);
}
