package com.socketio.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.socketio.test.adapter.MessageListAdapter;
import com.socketio.test.model.MessageInfo;
import com.socketio.test.model.MessageReceiveEvent;
import com.socketio.test.model.UserInfo;
import com.socketio.test.utils.SocketIOManager;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.HashMap;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MessageListAdapter.IListStatus {

    public static final String EXTRA_KEY_USER_NAME = "extra_key_user_name";
    public static final String EXTRA_KEY_USER_ID = "extra_key_user_id";
    public static final String EXTRA_KEY_INVITED_USER_ID = "extra_key_invited_user_id";

    @ViewById(R.id.rv_msg_list)
    RecyclerView mRvMsgList;
    @ViewById(R.id.et_message_box)
    EditText mEtMessageBox;
    @ViewById(R.id.btn_send_msg)
    ImageButton mBtnSendMsg;

    private SocketIOManager mSocketMgr;
    private MessageListAdapter mMsgListAdapter;
    private Gson mGson;
    private String mRoomId = null;
    private UserInfo mUserInfo;
    private HashMap<String, UserInfo> mRoomUserInfoMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initView();
    }

    private void initView() {
        mMsgListAdapter = new MessageListAdapter(this, mUserInfo.getUserId(), this);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(this);

        layoutMgr.setStackFromEnd(true);
        mRvMsgList.setLayoutManager(layoutMgr);
        mRvMsgList.setAdapter(mMsgListAdapter);
        mRvMsgList.setHasFixedSize(true);
        mRvMsgList.addItemDecoration(new DividerItemDecoration(this, 0));
        mRvMsgList.setItemAnimator(new DefaultItemAnimator());
    }

    private void init() {
        Intent intent = getIntent();
        String userName = intent.getStringExtra(EXTRA_KEY_USER_NAME);
        // userId supposed to be user token
        long userId = intent.getLongExtra(EXTRA_KEY_USER_ID, -1);
        mGson = new Gson();
        mUserInfo = new UserInfo();
        mRoomUserInfoMap = new HashMap<>();
        mSocketMgr = SocketIOManager.getInstance();
        SocketIOManager.Options options = new SocketIOManager.Options();

        mUserInfo.setUserId(Long.toString(userId));
        mUserInfo.setUserName(userName);

        // Init socket io commit
        options.host("https://10.24.100.101:3000")
                .isForceNew(true)
                .reconnection(false)
                .query("auth_token=" + userId);
        mSocketMgr.init(options);
        mSocketMgr.connect();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocketMgr.leaveRoom(mRoomId, mUserInfo);
        mSocketMgr.disconnect();
        EventBus.getDefault().unregister(this);
    }

    @Click(R.id.btn_send_msg)
    public void onSendMessage(View view) {

        if (TextUtils.isEmpty(mUserInfo.getUserName())) {
            Toast.makeText(MainActivity.this, "請輸入使用者名稱", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String msg = mEtMessageBox.getText().toString();
        sendMessage(1, msg);
        mEtMessageBox.getText().clear();
    }

    private void sendMessage(int msgType, String msg) {
        MessageInfo msgInfo = new MessageInfo();

        // TODO: ROOD ID is fixed currently
        msgInfo.setUserId(mUserInfo.getUserId());
        msgInfo.setRoomId(mRoomId);
        msgInfo.setMessageTime(System.currentTimeMillis());
        msgInfo.setMessageType(msgType);
        msgInfo.setEventResponseType(-1);
        msgInfo.setMessage(msg);
        mSocketMgr.sendMessage(msgInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageReceiveEvent event) {
        Object payload = event.getPayload();

        if (payload instanceof MessageInfo) {
            MessageInfo msgInfo = (MessageInfo) payload;
            int eventResponseType = msgInfo.getEventResponseType();
            int msgType = msgInfo.getMessageType();

            if (eventResponseType == -1 && msgType == -1) {
                return;
            }

            if (msgType != -1) {
                switch (msgType) {
                    case 0: {
                        if (eventResponseType == -1) {
                            // Unknown error event message
                            return;
                        }

//                        List<UserInfo> newUserInfoList = mGson.fromJson(msgInfo.getMessage(), new TypeToken<List<UserInfo>>(){}.getType());
//                        List<UserInfo> oldUserInfoList = new ArrayList<>();
//
//                        oldUserInfoList.addAll(mRoomUserInfoMap.values());
//
//                        if(newUserInfoList.size() > oldUserInfoList.size()) {
//                            newUserInfoList.removeAll(oldUserInfoList);
//                        } else {
//                            oldUserInfoList.removeAll(newUserInfoList);
//                        }

                        switch (eventResponseType) {
                            case 2: {
                                UserInfo userInfo = mGson.fromJson(msgInfo.getMessage(), UserInfo.class);

                                msgInfo.setMessage(userInfo.getUserName() + "加入聊天室");
                            }
                            break;
                            case 3: {
                                UserInfo userInfo = mGson.fromJson(msgInfo.getMessage(), UserInfo.class);

                                msgInfo.setMessage(userInfo.getUserName() + "離開聊天室");
                            }
                            break;
                        }
                    }
                    break;
                    case 1: {
                        Log.d("randy", "Receive message = " + msgInfo.getMessage() + " from " + msgInfo.getUserId());
                    }
                    break;
                    case 2: {

                    }
                    break;
                    case 3: {

                    }
                    break;
                    case 4: {

                    }
                    break;
                }
                mMsgListAdapter.addMessageInfos(msgInfo);
                mMsgListAdapter.updateRoomUserInfoMap(mRoomUserInfoMap);
            } else {
                switch (eventResponseType) {
                    case 0: {
                        Log.d("randy", "Connected...");

                        mSocketMgr.createRoom(1, mUserInfo);
                    }
                    break;
                    case 1: {
                        // Event for creating room, message content is RoomID
                        mRoomId = msgInfo.getMessage();
                        mSocketMgr.joinRoom(mRoomId, mUserInfo);
                        Log.d("randy", "Create Room...");
                    }
                    break;
                    case 2: {
                        Log.d("randy", "Join Room...");
                    }
                    break;
                    case 3: {
                        Log.d("randy", "Leave Room...");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onItemAdded() {
        mRvMsgList.smoothScrollToPosition(mMsgListAdapter.getItemCount() - 1);
    }
}
