package com.socketio.test.model;

public class MessageReceiveEvent<T> extends BaseEvent<T> {
    public MessageReceiveEvent(T payload) {
        setPayload(payload);
    }
}
