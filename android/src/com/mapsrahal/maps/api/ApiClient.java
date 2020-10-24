package com.mapsrahal.maps.api;

import com.mapsrahal.maps.Framework;
import com.mapsrahal.util.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    //private static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    // .client(okHttpClient)
    /*public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Framework.nativeGetBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }*/

    public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Framework.nativeGetBaseUrlLocal())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
