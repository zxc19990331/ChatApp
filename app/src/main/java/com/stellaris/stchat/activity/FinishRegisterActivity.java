package com.stellaris.stchat.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stellaris.stchat.R;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.database.UserEntry;
import com.stellaris.stchat.utils.ClearWriteEditText;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ThreadUtil;
import com.stellaris.stchat.utils.photochoose.ChoosePhoto;
import com.stellaris.stchat.utils.photochoose.SelectableRoundedImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class FinishRegisterActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView mIv_back;
    @BindView(R.id.mine_header)
    SelectableRoundedImageView mIv_header;
    @BindView(R.id.nick_name_et)
    ClearWriteEditText mEt_nickname;
    @BindView(R.id.tv_nickCount)
    TextView mTv_nickname;
    @BindView(R.id.finish_btn)
    Button mBtn_finish;
    ChoosePhoto mChoosePhoto;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_register);
        ButterKnife.bind(this);
        initView();
    }

    public void initView(){
        mEt_nickname.requestFocus();
        
    }


    @OnClick({R.id.iv_back, R.id.mine_header, R.id.tv_nickCount, R.id.finish_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.mine_header:{

                if ((ContextCompat.checkSelfPermission(FinishRegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(FinishRegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(FinishRegisterActivity.this, "请在应用管理中打开“读写存储”和“相机”访问权限！", Toast.LENGTH_SHORT).show();
                } else {
                    mChoosePhoto.showPhotoDialog(FinishRegisterActivity.this);
                }
            }
                break;
            case R.id.tv_nickCount:
                break;
            case R.id.finish_btn:
                mDialog = new ProgressDialog(FinishRegisterActivity.this);
                mDialog.setCancelable(false);
                mDialog.show();

                final String userId = SharePreferenceManager.getRegistrName();
                final String password = SharePreferenceManager.getRegistrPass();
                SharePreferenceManager.setRegisterUsername(userId);
                JMessageClient.login(userId, password, new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                            StApplication.registerOrLogin = 1;
                            String username = JMessageClient.getMyInfo().getUserName();
                            String appKey = JMessageClient.getMyInfo().getAppKey();
                            UserEntry user = UserEntry.getUser(username, appKey);
                            if (null == user) {
                                user = new UserEntry(username, appKey);
                                user.save();
                            }

                            String nickName = mEt_nickname.getText().toString();

                            UserInfo myUserInfo = JMessageClient.getMyInfo();
                            if (myUserInfo != null) {
                                myUserInfo.setNickname(nickName);
                            }
                            //注册时候更新昵称
                            JMessageClient.updateMyInfo(UserInfo.Field.nickname, myUserInfo, new BasicCallback() {
                                @Override
                                public void gotResult(final int status, String desc) {
                                    //更新跳转标志
                                    SharePreferenceManager.setCachedFixProfileFlag(false);
                                    mDialog.dismiss();
                                    if (status == 0) {
                                        goToActivity(FinishRegisterActivity.this, MainActivity.class);
                                    }
                                }
                            });
                            //注册时更新头像
                            final String avatarPath = SharePreferenceManager.getRegisterAvatarPath();
                            if (avatarPath != null) {
                                ThreadUtil.runInThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JMessageClient.updateUserAvatar(new File(avatarPath), new BasicCallback() {
                                            @Override
                                            public void gotResult(int responseCode, String responseMessage) {
                                                if (responseCode == 0) {
                                                    SharePreferenceManager.setCachedAvatarPath(avatarPath);
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                SharePreferenceManager.setCachedAvatarPath(null);
                            }
                        }
                    }
                });
                break;
        }
    }
}
