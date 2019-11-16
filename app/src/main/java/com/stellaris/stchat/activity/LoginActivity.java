package com.stellaris.stchat.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.database.UserEntry;
import com.stellaris.stchat.utils.BitmapLoader;
import com.stellaris.stchat.utils.DialogCreator;
import com.stellaris.stchat.utils.LoginUtils;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ToastUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_userName)
    EditText mLogin_userName;
    @BindView(R.id.login_password)
    EditText mLogin_password;
    @BindView(R.id.btn_login)
    Button mBtn_login;
    @BindView(R.id.login_register)
    TextView mLogin_register;

    public Context mContext = this;
    @BindView(R.id.de_login_logo)
    ImageView mIv_login_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initView();
    }

    public void initView() {
        //退出登录重现上次的账号以及头像
        String userName = SharePreferenceManager.getCachedUsername();
        String userAvatar = SharePreferenceManager.getCachedAvatarPath();
        Bitmap bitmap = BitmapLoader.getBitmapFromFile(userAvatar, mAvatarSize, mAvatarSize);
        if (bitmap != null) {
            mIv_login_logo.setImageBitmap(bitmap);
        } else {
            mIv_login_logo.setImageResource(R.drawable.avatar_default);
        }
        mLogin_userName.setText(userName);
        if (userName != null)
            mLogin_userName.setSelection(userName.length());//设置光标位置
    }

    @OnClick({R.id.btn_login, R.id.login_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login: {
                final String userId = getUserName();
                final String password = getPassword();
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
                final Dialog dialog = DialogCreator.createLoadingDialog(this,
                        this.getString(R.string.login_hint));
                dialog.show();
                JMessageClient.login(userId, password, new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                            SharePreferenceManager.setCachedPsw(password);
                            UserInfo myInfo = JMessageClient.getMyInfo();
                            File avatarFile = myInfo.getAvatarFile();
                            //登陆成功,如果用户有头像就把头像存起来,没有就设置null
                            if (avatarFile != null) {
                                SharePreferenceManager.setCachedAvatarPath(avatarFile.getAbsolutePath());
                            } else {
                                SharePreferenceManager.setCachedAvatarPath(null);
                            }
                            String username = myInfo.getUserName();
                            String appKey = myInfo.getAppKey();
                            UserEntry user = UserEntry.getUser(username, appKey);
                            if (null == user) {
                                user = new UserEntry(username, appKey);
                                user.save();
                            }
                            goToActivity(LoginActivity.this, MainActivity.class);
                            ToastUtil.shortToast(LoginActivity.this, "登陆成功");
                            finish();
                        } else {
                            ToastUtil.shortToast(LoginActivity.this, "登陆失败:" + responseMessage);
                        }
                        dialog.dismiss();
                    }
                });
            }
            break;
            case R.id.login_register: {
                goToActivity(LoginActivity.this, RegisterActivity.class);
            }
            break;
        }
    }

    public String getUserName() {
        return mLogin_userName.getText().toString().trim();
    }

    public String getPassword() {
        return mLogin_password.getText().toString().trim();
    }

}
