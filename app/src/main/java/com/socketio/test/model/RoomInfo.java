package com.socketio.test.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RoomInfo {

    @SerializedName("room_id")
    private String mRoomId;
    @SerializedName("room_type")
    private int mRoomType;
    @SerializedName("unread_count")
    private int mUnReadCount;
    @SerializedName("user_ids")
    private ArrayList<String> mUserIdList;
    @SerializedName("last_message")
    private String mLastMessage;
    @SerializedName("last_message_timestamp")
    private long mLastMessageTimestamp;

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public ArrayList<String> getUserIdList() {
        return mUserIdList;
    }

    public void setUserIdList(ArrayList<String> userIdList) {
        this.mUserIdList = userIdList;
    }

    public int getRoomType() {
        return mRoomType;
    }

    public void setRoomType(int mRoomType) {
        this.mRoomType = mRoomType;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String mLastMessage) {
        this.mLastMessage = mLastMessage;
    }

    public long getLastMessageTimestamp() {
        return mLastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long mLastMessageTime) {
        this.mLastMessageTimestamp = mLastMessageTime;
    }

    public int getUnReadCount() {
        return mUnReadCount;
    }

    public void setUnReadCount(int mUnReadCount) {
        this.mUnReadCount = mUnReadCount;
    }
}
