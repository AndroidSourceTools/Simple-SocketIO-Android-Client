package com.socketio.test.api;

import com.socketio.test.model.ResponseInfo;
import com.socketio.test.model.UserInfo;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IApi {
    @GET("/api/v1/users/user_info_list")
    Observable<ResponseInfo> getUserInfoList();

    @FormUrlEncoded
    @POST("/api/v1/users/sign_up")
    Observable<ResponseInfo> signUpUser(@Field("user_name") String userName, @Field("user_pwd") String userPwd);

}
