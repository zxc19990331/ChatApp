package com.stellaris.stchat.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stellaris.stchat.R;
import com.stellaris.stchat.utils.DialogCreator;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.photochoose.ChoosePhoto;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;

public class PersonalActivity extends BaseActivity {

    @BindView(R.id.iv_photo)
    ImageView mIv_avatar;
    @BindView(R.id.tv_userName)
    TextView mTv_userName;
    @BindView(R.id.tv_nickName)
    TextView mTv_nickName;
    @BindView(R.id.rl_nickName)
    RelativeLayout mRl_nickName;
    ChoosePhoto mChoosePhoto;
    private UserInfo mMyInfo;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    public void initData(){
        final Dialog dialog = DialogCreator.createLoadingDialog(PersonalActivity.this,
                PersonalActivity.this.getString(R.string.jmui_loading));
        dialog.show();
        mMyInfo = JMessageClient.getMyInfo();
        if(mMyInfo!=null){
            String nickname = mMyInfo.getNickname().isEmpty()?mMyInfo.getUserName():mMyInfo.getNickname();
            mTv_nickName.setText(nickname);
            mTv_userName.setText("用户名:" + mMyInfo.getUserName());
            mMyInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                    if (responseCode == 0) {
                        mIv_avatar.setImageBitmap(avatarBitmap);
                    } else {
                        mIv_avatar.setImageResource(R.drawable.avatar_default);
                    }
                }
            });
            dialog.dismiss();
        }
    }

    public void initView(){
        initTitle(true, true, "个人信息", "", false, "");
        mChoosePhoto = new ChoosePhoto();
        mChoosePhoto.setPortraitChangeListener(PersonalActivity.this, mIv_avatar, 2);
    }

    @OnClick({R.id.iv_photo, R.id.tv_userName, R.id.rl_nickName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_photo:
                if ((ContextCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(PersonalActivity.this, "请在应用管理中打开“读写存储”和“相机”访问权限！", Toast.LENGTH_SHORT).show();
                }
                mChoosePhoto.setInfo(PersonalActivity.this, true);
                mChoosePhoto.showPhotoDialog(PersonalActivity.this);
                break;
            case R.id.tv_userName:
                break;
            case R.id.rl_nickName:
                break;
        }
    }
}
