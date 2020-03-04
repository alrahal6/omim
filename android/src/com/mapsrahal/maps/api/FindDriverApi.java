package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.FindDriver;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FindDriverApi {
    /*@POST("finddriver")
    Call<List<FindDriver>> findDriver(@Body FindDriver findDriver);*/

    @POST("getNearByDrivers.php")
    Call<List<FindDriver>> findDriver(@Body FindDriver findDriver);
}
