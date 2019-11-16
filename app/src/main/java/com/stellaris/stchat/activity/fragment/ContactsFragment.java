package com.stellaris.stchat.activity.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.SearchForAddFriendActivity;
import com.stellaris.stchat.activity.VerificationActivity;
import com.stellaris.stchat.activity.VerificationMessageActivity;
import com.stellaris.stchat.adapter.StickyListAdapter;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.database.FriendEntry;
import com.stellaris.stchat.database.FriendRecommendEntry;
import com.stellaris.stchat.database.GroupApplyEntry;
import com.stellaris.stchat.database.RefuseGroupEntry;
import com.stellaris.stchat.database.UserEntry;
import com.stellaris.stchat.entity.Event;
import com.stellaris.stchat.entity.EventType;
import com.stellaris.stchat.entity.FriendInvitation;
import com.stellaris.stchat.entity.GroupApplyInvitation;
import com.stellaris.stchat.utils.DialogCreator;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ThreadUtil;
import com.stellaris.stchat.utils.pinyin.HanziToPinyin;
import com.stellaris.stchat.utils.pinyin.PinyinComparator;
import com.stellaris.stchat.utils.sidebar.SideBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.jmessage.support.google.gson.Gson;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.GroupApprovalEvent;
import cn.jpush.im.android.api.event.GroupApprovalRefuseEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends BaseFragment {


    @BindView(R.id.ib_goToAddFriend)
    ImageButton mBtn_addFriend;
    @BindView(R.id.listview)
    StickyListHeadersListView mListView;
    @BindView(R.id.sidebar)
    SideBar mSidebar;
    @BindView(R.id.group_dialog)
    TextView mTv_letterHint;
    private List<FriendEntry> mList = new ArrayList<>();
    private TextView mAllContactNumber;
    private List<FriendEntry> forDelete = new ArrayList<>();
    private StickyListAdapter mAdapter;
    private Activity mContext;

    //contact_header
    private LinearLayout mVerify_ll;
    private LinearLayout mGroup_ll;
    private TextView mGroup_verification_num;
    private TextView mNewFriendNum;
    private LayoutInflater mInflater;
    private LinearLayout mSearch_title;

    private ImageView mLoadingIv;
    private LinearLayout mLoadingTv;
    private View mView_line;
    private TextView mTv_friendCount;



    Unbinder unbinder;

    public ContactsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAllContactNumber = getActivity().findViewById(R.id.all_contact_number);
        mContext = this.getActivity();
        mInflater = LayoutInflater.from(getActivity());
        initModule();
        initContacts();
        return view;
    }

    public void initModule(){
        mSidebar.setTextView(mTv_letterHint);
        mSidebar.bringToFront();

        View header = mInflater.inflate(R.layout.contact_list_header, null);
        mVerify_ll = (LinearLayout) header.findViewById(R.id.verify_ll);
        mVerify_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),VerificationMessageActivity.class));
            }
        });
        mGroup_ll = (LinearLayout) header.findViewById(R.id.group_ll);
        mGroup_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ;
            }
        });
        mGroup_verification_num = (TextView) header.findViewById(R.id.group_verification_num);
        mNewFriendNum = (TextView) header.findViewById(R.id.friend_verification_num);
        mSearch_title = (LinearLayout) header.findViewById(R.id.search_title);
        mView_line = header.findViewById(R.id.view_line);
        mGroup_verification_num.setVisibility(INVISIBLE);
        mTv_friendCount = (TextView)header.findViewById(R.id.tv_friend_count);

        mListView.addHeaderView(header, null, false);
        mListView.setDrawingListUnderStickyHeader(true);
        mListView.setAreHeadersSticky(true);
        mListView.setStickyHeaderTopOffset(0);
        if (SharePreferenceManager.getCachedNewFriendNum() > 0) {
            showNewFriends(SharePreferenceManager.getCachedNewFriendNum());
        } else {
            mNewFriendNum.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ib_goToAddFriend, R.id.listview})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ib_goToAddFriend:
                startActivity(new Intent(getActivity(), SearchForAddFriendActivity.class));
                break;
            case R.id.listview:
                break;
        }
    }

    //view
    public void initContacts() {
        final UserEntry user = UserEntry.getUser(JMessageClient.getMyInfo().getUserName(),
                JMessageClient.getMyInfo().getAppKey());
        Dialog dialog = DialogCreator.createLoadingDialog(getActivity(),"加载中");
        dialog.show();
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                dialog.dismiss();
                if (responseCode == 0) {
                    if (userInfoList != null && userInfoList.size() != 0) {
                        //mContactsView.dismissLine();
                        ActiveAndroid.beginTransaction();
                        try {
                            for (UserInfo userInfo : userInfoList) {
                                String displayName = userInfo.getDisplayName();
                                String letter;
                                if (!TextUtils.isEmpty(displayName.trim())) {
                                    ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                                            .get(displayName);
                                    StringBuilder sb = new StringBuilder();
                                    if (tokens != null && tokens.size() > 0) {
                                        for (HanziToPinyin.Token token : tokens) {
                                            if (token.type == HanziToPinyin.Token.PINYIN) {
                                                sb.append(token.target);
                                            } else {
                                                sb.append(token.source);
                                            }
                                        }
                                    }
                                    String sortString = sb.toString().substring(0, 1).toUpperCase();
                                    if (sortString.matches("[A-Z]")) {
                                        letter = sortString.toUpperCase();
                                    } else {
                                        letter = "#";
                                    }
                                } else {
                                    letter = "#";
                                }
                                //避免重复请求时导致数据重复A
                                FriendEntry friend = FriendEntry.getFriend(user,
                                        userInfo.getUserName(), userInfo.getAppKey());
                                if (null == friend) {
                                    if (TextUtils.isEmpty(userInfo.getAvatar())) {
                                        friend = new FriendEntry(userInfo.getUserID(), userInfo.getUserName(), userInfo.getNotename(), userInfo.getNickname(), userInfo.getAppKey(),
                                                null, displayName, letter, user);
                                    } else {
                                        friend = new FriendEntry(userInfo.getUserID(), userInfo.getUserName(), userInfo.getNotename(), userInfo.getNickname(), userInfo.getAppKey(),
                                                userInfo.getAvatarFile().getAbsolutePath(), displayName, letter, user);
                                    }
                                    friend.save();
                                    mList.add(friend);
                                }
                                forDelete.add(friend);
                            }
                            ActiveAndroid.setTransactionSuccessful();
                        } finally {
                            ActiveAndroid.endTransaction();
                        }
                    } else {
                        //mContactsView.showLine();
                    }
                    //其他端删除好友后,登陆时把数据库中的也删掉
                    List<FriendEntry> friends = StApplication.getUserEntry().getFriends();
                    friends.removeAll(forDelete);
                    for (FriendEntry del : friends) {
                        del.delete();
                        mList.remove(del);
                    }
                    Collections.sort(mList, new PinyinComparator());
                    mAdapter = new StickyListAdapter(getActivity(), mList, false);
                    mListView.setAdapter(mAdapter);
                    refreshFriendCount();
                }
            }
        });

    }

    public void showNewFriends(int num) {
        mNewFriendNum.setVisibility(VISIBLE);
        if (num > 99) {
            mNewFriendNum.setText("99+");
        } else {
            mNewFriendNum.setText(String.valueOf(num));
        }
    }

    public void showContact() {
        mSidebar.setVisibility(VISIBLE);
        mListView.setVisibility(VISIBLE);
    }
    public void refresh(FriendEntry entry) {
        mList.add(entry);
        if (null == mAdapter) {
            mAdapter = new StickyListAdapter(getActivity(), mList, false);
        } else {
            Collections.sort(mList, new PinyinComparator());
        }
        mAdapter.notifyDataSetChanged();
        refreshFriendCount();
    }

    public void refreshContact() {
        final UserEntry user = UserEntry.getUser(JMessageClient.getMyInfo().getUserName(),
                JMessageClient.getMyInfo().getAppKey());
        mList = user.getFriends();
        Collections.sort(mList, new PinyinComparator());
        mAdapter = new StickyListAdapter(getActivity(), mList, false);
        mListView.setAdapter(mAdapter);
    }

    public void refreshFriendCount(){
        int count = mList.size();
        mTv_friendCount.setText(String.valueOf(count));
    }

    //
    @Override
    public void onResume() {
        super.onResume();
        showContact();
        refreshContact();
        //如果放到数据库做.能提高效率和网络状态不好的情况,但是不能实时获取在其他终端修改后的搜索匹配.
        //为搜索群组做准备
        StApplication.mGroupInfoList.clear();
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                JMessageClient.getGroupIDList(new GetGroupIDListCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                        if (responseCode == 0) {
                            for (final Long groupID : groupIDList) {
                                JMessageClient.getGroupInfo(groupID, new GetGroupInfoCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                                        if (responseCode == 0) {
                                            StApplication.mGroupInfoList.add(groupInfo);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
        //为搜索好友做准备
        if (StApplication.mFriendInfoList != null)
            StApplication.mFriendInfoList.clear();
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int i, String s, List<UserInfo> list) {
                if (i == 0) {
                    StApplication.mFriendInfoList = list;
                }
            }
        });
    }

    //接收到好友事件
    public void onEvent(ContactNotifyEvent event) {
        final UserEntry user = StApplication.getUserEntry();
        final String reason = event.getReason();
        final String username = event.getFromUsername();
        final String appKey = event.getfromUserAppKey();
        //对方接收了你的好友请求
        if (event.getType() == ContactNotifyEvent.Type.invite_accepted) {
            JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                    if (responseCode == 0) {
                        String name = info.getNickname();
                        if (TextUtils.isEmpty(name)) {
                            name = info.getUserName();
                        }
                        FriendEntry friendEntry = FriendEntry.getFriend(user, username, appKey);
                        if (friendEntry == null) {
                            final FriendEntry newFriend = new FriendEntry(info.getUserID(), username, info.getNotename(), info.getNickname(), appKey, info.getAvatar(), name, getLetter(name), user);
                            newFriend.save();
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh(newFriend);
                                }
                            });
                        }
                    }
                }
            });
            FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
            entry.state = FriendInvitation.ACCEPTED.getValue();
            entry.save();

            Conversation conversation = JMessageClient.getSingleConversation(username);
            if (conversation == null) {
                conversation = Conversation.createSingleConversation(username);
                EventBus.getDefault().post(new Event.Builder()
                        .setType(EventType.createConversation)
                        .setConversation(conversation)
                        .build());
            }

            //拒绝好友请求
        } else if (event.getType() == ContactNotifyEvent.Type.invite_declined) {
            StApplication.forAddFriend.remove(username);
            FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
            entry.state = FriendInvitation.BE_REFUSED.getValue();
            entry.reason = reason;
            entry.save();
            //收到好友邀请
        } else if (event.getType() == ContactNotifyEvent.Type.invite_received) {
            //如果同一个人申请多次,则只会出现一次;当点击进验证消息界面后,同一个人再次申请则可以收到
            if (StApplication.forAddFriend.size() > 0) {
                for (String forAdd : StApplication.forAddFriend) {
                    if (forAdd.equals(username)) {
                        return;
                    } else {
                        StApplication.forAddFriend.add(username);
                    }
                }
            } else {
                StApplication.forAddFriend.add(username);
            }
            JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    if (status == 0) {
                        String name = userInfo.getNickname();
                        if (TextUtils.isEmpty(name)) {
                            name = userInfo.getUserName();
                        }
                        FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
                        if (null == entry) {
                            if (null != userInfo.getAvatar()) {
                                String path = userInfo.getAvatarFile().getPath();
                                entry = new FriendRecommendEntry(userInfo.getUserID(), username, userInfo.getNotename(), userInfo.getNickname(), appKey, path,
                                        name, reason, FriendInvitation.INVITED.getValue(), user, 0);
                            } else {
                                entry = new FriendRecommendEntry(userInfo.getUserID(), username, userInfo.getNotename(), userInfo.getNickname(), appKey, null,
                                        username, reason, FriendInvitation.INVITED.getValue(), user, 0);
                            }
                        } else {
                            entry.state = FriendInvitation.INVITED.getValue();
                            entry.reason = reason;
                        }
                        entry.save();
                        //收到好友请求数字 +1
                        int showNum = SharePreferenceManager.getCachedNewFriendNum() + 1;
                        showNewFriends(showNum);
                        mAllContactNumber.setVisibility(VISIBLE);
                        mAllContactNumber.setText(String.valueOf(showNum));
                        SharePreferenceManager.setCachedNewFriendNum(showNum);
                    }
                }
            });
        } else if (event.getType() == ContactNotifyEvent.Type.contact_deleted) {
            StApplication.forAddFriend.remove(username);
            FriendEntry friendEntry = FriendEntry.getFriend(user, username, appKey);
            friendEntry.delete();
            refreshContact();
        }
    }

    public void onEventMainThread(Event event) {
        if (event.getType() == EventType.addFriend) {
            FriendRecommendEntry recommendEntry = FriendRecommendEntry.getEntry(event.getFriendId());
            if (null != recommendEntry) {
                FriendEntry friendEntry = FriendEntry.getFriend(recommendEntry.user,
                        recommendEntry.username, recommendEntry.appKey);
                if (null == friendEntry) {
                    friendEntry = new FriendEntry(recommendEntry.uid, recommendEntry.username, recommendEntry.noteName, recommendEntry.nickName, recommendEntry.appKey,
                            recommendEntry.avatar, recommendEntry.displayName,
                            getLetter(recommendEntry.displayName), recommendEntry.user);
                    friendEntry.save();
                    refresh(friendEntry);
                }
            }
        }
    }

    //群主收到群组验证事件
    public void onEvent(GroupApprovalEvent event) {
        final UserEntry user = StApplication.getUserEntry();
        GroupApprovalEvent.Type type = event.getType();
        long gid = event.getGid();
        event.getFromUserInfo(new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo fromUserInfo) {
                if (i == 0) {
                    Gson gson = new Gson();
                    event.getApprovalUserInfoList(new GetUserInfoListCallback() {
                        @Override
                        public void gotResult(int i, String s, List<UserInfo> list) {
                            if (i == 0) {
                                if (StApplication.forAddIntoGroup.size() > 0) {
                                    for (String addName : StApplication.forAddIntoGroup) {
                                        if (addName.equals(list.get(0).getUserName())) {
                                            return;
                                        } else {
                                            StApplication.forAddIntoGroup.add(list.get(0).getUserName());
                                        }
                                    }
                                }
                                GroupApplyEntry entry;
                                //邀请,from是邀请方
                                if (type.equals(GroupApprovalEvent.Type.invited_into_group)) {
                                    entry = GroupApplyEntry.getEntry(user, list.get(0).getUserName(), list.get(0).getAppKey());
                                    if (entry != null) {
                                        entry.delete();
                                    }
                                    if (fromUserInfo.getAvatar() != null) {
                                        entry = new GroupApplyEntry(fromUserInfo.getUserName(), list.get(0).getUserName(), fromUserInfo.getAppKey(),
                                                list.get(0).getAvatarFile().getPath(), fromUserInfo.getDisplayName(), list.get(0).getDisplayName(),
                                                null, GroupApplyInvitation.INVITED.getValue(), gson.toJson(event), gid + "",
                                                user, 0, 0);//邀请type=0
                                    } else {
                                        entry = new GroupApplyEntry(fromUserInfo.getUserName(), list.get(0).getUserName(), fromUserInfo.getAppKey(),
                                                null, fromUserInfo.getDisplayName(), list.get(0).getDisplayName(),
                                                null, GroupApplyInvitation.INVITED.getValue(), gson.toJson(event), gid + "",
                                                user, 0, 0);//邀请type=0
                                    }
                                } else {
                                    entry = GroupApplyEntry.getEntry(user, fromUserInfo.getUserName(), fromUserInfo.getAppKey());
                                    if (entry != null) {
                                        entry.delete();
                                    }
                                    if (fromUserInfo.getAvatar() != null) {
                                        entry = new GroupApplyEntry(list.get(0).getUserName(), list.get(0).getUserName(), list.get(0).getAppKey(),
                                                list.get(0).getAvatarFile().getPath(), list.get(0).getDisplayName(), list.get(0).getDisplayName(),
                                                event.getReason(), GroupApplyInvitation.INVITED.getValue(), gson.toJson(event), gid + "",
                                                user, 0, 1);//申请type=1
                                    } else {
                                        entry = new GroupApplyEntry(list.get(0).getUserName(), list.get(0).getUserName(), list.get(0).getAppKey(),
                                                null, fromUserInfo.getDisplayName(), list.get(0).getDisplayName(),
                                                event.getReason(), GroupApplyInvitation.INVITED.getValue(), gson.toJson(event), gid + "",
                                                user, 0, 1);//申请type=1
                                    }
                                }
                                entry.save();

                                int showNum = SharePreferenceManager.getCachedNewFriendNum() + 1;
                                showNewFriends(showNum);
                                mAllContactNumber.setVisibility(VISIBLE);
                                mAllContactNumber.setText(String.valueOf(showNum));

                                SharePreferenceManager.setCachedNewFriendNum(showNum);
                            }
                        }
                    });

                }
            }
        });
    }

    //收到被拒绝事件
    public void onEvent(GroupApprovalRefuseEvent event) {
        final UserEntry user = StApplication.getUserEntry();
        long gid = event.getGid();
        event.getToUserInfoList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int i, String s, List<UserInfo> list) {
                if (i == 0) {
                    String userName = list.get(0).getUserName();
                    String displayName = list.get(0).getDisplayName();
                    String appKey = list.get(0).getAppKey();
                    String path = null;
                    if (list.get(0).getAvatar() != null) {
                        path = list.get(0).getAvatarFile().getPath();
                    }
                    RefuseGroupEntry groupEntry = RefuseGroupEntry.getEntry(user, userName, appKey);
                    if (groupEntry != null) {
                        groupEntry.delete();
                    }
                    groupEntry = new RefuseGroupEntry(user, userName, displayName, gid + "", appKey, path);
                    groupEntry.save();

                }
            }
        });

    }

    private String getLetter(String name) {
        String letter;
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                .get(name);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (token.type == HanziToPinyin.Token.PINYIN) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        String sortString = sb.toString().substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            letter = sortString.toUpperCase();
        } else {
            letter = "#";
        }
        return letter;
    }
}
