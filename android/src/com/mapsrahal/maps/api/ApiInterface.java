package com.mapsrahal.maps.api;

import com.mapsrahal.maps.auth.IsBlocked;
import com.mapsrahal.maps.auth.MessageResponse;
import com.mapsrahal.maps.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {
    /*@POST("requests")
    Call<User> sentOTP(@Body User user);

    @POST("validuser")
    Call<User> verifyOTP(@Body User user);

    @POST("verifyuser")
    Call<IsBlocked> verifyUser(@Body IsBlocked isBlocked);*/

    @POST("registerUser.php")
    Call<User> sentOTP(@Body User user);

    @POST("validUser.php")
    Call<User> verifyOTP(@Body User user);

    @POST("verifyUser.php")
    Call<IsBlocked> verifyUser(@Body IsBlocked isBlocked);
}
