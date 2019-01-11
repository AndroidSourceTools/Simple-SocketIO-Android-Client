package com.socketio.test.utils;

import android.graphics.Path;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.socketio.test.model.MessageInfo;
import com.socketio.test.model.MessageReceiveEvent;
import com.socketio.test.model.RoomInfo;
import com.socketio.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;

public class SocketIOManager {

    private static SocketIOManager sSocketManager = null;

    private Socket mSocket;
    private Gson mGon;

    public static class Options {
        private boolean isForceNew = false;
        private boolean reconnection = false;
        private String query = "";
        private String host = "";

        public Options isForceNew(boolean isForceNew) {
            this.isForceNew = isForceNew;
            return this;
        }

        public Options reconnection(boolean reconnection) {
            this.reconnection = reconnection;
            return this;
        }

        public Options query(String query) {
            this.query = query;
            return this;
        }

        public Options host(String host) {
            this.host = host;
            return this;
        }
    }

    private SocketIOManager() {
        mGon = new Gson();
    }

    public void init(Options options) {
        initConfigs(options);
        initEvent();
    }

    private void initConfigs(Options ops) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;
            TrustManager[] trustAllCerts = new TrustManager[]{trustManager};
            sslContext.init(null, trustAllCerts, null);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(hostnameVerifier)
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .build();
            // default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            IO.Options options = new IO.Options();
            options.forceNew = ops.isForceNew;
            options.reconnection = ops.reconnection;
            options.query = ops.query;
            //options.query = "auth_token=" + System.currentTimeMillis();
            options.callFactory = okHttpClient;
            options.webSocketFactory = okHttpClient;
            // only use websocket
            //options.upgrade = false;
            //options.transports = new String[]{"websocket"};

            mSocket = IO.socket(ops.host, options);
            //mSocket = IO.socket("https://10.24.100.101:3000", options);
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static SocketIOManager getInstance() {
        if (sSocketManager == null) {
            sSocketManager = new SocketIOManager();
        }
        return sSocketManager;
    }

    private void initEvent() {
        mSocket.on("connect", (Object... args) -> {
            MessageInfo msgInfo = new MessageInfo();
            msgInfo.setMessageType(-1);
            msgInfo.setEventResponseType(0);

            MessageReceiveEvent<MessageInfo> msgRecvEvent = new MessageReceiveEvent<>(msgInfo);
            EventBus.getDefault().post(msgRecvEvent);
        }).on("create-room-success", (Object... args) -> {
            if (!isReceivedArgsValid(args)) {
                return;
            }

            JSONObject jsonObj = (JSONObject) args[0];
            RoomInfo roomInfo = new RoomInfo();
            ArrayList<String> userIds = new ArrayList<>();

            roomInfo.setRoomId(jsonObj.optString("room_id"));
            roomInfo.setRoomType(jsonObj.optInt("room_type"));
            roomInfo.setLastMessage(jsonObj.optString("last_message"));
            roomInfo.setUnReadCount(jsonObj.optInt("unread_count"));
            roomInfo.setLastMessageTimestamp(jsonObj.optLong("last_message_timestamp"));
            JSONArray userIdsJsonAry = null;

            try {
                userIdsJsonAry = new JSONArray(jsonObj.optString("user_ids"));
            } catch (JSONException e) {
                e.printStackTrace();
                userIdsJsonAry = new JSONArray();
            } finally {
                for (int i = 0, len = userIdsJsonAry.length(); i < len; i++) {
                    try {
                        String userId = userIdsJsonAry.getString(i);

                        userIds.add(userId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                roomInfo.setUserIds(userIds);
            }

            if (roomInfo == null || TextUtils.isEmpty(roomInfo.getRoomId())) {
                return;
            }

            MessageInfo msgInfo = new MessageInfo();
            msgInfo.setMessageType(-1);
            msgInfo.setEventResponseType(1);
            msgInfo.setMessage(roomInfo.getRoomId());
            MessageReceiveEvent<MessageInfo> msgRecvEvent = new MessageReceiveEvent<>(msgInfo);
            EventBus.getDefault().post(msgRecvEvent);
        }).on("join-room-success", (Object... args) -> {
            if (!isReceivedArgsValid(args)) {
                return;
            }

            String roomID = args[0].toString();
            if (TextUtils.isEmpty(roomID)) {
                return;
            }
            MessageInfo msgInfo = new MessageInfo();
            msgInfo.setMessageType(-1);
            msgInfo.setEventResponseType(2);
            msgInfo.setMessage(roomID);
            MessageReceiveEvent<MessageInfo> msgRecvEvent = new MessageReceiveEvent<>(msgInfo);

            EventBus.getDefault().post(msgRecvEvent);
        }).on("receive-message", (Object... args) -> {
            if (!isReceivedArgsValid(args)) {
                return;
            }

            String msgInfoJsonStr = args[0].toString();
            MessageInfo msgInfo = mGon.fromJson(msgInfoJsonStr, MessageInfo.class);
            MessageReceiveEvent<MessageInfo> msgRecvEvent = new MessageReceiveEvent<>(msgInfo);
            EventBus.getDefault().post(msgRecvEvent);
        }).on("error", (Object... args) -> {
            Log.d("randy", args.toString());
        });
    }

    private boolean isReceivedArgsValid(Object... args) {
        return args != null && args.length > 0;
    }

    public void createRoom(int roomType, UserInfo userInfo) {
        if (!isConnected()) {
            return;
        }
        //JsonArray jsonAry = new JsonArray();

        //jsonAry.add(mGon.toJson(userInfo));
        mSocket.emit("create-room", roomType, mGon.toJson(userInfo));
    }

    public void joinRoom(String roomId, UserInfo userInfo) {
        if (!isConnected()) {
            return;
        }
        mSocket.emit("join-room", roomId, mGon.toJson(userInfo));
    }

    public void leaveRoom(String roomId, String userId) {
        if (!isConnected()) {
            return;
        }
        mSocket.emit("leave-room", roomId, userId);
    }

    public void inviteMember(String roomId, UserInfo... memberInfoAry) {
        JsonArray memberInfoJsonAry = (JsonArray) mGon.toJsonTree(Arrays.asList(memberInfoAry), new TypeToken<List<UserInfo>>() {}.getType());

        mSocket.emit("invite_member", roomId, memberInfoJsonAry.toString());
    }

    public void sendMessage(MessageInfo msgInfo) {
        if (!isConnected()) {
            return;
        }

        mSocket.emit("send-message", mGon.toJson(msgInfo));
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.connected();
    }

    public void connect() {
        mSocket.connect();
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        this.mSocket.disconnect();
    }
}
