package com.mapsrahal.maps.api;

import com.mapsrahal.maps.model.CallLog;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.Price;
import com.mapsrahal.maps.model.StatusUpdate;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PostApi {

    @POST("posts")
    Call<List<Post>> createPost(@Body Post post);

    @POST("price")
    Call<Price> createPrice(@Body Price price);

    @POST("status")
    Call<StatusUpdate> updateStatus(@Body StatusUpdate statusUpdate);

    @POST("calllog")
    Call<CallLog> callLog(@Body CallLog callLog);
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
