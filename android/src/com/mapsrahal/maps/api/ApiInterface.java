package com.mapsrahal.maps.api;

import com.mapsrahal.maps.auth.IsBlocked;
import com.mapsrahal.maps.auth.MessageResponse;
import com.mapsrahal.maps.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {
    @POST("requests")
    Call<User> sentOTP(@Body User user);

    @GET("verify")
    Call<MessageResponse> verifyOTP(@Body String otp_entered_by_user);

    @POST("verifyuser")
    Call<IsBlocked> verifyUser(@Body IsBlocked isBlocked);
}
