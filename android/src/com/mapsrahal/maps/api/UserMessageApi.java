package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.maps.model.IsValid;
import com.mapsrahal.maps.model.UserMessage;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserMessageApi {

    /*@POST("fcmmsg")
    Call<UserMessage> sentMessage(@Body UserMessage userMessage);

    @POST("confirmlist")
    Call<UserMessage> sendConfirmation(@Body List<UserMessage> userMessage);

    @POST("startedlist")
    Call<UserMessage> sendTripStarted(@Body List<UserMessage> userMessage);

    @POST("completedlist")
    Call<UserMessage> sendTripCompleted(@Body List<UserMessage> userMessage);

    @POST("cancelledlist")
    Call<UserMessage> sendTripCancelled(@Body List<UserMessage> userMessage);*/
    /*@FormUrlEncoded
    @POST("confirmlist")
    Call<UserMessage> sendConfirmation(@FieldMap Map<String, String> data);*/

    @POST("sendMessage.php")
    Call<UserMessage> sentMessage(@Body UserMessage userMessage);

    @POST("sendConfirmation.php")
    Call<UserMessage> sendConfirmation(@Body List<UserMessage> userMessage);

    @POST("sendStarted.php")
    Call<UserMessage> sendTripStarted(@Body List<UserMessage> userMessage);

    @POST("sendCompleted.php")
    Call<UserMessage> sendTripCompleted(@Body List<UserMessage> userMessage);

    @POST("sendCancelled.php")
    Call<UserMessage> sendTripCancelled(@Body List<UserMessage> userMessage);

    @POST("sendPassengerCancelled.php")
    Call<IsValid> sendPassengerCancelled(@Body GetMyHistory getMyHistory);

    @POST("checkBeforeConfirm.php")
    Call<IsValid> checkBeforeConfirm(@Body List<UserMessage> userMessage);

    @POST("sendTaxiPassCancelled.php")
    Call<UserMessage> sendTaxiPassCancelled(UserMessage userMessage);

    @POST("sendTaxiCaptCancelled.php")
    Call<UserMessage> sendTaxiCaptCancelled();

}
