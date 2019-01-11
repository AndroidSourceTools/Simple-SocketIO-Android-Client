package com.socketio.test.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserInfo implements Parcelable {
    // Maybe it is user token
    @SerializedName("user_id")
    private String mUserId;
    @SerializedName("user_name")
    private String mUserName;
    @SerializedName("room_ids")
    private List<String> mRoomIdList = new ArrayList<>();

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

    public List<String> getRoomIdList() {
        return mRoomIdList;
    }

    public void setRoomIdList(List<String> mRoomIdList) {
        this.mRoomIdList = mRoomIdList;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof UserInfo)) {
            return false;
        }

        UserInfo otherUserInfo = (UserInfo) obj;

        return mUserId.equals(otherUserInfo.mUserId);
    }

    public UserInfo() {

    }

    public UserInfo(Parcel in) {
        mUserId = in.readString();
        mUserName = in.readString();
        in.readStringList(mRoomIdList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mUserName);
        dest.writeStringList(mRoomIdList);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
