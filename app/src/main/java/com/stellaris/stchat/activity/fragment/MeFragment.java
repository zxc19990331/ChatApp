package com.stellaris.stchat.activity.fragment;


import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.LoginActivity;
import com.stellaris.stchat.activity.PersonalActivity;
import com.stellaris.stchat.utils.DialogCreator;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ToastUtil;
import com.stellaris.stchat.utils.photochoose.SelectableRoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeFragment extends BaseFragment {


    @BindView(R.id.rl_personal)
    RelativeLayout mRl_personal;
    @BindView(R.id.btn_logout)
    Button mBtn_logout;
    Unbinder unbinder;
    @BindView(R.id.take_photo_iv)
    SelectableRoundedImageView mIv_avatar;
    @BindView(R.id.nickName)
    TextView mTv_nickName;
    @BindView(R.id.signature)
    TextView mTv_sign;
    Bitmap mAvatar;
    private Dialog mDialog;

    public MeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    public void initView() {

    }

    public void initData() {

    }

    public void showAvatar(Bitmap avatarBitmap) {
        if (avatarBitmap != null) {
            mIv_avatar.setImageBitmap(avatarBitmap);
        }else {
            mIv_avatar.setImageResource(R.drawable.avatar_default);
        }

    }

    public void showNickName(UserInfo myInfo) {
        if (!TextUtils.isEmpty(myInfo.getNickname().trim())) {
            mTv_nickName.setText(myInfo.getNickname());
        } else {
            mTv_nickName.setText(myInfo.getUserName());
        }
        mTv_sign.setText(myInfo.getSignature());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.rl_personal, R.id.btn_logout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_personal:
                startActivity(new Intent(getActivity(), PersonalActivity.class));
                break;
            case R.id.btn_logout:
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.jmui_cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.jmui_commit_btn:
                                Logout();
                                cancelNotification();
                                getActivity().finish();
                                mDialog.cancel();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createLogoutDialog(this.getActivity(), listener);
                mDialog.show();
                break;
        }
    }

    @Override
    public void onResume() {
        UserInfo myInfo = JMessageClient.getMyInfo();
        myInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if (i == 0) {
                    showAvatar(bitmap);
                    mAvatar = bitmap;
                }else {
                    showAvatar(null);
                    mAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_default);
                }
            }
        });
        showNickName(myInfo);
        super.onResume();
    }

    //退出登录
    public void Logout() {
        final Intent intent = new Intent();
        UserInfo info = JMessageClient.getMyInfo();
        if (null != info) {
            SharePreferenceManager.setCachedUsername(info.getUserName());
            if (info.getAvatarFile() != null) {
                SharePreferenceManager.setCachedAvatarPath(info.getAvatarFile().getAbsolutePath());
            }
            JMessageClient.logout();
            intent.setClass(this.getActivity(), LoginActivity.class);
            startActivity(intent);
        } else {
            ToastUtil.shortToast(this.getActivity(), "退出失败");
        }
    }

    public void cancelNotification() {
        NotificationManager manager = (NotificationManager) this.getActivity().getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }
}
