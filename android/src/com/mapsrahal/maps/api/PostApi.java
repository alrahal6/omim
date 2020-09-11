package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.CallLog;
import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.maps.model.MyAccount;
import com.mapsrahal.maps.model.MyTripHistory;
import com.mapsrahal.maps.model.NearbySearch;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.Price;
import com.mapsrahal.maps.model.StatusUpdate;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PostApi {

    @POST("posts.php")
    Call<List<Post>> createPost(@Body Post post);

    @POST("price.php")
    Call<Price> createPrice(@Body Price price);

    @POST("status.php")
    Call<StatusUpdate> updateStatus(@Body StatusUpdate statusUpdate);

    @POST("callLog.php")
    Call<CallLog> callLog(@Body CallLog callLog);

    @POST("getNearByPassLst.php")
    Call<List<NearbySearch>> nearbySearch(@Body NearbySearch nearbySearch);

    @POST("getMyHistory.php")
    Call<List<MyTripHistory>> myHistory(@Body GetMyHistory getMyHistory);

    @POST("getMyCurrent.php")
    Call<MyTripHistory> myCurrent(@Body GetMyHistory getMyHistory);

    @POST("getMyAccount.php")
    Call<MyAccount> myAccount(@Body GetMyHistory getMyHistory);
}
