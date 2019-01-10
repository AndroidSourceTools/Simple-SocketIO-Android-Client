package com.socketio.test.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ResponseInfo {

    @SerializedName("status")
    private int mStatus;

    @SerializedName("message")
    private int mMessage;

    @SerializedName("payload")
    private HashMap<String, Object> mPayload;

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    public HashMap<String, Object> getPayload() {
        return mPayload;
    }

    public void setPayload(HashMap<String, Object> mPayload) {
        this.mPayload = mPayload;
    }

    public int getMessage() {
        return mMessage;
    }

    public void setMessage(int mMessage) {
        this.mMessage = mMessage;
    }

}
