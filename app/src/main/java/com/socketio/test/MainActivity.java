package com.socketio.test;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.socketio.test.adapter.MessageListAdapter;
import com.socketio.test.api.ApiInstManager;
import com.socketio.test.api.IApi;
import com.socketio.test.model.MessageInfo;
import com.socketio.test.model.MessageReceiveEvent;
import com.socketio.test.model.ResponseInfo;
import com.socketio.test.model.UserInfo;
import com.socketio.test.utils.SocketIOManager;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.HashMap;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MessageListAdapter.IListStatus {

    public static final String EXTRA_KEY_USER_INFO = "extra_key_user_info";

    @ViewById(R.id.rv_msg_list)
    RecyclerView mRvMsgList;
    @ViewById(R.id.rv_member_list)
    RecyclerView mRvMemberList;
    @ViewById(R.id.et_message_box)
    EditText mEtMessageBox;
    @ViewById(R.id.btn_send_msg)
    ImageButton mBtnSendMsg;
    @ViewById(R.id.dl_drawer_layout)
    DrawerLayout mDlDrawerLayout;

    private SocketIOManager mSocketMgr;
    private IApi mApiInst;
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
        LinearLayoutManager msgListLayoutMgr = new LinearLayoutManager(this);

        msgListLayoutMgr.setStackFromEnd(true);
        mRvMsgList.setLayoutManager(msgListLayoutMgr);
        mRvMsgList.setAdapter(mMsgListAdapter);
        mRvMsgList.setHasFixedSize(true);
        mRvMsgList.addItemDecoration(new DividerItemDecoration(this, 0));
        mRvMsgList.setItemAnimator(new DefaultItemAnimator());

        mDlDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                Log.d("randy", "onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                Log.d("randy", "onDrawerClosed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void init() {
        Intent intent = getIntent();
        mUserInfo = intent.getParcelableExtra(EXTRA_KEY_USER_INFO);
        mGson = new Gson();
        mRoomUserInfoMap = new HashMap<>();
        mSocketMgr = SocketIOManager.getInstance();
        mApiInst = ApiInstManager.getApiInstance();
        SocketIOManager.Options options = new SocketIOManager.Options();

        // Init socket io commit
        options.host("https://172.20.10.2:8081")
                .isForceNew(true)
                .reconnection(false)
                .query("auth_token=" + mUserInfo.getUserId());
        mSocketMgr.init(options);
        mSocketMgr.connect();

        // TODO: for getUserInfoList testing
        mApiInst.getUserInfoList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(ResponseInfo responseInfo) {
                        Log.d("randy", "");
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onComplete() {}
                });

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
                        // TODO: For inviteMember testing
                        mSocketMgr.inviteMember(mRoomId, mUserInfo);
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
