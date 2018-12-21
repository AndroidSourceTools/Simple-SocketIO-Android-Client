package com.socketio.test.model;

import com.google.gson.annotations.SerializedName;

public class MessageInfo {

    @SerializedName("user_id")
    private String mUserId;
    @SerializedName("room_id")
    private String mRoomId;
    @SerializedName("message_type")
    private int mMessageType;
    @SerializedName("event_response_type")
    private int mEventResponseType;
    @SerializedName("message_time")
    private long mMessageTime;
    @SerializedName("message")
    private String mMessage;

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public long getMessageTime() {
        return mMessageTime;
    }

    public void setMessageTime(long mMessageTime) {
        this.mMessageTime = mMessageTime;
    }

    public int getMessageType() {
        return mMessageType;
    }

    public void setMessageType(int mMessageType) {
        this.mMessageType = mMessageType;
    }

    public int getEventResponseType() {
        return mEventResponseType;
    }

    public void setEventResponseType(int mEventType) {
        this.mEventResponseType = mEventType;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public MessageInfo clone()  {
        MessageInfo dupMsgInfo = new MessageInfo();
        dupMsgInfo.mUserId = mUserId;
        dupMsgInfo.mRoomId = mRoomId;
        dupMsgInfo.mMessageType = mMessageType;
        dupMsgInfo.mEventResponseType = mEventResponseType;
        dupMsgInfo.mMessageTime = mMessageTime;
        dupMsgInfo.mMessage = mMessage;

        return dupMsgInfo;
    }
}
