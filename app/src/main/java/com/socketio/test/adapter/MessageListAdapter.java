package com.socketio.test.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.socketio.test.R;
import com.socketio.test.model.MessageInfo;
import com.socketio.test.model.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.socketio.test.adapter.MessageListAdapter.MESSAGE_FROM_CONSTANT.EVENT_MESSAGE;
import static com.socketio.test.adapter.MessageListAdapter.MESSAGE_FROM_CONSTANT.FROM_MY_MESSAGE;
import static com.socketio.test.adapter.MessageListAdapter.MESSAGE_FROM_CONSTANT.FROM_THEIR_MESSAGE;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface IListStatusListener {
        void onItemAdded();
    }

    enum MESSAGE_FROM_CONSTANT {
        FROM_MY_MESSAGE, FROM_THEIR_MESSAGE, EVENT_MESSAGE
    }

    private IListStatusListener mListener;
    private final LayoutInflater mInflator;
    private final ArrayList<MessageInfo> mMsgInfoList;
    private HashMap<String, UserInfo> mRoomUserInfoMap;
    private final String mUserId;

    public MessageListAdapter(Context ctx, String userId, IListStatusListener listener) {
        this.mListener = listener;
        this.mUserId = userId;
        this.mInflator = LayoutInflater.from(ctx);
        this.mMsgInfoList = new ArrayList<>();
        this.mRoomUserInfoMap = new HashMap<>();
    }

    public void addMemberInfo(UserInfo... userInfos) {
        for (UserInfo userInfo : userInfos) {
            removeMemberInfo(userInfo);
            mRoomUserInfoMap.put(userInfo.getUserId(), userInfo);
        }
    }

    public void removeMemberInfo(UserInfo... userInfos) {
        for (UserInfo userInfo : userInfos) {
            if (!mRoomUserInfoMap.containsKey(userInfo.getUserId())) {
                continue;
            }
            mRoomUserInfoMap.remove(userInfo.getUserId());
        }
    }

    public void addMessageInfos(MessageInfo... msgInfos) {
        int changeFrom = getItemCount();

        mMsgInfoList.addAll(Arrays.asList(msgInfos));
        notifyItemRangeInserted(changeFrom, msgInfos.length);

        if (mListener != null) {
            mListener.onItemAdded();
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageInfo msgInfo = mMsgInfoList.get(position);
        int eventResponseType = msgInfo.getEventResponseType();
        int messageType = msgInfo.getMessageType();

        if (eventResponseType != -1) {
            return EVENT_MESSAGE.ordinal();
        } else {
            return this.mUserId.equals(msgInfo.getUserId()) ? FROM_MY_MESSAGE.ordinal() : FROM_THEIR_MESSAGE.ordinal();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == FROM_MY_MESSAGE.ordinal()) {
            return new MyMessageItemViewHolder(mInflator.inflate(R.layout.view_my_message, viewGroup, false));
        } else if (viewType == FROM_THEIR_MESSAGE.ordinal()) {
            return new TheirMessageItemViewHolder(mInflator.inflate(R.layout.view_their_message, viewGroup, false));
        } else {
            return new EventMessageItemViewHolder(mInflator.inflate(R.layout.view_event_message, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder messageItemViewHolder, int i) {
        MessageInfo msgInfo = mMsgInfoList.get(i);
        int viewType = messageItemViewHolder.getItemViewType();

        if (viewType == FROM_MY_MESSAGE.ordinal()) {
            MyMessageItemViewHolder myMsgItemHolder = (MyMessageItemViewHolder) messageItemViewHolder;

            myMsgItemHolder.mMyMsgBody.setText(msgInfo.getMessage());
        } else if (viewType == FROM_THEIR_MESSAGE.ordinal()) {
            TheirMessageItemViewHolder theirMsgItemHolder = (TheirMessageItemViewHolder) messageItemViewHolder;
            String theirName = (mRoomUserInfoMap.containsKey(msgInfo.getUserId())) ? mRoomUserInfoMap.get(msgInfo.getUserId()).getUserName() : null;

            theirMsgItemHolder.mTheirName.setText(theirName);
            theirMsgItemHolder.mTheirMsgBody.setText(msgInfo.getMessage());
        } else {
            EventMessageItemViewHolder eventMsgItemHolder = (EventMessageItemViewHolder) messageItemViewHolder;

            eventMsgItemHolder.mEventMsgBody.setText(msgInfo.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgInfoList.size();
    }

    static class MyMessageItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.my_message_body)
        TextView mMyMsgBody;

        public MyMessageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class TheirMessageItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.avatar)
        ImageView mTheirAvatar;
        @BindView(R.id.name)
        TextView mTheirName;
        @BindView(R.id.their_message_body)
        TextView mTheirMsgBody;

        public TheirMessageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class EventMessageItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_event_message_body)
        TextView mEventMsgBody;

        public EventMessageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
