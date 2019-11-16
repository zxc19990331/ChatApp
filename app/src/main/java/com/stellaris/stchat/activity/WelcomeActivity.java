package com.stellaris.stchat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.stellaris.stchat.R;
import com.yanzhenjie.permission.AndPermission;

import java.util.ArrayList;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

public class WelcomeActivity extends BaseActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mContext = this;
        initData();
    }

    private void initData() {
        //检测账号是否登陆
        UserInfo myInfo = JMessageClient.getMyInfo();
        if (myInfo == null) {
            goToRegisterAndLoginActivity();
        }else {
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        startActivity(new Intent(mContext, MainActivity.class));
        finish();
    }

    private void goToRegisterAndLoginActivity() {
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

    private void goToTestAcitivity(){
        startActivity(new Intent(mContext, RegisterActivity.class));
        finish();
    }


}
