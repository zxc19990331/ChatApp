package com.stellaris.stchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.stellaris.stchat.R;
import com.stellaris.stchat.utils.HandleResponseCode;
import com.stellaris.stchat.utils.LoginUtils;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.login_userName)
    EditText mReg_userName;
    @BindView(R.id.login_password)
    EditText mReg_password;
    @BindView(R.id.login_password_again)
    EditText mReg_password_again;
    @BindView(R.id.btn_register)
    Button mBtn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_register)
    public void onViewClicked() {
        final String userId = getUserName();
        final String password = getPassword();
        final String passwordAgain = getPasswordAgain();
        if (TextUtils.isEmpty(userId)) {
            ToastUtil.shortToast(this, "用户名不能为空");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtil.shortToast(this, "密码不能为空");
            return;
        }
        if (userId.length() < 4 || userId.length() > 128) {
            ToastUtil.shortToast(this, "用户名为4-128位字符");
            return;
        }
        if (password.length() < 4 || password.length() > 128) {
            ToastUtil.shortToast(this, "密码为4-128位字符");
            return;
        }
        if (LoginUtils.isContainChinese(userId)) {
            ToastUtil.shortToast(this, "用户名不支持中文");
            return;
        }
        if (!LoginUtils.whatStartWith(userId)) {
            ToastUtil.shortToast(this, "用户名以字母或者数字开头");
            return;
        }
        if (!LoginUtils.whatContain(userId)) {
            ToastUtil.shortToast(this, "只能含有: 数字 字母 下划线 . - @");
            return;
        }
        if(!password.equals(passwordAgain)){
            ToastUtil.shortToast(this, "两次密码输入不一致！");
            return;
        }
        JMessageClient.register(userId, password, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    SharePreferenceManager.setRegisterName(userId);
                    SharePreferenceManager.setRegistePass(password);
                    goToActivity(RegisterActivity.this,FinishRegisterActivity.class);
                    ToastUtil.shortToast(RegisterActivity.this, "注册成功");
                } else {
                    HandleResponseCode.onHandle(RegisterActivity.this, i, false);
                }
            }
        });

    }

    public String getUserName(){
        return mReg_userName.getText().toString().trim();
    }

    public String getPassword(){
        return mReg_password.getText().toString().trim();
    }

    public String getPasswordAgain(){
        return mReg_password_again.getText().toString().trim();
    }
}
