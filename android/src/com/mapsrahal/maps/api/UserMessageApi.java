package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.UserMessage;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserMessageApi {
    @POST("fcmmsg")
    Call<UserMessage> sentMessage(@Body UserMessage userMessage);

    @POST("confirmlist")
    Call<UserMessage> sendConfirmation(@Body List<UserMessage> userMessage);

    @POST("startedlist")
    Call<UserMessage> sendTripStarted(@Body List<UserMessage> userMessage);

    @POST("completedlist")
    Call<UserMessage> sendTripCompleted(@Body List<UserMessage> userMessage);

    @POST("cancelledlist")
    Call<UserMessage> sendTripCancelled(@Body List<UserMessage> userMessage);
    /*@FormUrlEncoded
    @POST("confirmlist")
    Call<UserMessage> sendConfirmation(@FieldMap Map<String, String> data);*/

}
