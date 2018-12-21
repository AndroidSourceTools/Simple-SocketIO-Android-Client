package com.socketio.test.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UserInfo {
    // Maybe it is user token
    @SerializedName("user_id")
    private String mUserId;
    @SerializedName("user_name")
    private String mUserName;

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof UserInfo)) {
            return false;
        }

        UserInfo otherUserInfo = (UserInfo) obj;

        return mUserId.equals(otherUserInfo.mUserId);
    }
}
