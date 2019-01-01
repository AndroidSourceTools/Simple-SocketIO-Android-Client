package com.socketio.test.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiInstManager {

    private final static String SERVER_URL = "http:xxxx:8082";

    private static Retrofit mRetrofit;

    public static IApi getApiInstance() {
        if(mRetrofit == null){
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .client(Client.get())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return mRetrofit.create(IApi.class);
    }

}
