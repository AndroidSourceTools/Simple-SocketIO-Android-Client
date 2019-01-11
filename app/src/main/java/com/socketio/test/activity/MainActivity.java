package com.socketio.test.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.socketio.test.R;
import com.socketio.test.adapter.MemberListAdapter;
import com.socketio.test.adapter.MessageListAdapter;
import com.socketio.test.api.ApiInstManager;
import com.socketio.test.api.IApi;
import com.socketio.test.model.MessageInfo;
import com.socketio.test.model.MessageReceiveEvent;
import com.socketio.test.model.ResponseInfo;
import com.socketio.test.model.RoomInfo;
import com.socketio.test.model.UserInfo;
import com.socketio.test.utils.Constants;
import com.socketio.test.utils.SocketIOManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MessageListAdapter.IListStatusListener, MemberListAdapter.IMemberItemClickListener {

    public static final String EXTRA_KEY_USER_INFO = "extra_key_user_info";

    @ViewById(R.id.v_status_bar)
    View mVStatusBar;
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

    private Handler mHandler;
    private SocketIOManager mSocketMgr;
    private IApi mApiInst;
    private MessageListAdapter mMsgListAdapter;
    private MemberListAdapter mMemberListAdapter;
    private Gson mGson;
    private String mRoomId = null;
    private UserInfo mUserInfo;
    private boolean mIsImmersiveHasInit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void initImmersive() {
        ImmersionBar.with(this)
                .statusBarView(mVStatusBar)
                .transparentNavigationBar()
                .hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
                .keyboardEnable(true)
                .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                .setOnKeyboardListener((isPopup, keyboardHeight) -> {
                    if (!isPopup && !mIsImmersiveHasInit) {
                        mIsImmersiveHasInit = true;
                        ImmersionBar.with(MainActivity.this).destroy();
                        initImmersive();
                    } else if (isPopup && mIsImmersiveHasInit) {
                        mIsImmersiveHasInit = false;
                    }
                })
                .init();
    }

    @AfterViews
    void initView() {
        initImmersive();

        LinearLayoutManager msgListLayoutMgr = new LinearLayoutManager(this);
        msgListLayoutMgr.setStackFromEnd(true);
        mRvMsgList.setLayoutManager(msgListLayoutMgr);
        mRvMsgList.setHasFixedSize(true);
        mRvMsgList.addItemDecoration(new DividerItemDecoration(this, 0));
        mRvMsgList.setItemAnimator(new DefaultItemAnimator());

        mRvMemberList.setLayoutManager(new LinearLayoutManager(this));
        mRvMemberList.setHasFixedSize(true);
        mRvMemberList.addItemDecoration(new DividerItemDecoration(this, 0));
        mRvMemberList.setItemAnimator(new DefaultItemAnimator());

        mDlDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                Log.d(Constants.TAG, "onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                Log.d(Constants.TAG, "onDrawerClosed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @AfterViews
    void init() {
        Intent intent = getIntent();
        mHandler = new Handler();
        mUserInfo = intent.getParcelableExtra(EXTRA_KEY_USER_INFO);
        mGson = new Gson();
        mApiInst = ApiInstManager.getApiInstance();
        mMsgListAdapter = new MessageListAdapter(this, mUserInfo.getUserId(), this);
        mMemberListAdapter = new MemberListAdapter(this, this);

        mRvMsgList.setAdapter(mMsgListAdapter);
        mRvMemberList.setAdapter(mMemberListAdapter);

        initSocketIo();
        EventBus.getDefault().register(this);
    }

    private void initSocketIo() {
        SocketIOManager.Options options = new SocketIOManager.Options();
        mSocketMgr = SocketIOManager.getInstance();
        // Init socket io commit
        options.host(Constants.SOCKET_SERVER_URL)
                .isForceNew(true)
                .reconnection(false)
                .query("auth_token=" + mUserInfo.getUserId());
        mSocketMgr.init(options);
        mSocketMgr.connect();
    }

    private void refreshMemberList(RoomInfo roomInfo) {
        // Init member list
        mApiInst.getUserInfoList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseInfo responseInfo) {
                        if (responseInfo.getStatus() == HttpURLConnection.HTTP_OK) {
                            Map<String, Object> payload = responseInfo.getPayload();
                            List<UserInfo> userInfoList = mGson.fromJson(payload.get("user_info_list").toString(), new TypeToken<List<UserInfo>>() {
                            }.getType());

                            if (roomInfo != null) {
                                List<String> userIdList = roomInfo.getUserIdList();

                                for (String userId : userIdList) {
                                    for (UserInfo userInfo : userInfoList) {
                                        if (!TextUtils.equals(userId, userInfo.getUserId())) {
                                            continue;
                                        }
                                        mMemberListAdapter.addMemberInfo(userInfo);
                                        break;
                                    }
                                }
                            }

                            mMsgListAdapter.updateMemberInfoList(userInfoList);
                        } else {
                            onError(new Throwable(new StringBuilder("Api fail status = ")
                                    .append(responseInfo.getStatus()).append(" , message = ")
                                    .append(responseInfo.getMessage())
                                    .toString()));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(Constants.TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(mRoomId) && mUserInfo != null) {
            mSocketMgr.joinRoom(mRoomId, mUserInfo);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!TextUtils.isEmpty(mRoomId) && mUserInfo != null) {
            mSocketMgr.leaveRoom(mRoomId, mUserInfo.getUserId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocketMgr.disconnect();
        ImmersionBar.with(this).destroy();
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

                        switch (eventResponseType) {
                            case 2: {
                                UserInfo userInfo = mGson.fromJson(msgInfo.getMessage(), UserInfo.class);

                                msgInfo.setMessage(userInfo.getUserName() + "加入聊天室");
                                mMemberListAdapter.addMemberInfo(userInfo);
                            }
                            break;
                            case 3: {
                                UserInfo userInfo = mGson.fromJson(msgInfo.getMessage(), UserInfo.class);

                                msgInfo.setMessage(userInfo.getUserName() + "離開聊天室");
                                mMemberListAdapter.removeMemberInfo(userInfo);
                            }
                            break;
                        }
                    }
                    break;
                    case 1: {
                        Log.d(Constants.TAG, "Receive message = " + msgInfo.getMessage() + " from " + msgInfo.getUserId());
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
            } else {
                switch (eventResponseType) {
                    case 0: {
                        Log.d(Constants.TAG, "Connected...");

                        mSocketMgr.createRoom(1, mUserInfo);
                    }
                    break;
                    case 1: {
                        // Event for creating room, message content is RoomID
                        mRoomId = msgInfo.getMessage();

                        mSocketMgr.joinRoom(mRoomId, mUserInfo);
                        Log.d(Constants.TAG, "Create Room...");
                        // TODO: For inviteMember testing
                        //mSocketMgr.inviteMember(mRoomId, mUserInfo);
                    }
                    break;
                    case 2: {
                        RoomInfo roomInfo = mGson.fromJson(msgInfo.getMessage(), RoomInfo.class);
                        mRoomId = roomInfo.getRoomId();

                        refreshMemberList(roomInfo);
                        Log.d(Constants.TAG, "Join Room...");
                    }
                    break;
                    case 3: {
                        Log.d(Constants.TAG, "Leave Room...");
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

    @Override
    public void onMemberItemClick() {

    }
}
