package com.socketio.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.socketio.test.MainActivity.EXTRA_KEY_INVITED_USER_ID;
import static com.socketio.test.MainActivity.EXTRA_KEY_USER_ID;
import static com.socketio.test.MainActivity.EXTRA_KEY_USER_NAME;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_user_name)
    EditText mEtUserName;
    @BindView(R.id.btn_login)
    Button mBtnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        long curUserID = 1545807642;
        long invitedUserID = 2;
//        String curUserID = "2";
//        String invitedUserID = "1";



        String userName = mEtUserName.getText().toString();
        if(TextUtils.isEmpty(userName)) {
            Toast.makeText(LoginActivity.this, "請輸入使用者名稱", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_KEY_USER_NAME, userName);
        // TODO: Need a real user id
        intent.putExtra(EXTRA_KEY_USER_ID, curUserID);
        intent.putExtra(EXTRA_KEY_INVITED_USER_ID, invitedUserID);
        startActivity(intent);
        finish();
    }
}
