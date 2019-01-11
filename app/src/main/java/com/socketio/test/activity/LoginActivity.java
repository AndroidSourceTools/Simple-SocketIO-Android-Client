package com.socketio.test.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.socketio.test.R;
import com.socketio.test.api.ApiInstManager;
import com.socketio.test.api.IApi;
import com.socketio.test.model.ResponseInfo;
import com.socketio.test.model.UserInfo;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import static com.socketio.test.activity.MainActivity.EXTRA_KEY_USER_INFO;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {

    @ViewById(R.id.et_user_name)
    EditText mEtUserName;
    @ViewById(R.id.et_user_pwd)
    EditText mEtUserPwd;
    @ViewById(R.id.btn_signup)
    Button mBtnSignIn;
    @ViewById(R.id.btn_login)
    Button mBtnLogin;

    private Gson mGson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGson = new Gson();
    }

    @Click({R.id.btn_login, R.id.btn_signup})
    public void onViewClicked(View view) {
        int id = view.getId();

        String name = mEtUserName.getText().toString();
        String pwd = mEtUserPwd.getText().toString();
        IApi apiInst = ApiInstManager.getApiInstance();

        if (id == R.id.btn_signup) {
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(LoginActivity.this, getString(R.string.toast_hint_no_valid_user_name_and_pwd), Toast.LENGTH_LONG).show();
                return;
            }

            apiInst.signUpUser(name, pwd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseInfo>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(ResponseInfo responseInfo) {
                            Toast.makeText(LoginActivity.this, getString(R.string.toast_hint_user_signup_success), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });

        } else if (id == R.id.btn_login) {
            String userName = mEtUserName.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(LoginActivity.this, getString(R.string.toast_hint_no_valid_user_name_and_pwd), Toast.LENGTH_LONG).show();
                return;
            }

            apiInst.signInUser(userName, pwd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseInfo>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(ResponseInfo responseInfo) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity_.class);
                            UserInfo userInfo = mGson.fromJson(mGson.toJson(responseInfo.getPayload()), UserInfo.class);

                            intent.putExtra(EXTRA_KEY_USER_INFO, userInfo);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(LoginActivity.this, getString(R.string.toast_hint_login_fail), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }
}
