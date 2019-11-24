package com.stellaris.stchat.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.model.InfoModel;
import com.stellaris.stchat.utils.DialogCreator;
import com.stellaris.stchat.utils.ToastUtil;
import com.stellaris.stchat.utils.photochoose.SelectableRoundedImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;

public class SearchForAddFriendActivity extends BaseActivity {

    @BindView(R.id.et_searchUser)
    EditText mEt_searchUser;
    @BindView(R.id.btn_search)
    Button mBtn_search;
    @BindView(R.id.search_header)
    SelectableRoundedImageView mSearch_header;
    @BindView(R.id.search_name)
    TextView mSearch_name;
    @BindView(R.id.search_addBtn)
    Button mBtn_add;
    @BindView(R.id.search_result)
    LinearLayout mSearch_result;
    @BindView(R.id.no_result)
    LinearLayout mNoResult;

    private UserInfo mMyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_add_friend);
        ButterKnife.bind(this);
        initView();
    }

    public void initView() {
        mEt_searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNoResult.setVisibility(View.GONE);
                boolean feedback = mEt_searchUser.getText().length() > 0;
                if (feedback) {
                    mBtn_search.setEnabled(true);
                } else {
                    mBtn_search.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mMyInfo = JMessageClient.getMyInfo();
        initTitle(true,false,"","搜索",false,"");
    }

    @OnClick({R.id.btn_search, R.id.search_addBtn, R.id.search_result})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                hintKbTwo();
                String searchUserName = mEt_searchUser.getText().toString();
                if (!TextUtils.isEmpty(searchUserName)) {
                    Dialog dialog = DialogCreator.createLoadingDialog(this, "查询中");
                    dialog.show();
                    JMessageClient.getUserInfo(searchUserName, new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                            dialog.dismiss();
                            if (responseCode == 0) {
                                InfoModel.getInstance().friendInfo = info;
                                mSearch_result.setVisibility(View.VISIBLE);
                                mBtn_add.setText("加好友");
                                mBtn_add.setEnabled(true);
                                //已经是好友则不显示"加好友"按钮
                                if (info.isFriend()) {
                                    mBtn_add.setVisibility(View.VISIBLE);
                                    mBtn_add.setText("已是好友");
                                    mBtn_add.setEnabled(false);
                                    //如果是发起单聊.那么不能显示加好友按钮
                                } else if (getIntent().getFlags() == 2) {
                                    mBtn_add.setVisibility(View.GONE);
                                }else if (mMyInfo.getUserID() == info.getUserID()){
                                    //如果是自己 则不能点击添加
                                    mBtn_add.setVisibility(View.VISIBLE);
                                    mBtn_add.setText("我自己");
                                    mBtn_add.setEnabled(false);
                                }
                                //这个接口会在本地寻找头像文件,不存在就异步拉取
                                File avatarFile = info.getAvatarFile();
                                if (avatarFile != null) {
                                    mSearch_header.setImageBitmap(BitmapFactory.decodeFile(avatarFile.getAbsolutePath()));
                                    InfoModel.getInstance().setBitmap(BitmapFactory.decodeFile(avatarFile.getAbsolutePath()));
                                } else {
                                    mSearch_header.setImageResource(R.drawable.avatar_default);
                                    InfoModel.getInstance().setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.avatar_default));
                                }
                                mSearch_name.setText(TextUtils.isEmpty(info.getNickname()) ? info.getUserName() : info.getNickname());
                            } else {
                                mNoResult.setVisibility(View.VISIBLE);
                                mSearch_result.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                break;
            case R.id.search_addBtn:
                goToActivity(this,VerificationActivity.class);
                break;
            case R.id.search_result:
                break;
        }
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
