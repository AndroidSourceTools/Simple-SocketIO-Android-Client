package com.socketio.test.model;

public class BaseEvent<T> {
    private T mPayload;

    public void setPayload(T payload) {
        this.mPayload = payload;
    }

    public T getPayload() {
        return mPayload;
    }
}
