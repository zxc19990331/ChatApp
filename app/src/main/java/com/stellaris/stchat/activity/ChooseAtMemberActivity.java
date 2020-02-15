package com.stellaris.stchat.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.adapter.AtMemberAdapter;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.model.ParentLinkedHolder;
import com.stellaris.stchat.utils.pinyin.UserComparator;
import com.stellaris.stchat.utils.sidebar.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ChooseAtMemberActivity extends BaseActivity {
    private List<UserInfo> mList;
    private SideBar mSideBar;
    private TextView mLetterHintTv;
    private AtMemberAdapter mAdapter;
    private StickyListHeadersListView mListView;
    private LinearLayout mLl_groupAll;
    private LinearLayout mSearch_title;
    private static ParentLinkedHolder<EditText> textParentLinkedHolder;
    private List<UserInfo> forDel = new ArrayList<>();

    public static void show(ChatActivity textWatcher, EditText editText, String targetId) {
        synchronized (ChooseAtMemberActivity.class) {
            ParentLinkedHolder<EditText> holder = new ParentLinkedHolder<>(editText);
            textParentLinkedHolder = holder.addParent(textParentLinkedHolder);
        }

        Intent intent = new Intent(textWatcher, ChooseAtMemberActivity.class);
        intent.putExtra(StApplication.GROUP_ID, Long.parseLong(targetId));
        textWatcher.startActivityForResult(intent, StApplication
                .REQUEST_CODE_AT_MEMBER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_at_member);
        mListView = (StickyListHeadersListView) findViewById(R.id.at_member_list_view);
        mLl_groupAll = (LinearLayout) findViewById(R.id.ll_groupAll);
        mSearch_title = (LinearLayout) findViewById(R.id.search_title);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);

        initTitle(true, true, "选择成员", "", false, "");

        long groupId = getIntent().getLongExtra(StApplication.GROUP_ID, 0);
        if (0 != groupId) {
            Conversation conv = JMessageClient.getGroupConversation(groupId);
            GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
            mList = groupInfo.getGroupMembers();
            for (UserInfo info : mList) {
                if (info.getUserName().equals(JMessageClient.getMyInfo().getUserName())) {
                    forDel.clear();
                    forDel.add(info);
                }
            }
            mList.removeAll(forDel);
            Collections.sort(mList, new UserComparator());

            mAdapter = new AtMemberAdapter(this, mList);
            mListView.setAdapter(mAdapter);
        }

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getSectionForLetter(s);
                if (position != -1 && position < mAdapter.getCount()) {
                    mListView.setSelection(position - 1);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                UserInfo userInfo = mList.get(position);
                Intent intent = new Intent();
                String atName = userInfo.getDisplayName();

                synchronized (ChooseAtMemberActivity.class) {
                    if (textParentLinkedHolder != null) {
                        EditText editText = textParentLinkedHolder.item;
                        if (editText != null)
                            intent.putExtra(StApplication.NAME, atName);
                    }
                }

                intent.putExtra(StApplication.TARGET_ID, userInfo.getUserName());
                intent.putExtra(StApplication.TARGET_APP_KEY, userInfo.getAppKey());
                setResult(StApplication.RESULT_CODE_AT_MEMBER, intent);
                finish();
            }
        });

        mLl_groupAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //@all
                Intent intent = new Intent();
                intent.putExtra(StApplication.ATALL, true);
                setResult(StApplication.RESULT_CODE_AT_ALL, intent);
                finish();
            }
        });

        mSearch_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ChooseAtMemberActivity.this, SearchAtMemberActivity.class);
                //StApplication.mSearchAtMember = mList;
                //startActivityForResult(intent, StApplication.SEARCH_AT_MEMBER_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && data != null) {
            Intent intent = new Intent();
            intent.putExtra(StApplication.NAME, data.getStringExtra(StApplication.SEARCH_AT_MEMBER_NAME));
            intent.putExtra(StApplication.TARGET_ID, data.getStringExtra(StApplication.SEARCH_AT_MEMBER_USERNAME));
            intent.putExtra(StApplication.TARGET_APP_KEY, data.getStringExtra(StApplication.SEARCH_AT_APPKEY));
            setResult(StApplication.RESULT_CODE_AT_MEMBER, intent);
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (ChooseAtMemberActivity.class) {
            if (textParentLinkedHolder != null) {
                textParentLinkedHolder = textParentLinkedHolder.putParent();
            }
        }
    }
}
