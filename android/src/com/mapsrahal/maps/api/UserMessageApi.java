package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.User;
import com.mapsrahal.maps.model.UserMessage;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserMessageApi {
    @POST("fcmmsg")
    Call<UserMessage> sentMessage(@Body UserMessage userMessage);

}
