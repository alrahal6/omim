package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.LatLngDestination;
import com.mapsrahal.maps.model.RequestMatch;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestApi {

    @POST("requests")
    Call<List<RequestMatch>> sendRequest(@Body RequestMatch requestMatch);

    @POST("destinationCoord.php")
    Call<LatLngDestination> setDestination(@Body LatLngDestination latLngDestination);

    @POST("removeDestination.php")
    Call<LatLngDestination> removeDestination(@Body LatLngDestination latLngDestination);
    
    /*
    @GET("posts")
    Call<List<Post>> getPosts(
            @Query("userId") Integer[] userId,
            @Query("_sort") String sort,
            @Query("_order") String order
    );

    @GET("posts")
    Call<List<Post>> getPosts(@QueryMap Map<String, String> parameters);

    @GET("posts/{id}/comments")
    Call<List<Comment>> getComments(@Path("id") int postId);

    @GET
    Call<List<Comment>> getComments(@Url String url);

    @POST("posts")
    Call<Post> createPost(@Body Post post);

    @FormUrlEncoded
    @POST("posts")
    Call<Post> createPost(
            @Field("userId") int userId,
            @Field("title") String title,
            @Field("body") String text
    );

    @FormUrlEncoded
    @POST("posts")
    Call<Post> createPost(@FieldMap Map<String, String> fields);
    */
}
