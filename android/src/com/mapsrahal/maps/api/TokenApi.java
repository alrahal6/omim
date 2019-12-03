package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.NewToken;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface TokenApi {
    @POST("fcm")
    Call<NewToken> sendToken(@Body NewToken newToken);

}
