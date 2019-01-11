package com.socketio.test.api;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class Client {

    private final static int CONNECTION_TIMEOUT = 10;
    private final static int DEFAULT_TIMEOUT = 30;
    private final static String HTTP_LOG_TAG = "HttpLog:";
    private final static HttpLoggingInterceptor HTTP_LOGGING_INTERCEPTOR =
            new HttpLoggingInterceptor(message -> Log.d(HTTP_LOG_TAG, message))
                    .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient sOkHttpClient;

    public static OkHttpClient get() {
        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(HTTP_LOGGING_INTERCEPTOR)
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        }
        return sOkHttpClient;
    }

}
