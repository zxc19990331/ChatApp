package com.stellaris.stchat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.sj.emoji.EmojiBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.event.ChatRoomMessageEvent;
import cn.jpush.im.android.api.event.ChatRoomNotificationEvent;
import cn.jpush.im.android.api.event.CommandNotificationEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent;
import cn.jpush.im.android.api.event.MessageRetractEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;
import com.stellaris.stchat.R;
import com.stellaris.stchat.adapter.ChattingListAdapter;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.entity.Event;
import com.stellaris.stchat.entity.EventType;
import com.stellaris.stchat.model.Constants;
import com.stellaris.stchat.pickerimage.PickImageActivity;
import com.stellaris.stchat.pickerimage.utils.Extras;
import com.stellaris.stchat.pickerimage.utils.RequestCode;
import com.stellaris.stchat.pickerimage.utils.SendImageHelper;
import com.stellaris.stchat.pickerimage.utils.StorageType;
import com.stellaris.stchat.pickerimage.utils.StorageUtil;
import com.stellaris.stchat.pickerimage.utils.StringUtil;
import com.stellaris.stchat.utils.CommonUtils;
import com.stellaris.stchat.utils.IdHelper;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ToastUtil;
import com.stellaris.stchat.utils.event.ImageEvent;
import com.stellaris.stchat.utils.imagepicker.bean.ImageItem;


import com.stellaris.stchat.view.ChatView;

import com.stellaris.stchat.view.listview.DropDownListView;


/**
 * Created by ${chenyn} on 2017/3/26.
 */

public class ChatActivity extends BaseActivity implements  View.OnClickListener {

    DropDownListView lvChat;

    EditText etInput;

    public static final String JPG = ".jpg";
    private static String MsgIDs = "msgIDs";

    private String mTitle;
    private boolean mLongClick = false;

    private static final String MEMBERS_COUNT = "membersCount";
    private static final String GROUP_NAME = "groupName";

    public static final String TARGET_ID = "targetId";
    public static final String TARGET_APP_KEY = "targetAppKey";
    private static final String DRAFT = "draft";
    private ArrayList<ImageItem> selImageList; //当前选择的所有图片
    public static final int REQUEST_CODE_SELECT = 100;
    private ChatView mChatView;
    private boolean mIsSingle = true;
    private Conversation mConv;
    private String mTargetId;
    private String mTargetAppKey;
    private Activity mContext;
    private ChattingListAdapter mChatAdapter;
    int maxImgCount = 9;
    private List<UserInfo> mAtList;
    private long mGroupId;
    private static final int REFRESH_LAST_PAGE = 0x1023;
    private static final int REFRESH_CHAT_TITLE = 0x1024;
    private static final int REFRESH_GROUP_NAME = 0x1025;
    private static final int REFRESH_GROUP_NUM = 0x1026;
    private Dialog mDialog;

    private GroupInfo mGroupInfo;
    private UserInfo mMyInfo;
    private static final String GROUP_ID = "groupId";
    private int mAtMsgId;
    private int mAtAllMsgId;
    private int mUnreadMsgCnt;
    private boolean mShowSoftInput = false;
    private List<UserInfo> forDel = new ArrayList<>();

    Window mWindow;
    InputMethodManager mImm;
    private final UIHandler mUIHandler = new UIHandler(this);
    private boolean mAtAll = false;
    private boolean isChatRoom = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mContext = this;

        setContentView(R.layout.activity_chat);
        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.initModule(mDensity, mDensityDpi);

        this.mWindow = getWindow();
        this.mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mChatView.setListeners(this);

        ButterKnife.bind(this);

        initView();
        initData();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initData() {
//        SimpleCommonUtils.initEmoticonsEditText(ekBar.getEtChat());
        Intent intent = getIntent();
        mTargetId = intent.getStringExtra(TARGET_ID);
        mTargetAppKey = intent.getStringExtra(TARGET_APP_KEY);
        mTitle = intent.getStringExtra(StApplication.CONV_TITLE);
        mMyInfo = JMessageClient.getMyInfo();
        initInput();
        if (!TextUtils.isEmpty(mTargetId)) {
            //单聊
            mIsSingle = true;
            mChatView.setChatTitle(mTitle);
            mConv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
            if (mConv == null) {
                mConv = Conversation.createSingleConversation(mTargetId, mTargetAppKey);
            }
            mChatAdapter = new ChattingListAdapter(mContext, mConv, longClickListener);
        } else {
            //群聊
            mIsSingle = false;
            mGroupId = intent.getLongExtra(GROUP_ID, 0);
            mTargetId = String.valueOf(mGroupId);
            final boolean fromGroup = intent.getBooleanExtra("fromGroup", false);
            if (fromGroup) {
                mChatView.setChatTitle(mTitle, intent.getIntExtra(MEMBERS_COUNT, 0));
                mConv = JMessageClient.getGroupConversation(mGroupId);
                mChatAdapter = new ChattingListAdapter(mContext, mConv, longClickListener);//长按聊天内容监听
            } else {
                mAtMsgId = intent.getIntExtra("atMsgId", -1);
                mAtAllMsgId = intent.getIntExtra("atAllMsgId", -1);
                mConv = JMessageClient.getGroupConversation(mGroupId);
                if (mConv != null) {
                    GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
                    UserInfo userInfo = groupInfo.getGroupMemberInfo(mMyInfo.getUserName(), mMyInfo.getAppKey());
                    //如果自己在群聊中，聊天标题显示群人数
                    if (userInfo != null) {
                        if (!TextUtils.isEmpty(groupInfo.getGroupName())) {
                            mChatView.setChatTitle(mTitle, groupInfo.getGroupMembers().size());
                        } else {
                            mChatView.setChatTitle(mTitle, groupInfo.getGroupMembers().size());
                        }
                        mChatView.showRightBtn();
                    } else {
                        if (!TextUtils.isEmpty(mTitle)) {
                            mChatView.setChatTitle(mTitle);
                        } else {
                            mChatView.setChatTitle(R.string.group);
                        }
                        mChatView.dismissRightBtn();
                    }
                } else {
                    mConv = Conversation.createGroupConversation(mGroupId);
                }
                //更新群名
                JMessageClient.getGroupInfo(mGroupId, new GetGroupInfoCallback(false) {
                    @Override
                    public void gotResult(int status, String desc, GroupInfo groupInfo) {
                        if (status == 0) {
                            mGroupInfo = groupInfo;
                            mUIHandler.sendEmptyMessage(REFRESH_CHAT_TITLE);
                        }
                    }
                });
                if (mAtMsgId != -1) {
                    mUnreadMsgCnt = mConv.getUnReadMsgCnt();
                    // 如果 @我 的消息位于屏幕显示的消息之上，显示 有人@我 的按钮
                    if (mAtMsgId + 8 <= mConv.getLatestMessage().getId()) {
                        mChatView.showAtMeButton();
                    }
                    mChatAdapter = new ChattingListAdapter(mContext, mConv, longClickListener, mAtMsgId);
                } else {
                    mChatAdapter = new ChattingListAdapter(mContext, mConv, longClickListener);
                }

            }
            //聊天信息标志改变
            mChatView.setGroupIcon();
        }

        String draft = intent.getStringExtra(DRAFT);
        if (draft != null && !TextUtils.isEmpty(draft)) {
            etInput.setText(draft);
        }

        mChatView.setChatListAdapter(mChatAdapter);
//        mChatAdapter.initMediaPlayer();
        mChatView.getListView().setOnDropDownListener(new DropDownListView.OnDropDownListener() {
            @Override
            public void onDropDown() {
                mUIHandler.sendEmptyMessageDelayed(REFRESH_LAST_PAGE, 1000);
            }
        });
        mChatView.setToBottom();
        mChatView.setConversation(mConv);
    }

    private void initView() {
        lvChat = (DropDownListView) findViewById(R.id.lv_chat);
        etInput = mChatView.getEditInput();
        initListView();

        etInput.addTextChangedListener(new TextWatcher() {
            private CharSequence temp = "";

            @Override
            public void afterTextChanged(Editable arg0) {
                if (temp.length() > 0) {
                    mLongClick = false;
                }

                if (mAtList != null && mAtList.size() > 0) {
                    for (UserInfo info : mAtList) {
                        String name = info.getDisplayName();

                        if (!arg0.toString().contains("@" + name + " ")) {
                            forDel.add(info);
                        }
                    }
                    mAtList.removeAll(forDel);
                }

                if (!arg0.toString().contains("@所有成员 ")) {
                    mAtAll = false;
                }

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
                if (s.length() > 0 && after >= 1 && s.subSequence(start, start + 1).charAt(0) == '@' && !mLongClick) {
                    if (null != mConv && mConv.getType() == ConversationType.group) {
//                        TODO:At功能
                        //ChooseAtMemberActivity.show(ChatActivity.this, etInput, mConv.getTargetId());
                    }
                }
            }
        });

        etInput.setOnFocusChangeListener((v, hasFocus) -> {
            String content;
            if (hasFocus) {
                content = "{\"type\": \"input\",\"content\": {\"message\": \"对方正在输入\"}}";
            } else {
                content = "{\"type\": \"input\",\"content\": {\"message\": \"\"}}";
            }
            if (mIsSingle) {
                JMessageClient.sendSingleTransCommand(mTargetId, null, content, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {

                    }
                });
            }
        });

        mChatView.getChatListView().setOnTouchListener((v, event) -> {
            mChatView.getChatListView().setFocusable(true);
            mChatView.getChatListView().setFocusableInTouchMode(true);
            mChatView.getChatListView().requestFocus();
            CommonUtils.hideKeyboard(mContext);
            return false;
        });
    }

    public void initInput(){
        mChatView.getBtnSend().setOnClickListener(v -> {
            String msgcontent = etInput.getText().toString();
            scrollToBottom();
            if(msgcontent.equals(""))
                return;
            Message msg;
            TextContent content = new TextContent(msgcontent);
            if (mAtAll) {
                msg = mConv.createSendMessageAtAllMember(content, null);
                mAtAll = false;
            } else if (null != mAtList) {
                msg = mConv.createSendMessage(content, mAtList, null);
            } else {
                Log.d("ChatActivity", "create send message conversation = " + mConv + "==content==" + content.toString());
                msg = mConv.createSendMessage(content);
            }

            if (!isChatRoom) {
                //设置需要已读回执
                MessageSendingOptions options = new MessageSendingOptions();
                options.setNeedReadReceipt(true);
                JMessageClient.sendMessage(msg, options);
                mChatAdapter.addMsgFromReceiptToList(msg);
                etInput.setText("");
                if (mAtList != null) {
                    mAtList.clear();
                }
                if (forDel != null) {
                    forDel.clear();
                }
            } else {
                JMessageClient.sendMessage(msg);
                mChatAdapter.addMsgToList(msg);
                etInput.setText("");
            }
        });
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.jmui_return_btn:
                returnBtn();
                break;
            case R.id.jmui_right_btn:
                startChatDetailActivity(mTargetId, mTargetAppKey, mGroupId);
                break;
            case R.id.jmui_at_me_btn:
                if (mUnreadMsgCnt < ChattingListAdapter.PAGE_MESSAGE_COUNT) {
                    int position = ChattingListAdapter.PAGE_MESSAGE_COUNT + mAtMsgId - mConv.getLatestMessage().getId();
                    mChatView.setToPosition(position);
                } else {
                    mChatView.setToPosition(mAtMsgId + mUnreadMsgCnt - mConv.getLatestMessage().getId());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        returnBtn();
    }

    private void returnBtn() {
        mConv.resetUnreadCount();
        dismissSoftInput();
        if (mChatAdapter != null) {
            mChatAdapter.stopMediaPlayer();
        }
        JMessageClient.exitConversation();
        //发送保存为草稿事件到会话列表
        EventBus.getDefault().post(new Event.Builder().setType(EventType.draft)
                .setConversation(mConv)
                .setDraft(etInput.getText().toString())
                .build());
        StApplication.delConversation = null;
        if (mConv.getAllMessage() == null || mConv.getAllMessage().size() == 0) {
            if (mIsSingle) {
                JMessageClient.deleteSingleConversation(mTargetId);
            } else {
                JMessageClient.deleteGroupConversation(mGroupId);
            }
            StApplication.delConversation = mConv;
        }
        if (isChatRoom) {
            ChatRoomManager.leaveChatRoom(Long.valueOf(mTargetId), new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    ChatActivity.this.finish();
                    ChatActivity.super.onBackPressed();
                }
            });
        } else {
            finish();
            super.onBackPressed();
        }
    }

    private void dismissSoftInput() {
        if (mShowSoftInput) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
                mShowSoftInput = false;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startChatDetailActivity(String targetId, String appKey, long groupId) {
        Intent intent = new Intent();
        intent.putExtra(TARGET_ID, targetId);
        intent.putExtra(TARGET_APP_KEY, appKey);
        intent.putExtra(GROUP_ID, groupId);
        intent.setClass(this, ChatDetailActivity.class);
        startActivityForResult(intent, StApplication.REQUEST_CODE_CHAT_DETAIL);
    }

    private ChattingListAdapter.ContentLongClickListener longClickListener = new ChattingListAdapter.ContentLongClickListener() {
        @Override
        public void onContentLongClick(final int position, View view) {
            ;
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (EmoticonsKeyboardUtils.isFullScreen(this)) {
//            boolean isConsum = ekBar.dispatchKeyEventInFullScreen(event);
//            return isConsum ? isConsum : super.dispatchKeyEvent(event);
//        }
        return super.dispatchKeyEvent(event);
    }

    private void initListView() {
        lvChat.setAdapter(mChatAdapter);
        lvChat.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_FLING:
                        break;
                    case SCROLL_STATE_IDLE:
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        //ekBar.reset();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }


    private void scrollToBottom() {
        lvChat.requestLayout();
        lvChat.post(new Runnable() {
            @Override
            public void run() {
                lvChat.setSelection(lvChat.getBottom());
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        JMessageClient.exitConversation();
        //ekBar.reset();
    }

    @Override
    protected void onResume() {
        String targetId = getIntent().getStringExtra(TARGET_ID);
        if (mIsSingle) {
            if (null != targetId) {
                String appKey = getIntent().getStringExtra(TARGET_APP_KEY);
                JMessageClient.enterSingleConversation(targetId, appKey);
            }
        } else if (!isChatRoom) {
            long groupId = getIntent().getLongExtra(GROUP_ID, 0);
            if (groupId != 0) {
                StApplication.isAtMe.put(groupId, false);
                StApplication.isAtall.put(groupId, false);
                JMessageClient.enterGroupConversation(groupId);
            }
        }

        //历史消息中删除后返回到聊天界面刷新界面
        if (StApplication.ids != null && StApplication.ids.size() > 0) {
            for (Message msg : StApplication.ids) {
                mChatAdapter.removeMessage(msg);
            }
        }
        if (mChatAdapter != null)
            mChatAdapter.notifyDataSetChanged();
        //发送名片返回聊天界面刷新信息
        if (SharePreferenceManager.getIsOpen()) {
            if (!isChatRoom) {
                initData();
            }
            SharePreferenceManager.setIsOpen(false);
        }
        super.onResume();

    }

    public void onEvent(CommandNotificationEvent event) {
        if (event.getType().equals(CommandNotificationEvent.Type.single)) {
            String msg = event.getMsg();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(msg);
                        JSONObject jsonContent = object.getJSONObject("content");
                        String messageString = jsonContent.getString("message");
                        if (TextUtils.isEmpty(messageString)) {
                            mChatView.setTitle(mConv.getTitle());
                        } else {
                            mChatView.setTitle(messageString);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void onEventMainThread(ChatRoomMessageEvent event) {
        List<Message> messages = event.getMessages();
        mChatAdapter.addMsgListToList(messages);
    }

    public void onEventMainThread(ChatRoomNotificationEvent event) {
        try {
            Constructor constructor =  EventNotificationContent.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            List<Message> messages = new ArrayList<>();
            switch (event.getType()) {
                case add_chatroom_admin:
                case del_chatroom_admin:
                    event.getTargetUserInfoList(new GetUserInfoListCallback() {
                        @Override
                        public void gotResult(int i, String s, List<UserInfo> list) {
                            if (i == 0) {
                                for (UserInfo userInfo : list) {
                                    try {
                                        EventNotificationContent content = (EventNotificationContent) constructor.newInstance();
                                        Field field = content.getClass().getSuperclass().getDeclaredField("contentType");
                                        field.setAccessible(true);
                                        field.set(content, ContentType.eventNotification);
                                        String user = userInfo.getUserID() == JMessageClient.getMyInfo().getUserID()
                                                ? "你" : TextUtils.isEmpty(userInfo.getNickname()) ? userInfo.getUserName() : userInfo.getNickname();
                                        String result = event.getType() == ChatRoomNotificationEvent.Type.add_chatroom_admin ? "被设置成管理员" : "被取消管理员";
                                        content.setStringExtra("msg", user + result);
                                        if (mConv != null) {
                                            messages.add(mConv.createSendMessage(content));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (messages.size() > 0) {
                                    mChatAdapter.addMsgListToList(messages);
                                }
                            }
                        }
                    });
                    break;
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEvent(MessageEvent event) {
        final Message message = event.getMessage();

        //若为群聊相关事件，如添加、删除群成员
        if (message.getContentType() == ContentType.eventNotification) {
            GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
            long groupId = groupInfo.getGroupID();
            EventNotificationContent.EventNotificationType type = ((EventNotificationContent) message
                    .getContent()).getEventNotificationType();
            if (groupId == mGroupId) {
                switch (type) {
                    case group_member_added:
                        //添加群成员事件
                        List<String> userNames = ((EventNotificationContent) message.getContent()).getUserNames();
                        //群主把当前用户添加到群聊，则显示聊天详情按钮
                        refreshGroupNum();
                        if (userNames.contains(mMyInfo.getNickname()) || userNames.contains(mMyInfo.getUserName())) {
                            runOnUiThread(() -> mChatView.showRightBtn());
                        }

                        break;
                    case group_member_removed:
                        //删除群成员事件
                        userNames = ((EventNotificationContent) message.getContent()).getUserNames();
                        UserInfo operator = ((EventNotificationContent) message.getContent()).getOperatorUserInfo();
                        //群主删除了当前用户，则隐藏聊天详情按钮
                        if ((userNames.contains(mMyInfo.getNickname()) || userNames.contains(mMyInfo.getUserName())) && operator.getUserID() != mMyInfo.getUserID()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mChatView.dismissRightBtn();
                                    GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
                                    if (TextUtils.isEmpty(groupInfo.getGroupName())) {
                                        mChatView.setChatTitle(R.string.group);
                                    } else {
                                        mChatView.setChatTitle(groupInfo.getGroupName());
                                    }
                                    mChatView.dismissGroupNum();
                                }
                            });
                        } else {
                            refreshGroupNum();
                        }

                        break;
                    case group_member_exit:
                        EventNotificationContent content = (EventNotificationContent) message.getContent();
                        if (content.getUserNames().contains(JMessageClient.getMyInfo().getUserName())) {
                            mChatAdapter.notifyDataSetChanged();
                        } else {
                            refreshGroupNum();
                        }
                        break;
                }
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.getTargetType() == ConversationType.single) {
                    UserInfo userInfo = (UserInfo) message.getTargetInfo();
                    String targetId = userInfo.getUserName();
                    String appKey = userInfo.getAppKey();
                    if (mIsSingle && targetId.equals(mTargetId) && appKey.equals(mTargetAppKey)) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        if (lastMsg == null || message.getId() != lastMsg.getId()) {
                            mChatAdapter.addMsgToList(message);
                        } else {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    long groupId = ((GroupInfo) message.getTargetInfo()).getGroupID();
                    if (groupId == mGroupId) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        if (lastMsg == null || message.getId() != lastMsg.getId()) {
                            mChatAdapter.addMsgToList(message);
                        } else {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    public void onEventMainThread(MessageRetractEvent event) {
        Message retractedMessage = event.getRetractedMessage();
        mChatAdapter.delMsgRetract(retractedMessage);
    }

    /**
     * 当在聊天界面断网再次连接时收离线事件刷新
     */
    public void onEvent(OfflineMessageEvent event) {
        Conversation conv = event.getConversation();
        if (conv.getType().equals(ConversationType.single)) {
            UserInfo userInfo = (UserInfo) conv.getTargetInfo();
            String targetId = userInfo.getUserName();
            String appKey = userInfo.getAppKey();
            if (mIsSingle && targetId.equals(mTargetId) && appKey.equals(mTargetAppKey)) {
                List<Message> singleOfflineMsgList = event.getOfflineMessageList();
                if (singleOfflineMsgList != null && singleOfflineMsgList.size() > 0) {
                    mChatView.setToBottom();
                    mChatAdapter.addMsgListToList(singleOfflineMsgList);
                }
            }
        } else {
            long groupId = ((GroupInfo) conv.getTargetInfo()).getGroupID();
            if (groupId == mGroupId) {
                List<Message> offlineMessageList = event.getOfflineMessageList();
                if (offlineMessageList != null && offlineMessageList.size() > 0) {
                    mChatView.setToBottom();
                    mChatAdapter.addMsgListToList(offlineMessageList);
                }
            }
        }
    }

    private void refreshGroupNum() {
        Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        if (!TextUtils.isEmpty(groupInfo.getGroupName())) {
            android.os.Message handleMessage = mUIHandler.obtainMessage();
            handleMessage.what = REFRESH_GROUP_NAME;
            Bundle bundle = new Bundle();
            bundle.putString(GROUP_NAME, groupInfo.getGroupName());
            bundle.putInt(MEMBERS_COUNT, groupInfo.getGroupMembers().size());
            handleMessage.setData(bundle);
            handleMessage.sendToTarget();
        } else {
            android.os.Message handleMessage = mUIHandler.obtainMessage();
            handleMessage.what = REFRESH_GROUP_NUM;
            Bundle bundle = new Bundle();
            bundle.putInt(MEMBERS_COUNT, groupInfo.getGroupMembers().size());
            handleMessage.setData(bundle);
            handleMessage.sendToTarget();
        }
    }

    /**
     * 消息已读事件
     */
    public void onEventMainThread(MessageReceiptStatusChangeEvent event) {
        List<MessageReceiptStatusChangeEvent.MessageReceiptMeta> messageReceiptMetas = event.getMessageReceiptMetas();
        for (MessageReceiptStatusChangeEvent.MessageReceiptMeta meta : messageReceiptMetas) {
            long serverMsgId = meta.getServerMsgId();
            int unReceiptCnt = meta.getUnReceiptCnt();
            mChatAdapter.setUpdateReceiptCount(serverMsgId, unReceiptCnt);
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ImageEvent event) {
        Intent intent;
        switch (event.getFlag()) {
            case StApplication.IMAGE_MESSAGE:
                int from = PickImageActivity.FROM_LOCAL;
                int requestCode = RequestCode.PICK_IMAGE;
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请在应用管理中打开“读写存储”访问权限！", Toast.LENGTH_LONG).show();
                } else {
                    PickImageActivity.start(ChatActivity.this, requestCode, from, tempFile(), true, 9,
                            true, false, 0, 0);
                }
                break;
            default:
                break;
        }

    }

    private String tempFile() {
        String filename = StringUtil.get32UUID() + JPG;
        return StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.PICK_IMAGE://4
                onPickImageActivityResult(requestCode, data);
                break;
            case StApplication.REQUEST_CODE_FRIEND_LIST:
                // 发送名片成功后，聊天室需要添加消息
                if (resultCode == RESULT_OK && isChatRoom) {
                    String msgJson = data.getStringExtra(StApplication.MSG_JSON);
                    if (msgJson != null) {
                        Message msg = Message.fromJson(msgJson);
                        if (msg != null) {
                            mChatAdapter.addMsgToList(msg);
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;

        }

//        处理返回该Activity的消息
        switch (resultCode) {
            case StApplication.RESULT_CODE_AT_MEMBER:
                if (!mIsSingle) {
                    GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
                    String username = data.getStringExtra(StApplication.TARGET_ID);
                    String appKey = data.getStringExtra(StApplication.TARGET_APP_KEY);
                    UserInfo userInfo = groupInfo.getGroupMemberInfo(username, appKey);
                    if (null == mAtList) {
                        mAtList = new ArrayList<UserInfo>();
                    }
                    mAtList.add(userInfo);
                    mLongClick = true;
                    etInput.setText(etInput.getText().toString() + data.getStringExtra(StApplication.NAME));
                    etInput.setSelection(etInput.getText().length());
                }
                break;
            case StApplication.RESULT_CODE_AT_ALL:
                mAtAll = data.getBooleanExtra(StApplication.ATALL, false);
                mLongClick = true;
                if (mAtAll) {
                    etInput.setText(etInput.getText().toString() + "所有成员 ");
                    etInput.setSelection(etInput.getText().length());
                }
                break;
            case RequestCode.TAKE_PHOTO:
                if (data != null) {
                    String name = data.getStringExtra("take_photo");
                    Bitmap bitmap = BitmapFactory.decodeFile(name);
                    ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                            if (responseCode == 0) {
                                Message msg = mConv.createSendMessage(imageContent);
                                handleSendMsg(msg);
                            }
                        }
                    });
                }
                break;
            case RequestCode.TAKE_VIDEO:
                if (data != null) {
                    String path = data.getStringExtra("video");
                    try {
                        FileContent fileContent = new FileContent(new File(path));
                        fileContent.setStringExtra("video", "mp4");
                        Message msg = mConv.createSendMessage(fileContent);
                        handleSendMsg(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case StApplication.RESULT_CODE_SEND_FILE:
                String msgListJson = data.getStringExtra(StApplication.MSG_LIST_JSON);
                if (msgListJson != null) {
                    for (Message msg : Message.fromJsonToCollection(msgListJson)) {
                        handleSendMsg(msg);
                    }
                }
                break;
            case StApplication.RESULT_CODE_CHAT_DETAIL:
                String title = data.getStringExtra(StApplication.CONV_TITLE);
                if (!mIsSingle) {
                    GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
                    UserInfo userInfo = groupInfo.getGroupMemberInfo(mMyInfo.getUserName(), mMyInfo.getAppKey());
                    //如果自己在群聊中，同时显示群人数
                    if (userInfo != null) {
                        if (TextUtils.isEmpty(title)) {
                            mChatView.setChatTitle(IdHelper.getString(mContext, "group"),
                                    data.getIntExtra(MEMBERS_COUNT, 0));
                        } else {
                            mChatView.setChatTitle(title, data.getIntExtra(MEMBERS_COUNT, 0));
                        }
                    } else {
                        if (TextUtils.isEmpty(title)) {
                            mChatView.setChatTitle(IdHelper.getString(mContext, "group"));
                        } else {
                            mChatView.setChatTitle(title);
                        }
                        mChatView.dismissGroupNum();
                    }

                } else mChatView.setChatTitle(title);
                if (data.getBooleanExtra("deleteMsg", false)) {
                    mChatAdapter.clearMsgList();
                }
                break;
        }

    }


    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(int requestCode, Intent data) {
        if (data == null) {
            return;
        }
        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {
            // 本地相册
            sendImageAfterSelfImagePicker(data);
        }
    }

    /**
     * 发送图片
     */

    private void sendImageAfterSelfImagePicker(final Intent data) {
        SendImageHelper.sendImageAfterSelfImagePicker(this, data, new SendImageHelper.Callback() {
            @Override
            public void sendImage(final File file, boolean isOrig) {

                //所有图片都在这里拿到
                ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                        if (responseCode == 0) {
                            Message msg = mConv.createSendMessage(imageContent);
                            handleSendMsg(msg);
                        }
                    }
                });

            }
        });
    }

    //发送极光熊
    private void OnSendImage(String iconUri) {
        String substring = iconUri.substring(7);
        File file = new File(substring);
        ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                if (responseCode == 0) {
                    imageContent.setStringExtra("jiguang", "xiong");
                    Message msg = mConv.createSendMessage(imageContent);
                    handleSendMsg(msg);
                } else {
                    ToastUtil.shortToast(mContext, responseMessage);
                }
            }
        });
    }

    /**
     * 处理发送图片，刷新界面
     *
     * @param msg
     */
    private void handleSendMsg(Message msg) {
        mChatAdapter.setSendMsgs(msg);
        mChatView.setToBottom();
    }

    private static class UIHandler extends Handler {
        private final WeakReference<ChatActivity> mActivity;

        public UIHandler(ChatActivity activity) {
            mActivity = new WeakReference<ChatActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ChatActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case REFRESH_LAST_PAGE:
                        activity.mChatAdapter.dropDownToRefresh();
                        activity.mChatView.getListView().onDropDownComplete();
                        if (activity.mChatAdapter.isHasLastPage()) {
                            if (Build.VERSION.SDK_INT >= 21) {
                                activity.mChatView.getListView()
                                        .setSelectionFromTop(activity.mChatAdapter.getOffset(),
                                                activity.mChatView.getListView().getHeaderHeight());
                            } else {
                                activity.mChatView.getListView().setSelection(activity.mChatAdapter
                                        .getOffset());
                            }
                            activity.mChatAdapter.refreshStartPosition();
                        } else {
                            activity.mChatView.getListView().setSelection(0);
                        }
                        //显示上一页的消息数18条
                        activity.mChatView.getListView()
                                .setOffset(activity.mChatAdapter.getOffset());
                        break;
                    case REFRESH_GROUP_NAME:
                        if (activity.mConv != null) {
                            int num = msg.getData().getInt(MEMBERS_COUNT);
                            String groupName = msg.getData().getString(GROUP_NAME);
                            activity.mChatView.setChatTitle(groupName, num);
                        }
                        break;
                    case REFRESH_GROUP_NUM:
                        int num = msg.getData().getInt(MEMBERS_COUNT);
                        activity.mChatView.setChatTitle(R.string.group, num);
                        break;
                    case REFRESH_CHAT_TITLE:
                        if (activity.mGroupInfo != null) {
                            //检查自己是否在群组中
                            UserInfo info = activity.mGroupInfo.getGroupMemberInfo(activity.mMyInfo.getUserName(),
                                    activity.mMyInfo.getAppKey());
                            if (!TextUtils.isEmpty(activity.mGroupInfo.getGroupName())) {
                                if (info != null) {
                                    activity.mChatView.setChatTitle(activity.mTitle,
                                            activity.mGroupInfo.getGroupMembers().size());
                                    activity.mChatView.showRightBtn();
                                } else {
                                    activity.mChatView.setChatTitle(activity.mTitle);
                                    activity.mChatView.dismissRightBtn();
                                }
                            }
                        }
                        break;
                }
            }
        }
    }

}
