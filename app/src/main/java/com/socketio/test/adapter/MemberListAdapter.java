package com.socketio.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.socketio.test.R;
import com.socketio.test.model.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.MemberViewHolder> {

    public interface IMemberItemClickListener {
        void onMemberItemClick();
    }

    private LayoutInflater mInflator;
    private IMemberItemClickListener mListener;
    private ArrayList<UserInfo> mMemberInfoList;

    public MemberListAdapter(Context ctx, IMemberItemClickListener listener) {
        this.mInflator = LayoutInflater.from(ctx);
        this.mListener = listener;
        this.mMemberInfoList = new ArrayList<>();
    }

    public void addMemberInfo(UserInfo... userInfos) {
        boolean isChanged = false;

        for (UserInfo userInfo : userInfos) {
            if (mMemberInfoList.contains(userInfo)) {
                continue;
            }
            isChanged = true;
            mMemberInfoList.add(userInfo);
        }

        if (!isChanged) {
            return;
        }

        notifyDataSetChanged();
    }

    public void removeMemberInfo(UserInfo... userInfos) {
        boolean isChanged = false;

        for (UserInfo userInfo : userInfos) {
            if (!mMemberInfoList.contains(userInfo)) {
                continue;
            }
            isChanged = true;
            mMemberInfoList.remove(userInfo);
        }

        if (!isChanged) {
            return;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberViewHolder(mInflator.inflate(R.layout.view_member_info, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        UserInfo memberInfo = mMemberInfoList.get(position);

        holder.mTvName.setText(memberInfo.getUserName());
    }

    @Override
    public int getItemCount() {
        return mMemberInfoList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_avatar)
        ImageView mIvAvatar;
        @BindView(R.id.tv_name)
        TextView mTvName;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
