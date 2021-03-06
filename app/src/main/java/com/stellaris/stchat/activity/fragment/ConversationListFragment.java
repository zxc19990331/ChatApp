package com.stellaris.stchat.activity.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.stellaris.stchat.R;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.controller.AtItemController;
import com.stellaris.stchat.controller.ConversationListController;
import com.stellaris.stchat.controller.MenuItemController;
import com.stellaris.stchat.entity.Event;
import com.stellaris.stchat.view.AtItemView;
import com.stellaris.stchat.view.ConversationListView;
import com.stellaris.stchat.view.MenuItemView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.jmessage.support.qiniu.android.dns.NetworkReceiver;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent;
import cn.jpush.im.android.api.event.MessageRetractEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationListFragment extends BaseFragment {

    private Activity mContext;
    private View mRootView;
    private ConversationListView mConvListView;
    private ConversationListController mConvListController;
    private HandlerThread mThread;
    private static final int SHOW_AT_MSG_RED_DOT = 0x2009;
    private static final int REFRESH_CONVERSATION_LIST = 0x3000;
    private static final int DISMISS_REFRESH_HEADER = 0x3001;
    private static final int ROAM_COMPLETED = 0x3002;
    private BackgroundHandler mBackgroundHandler;
    private View mMenuView;
    private PopupWindow mMenuPopWindow;
    private MenuItemView mMenuItemView;
    private NetworkReceiver mReceiver;
    private MenuItemController mMenuController;
    //atMe的悬浮图标
    private RelativeLayout mRlMain;
    private AtItemView mAtMeItemView;
    private AtItemController mAtMeItemController;

    protected boolean isCreate = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        isCreate = true;
        mContext = this.getActivity();

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_conversation_list,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mConvListView = new ConversationListView(mRootView, this.getActivity(), this);
        mConvListView.initModule();
        mThread = new HandlerThread("MainActivity");
        mThread.start();
        mBackgroundHandler = new BackgroundHandler(mThread.getLooper());

        //加入群组 创建群组的 popupwindow
        mMenuView = getActivity().getLayoutInflater().inflate(R.layout.drop_down_menu, null);
        mMenuPopWindow = new PopupWindow(mMenuView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, true);

        mConvListController = new ConversationListController(mConvListView, this, mWidth);
        mConvListView.setListener(mConvListController);
        mConvListView.setItemListeners(mConvListController);
        mConvListView.setLongClickListener(mConvListController);

        mMenuItemView = new MenuItemView(mMenuView);
        mMenuItemView.initModule();

        mMenuController = new MenuItemController(this);
        mMenuItemView.setListeners(mMenuController);

        //悬浮@小窗口
        mRlMain = mConvListView.getmRlMain();
        mAtMeItemView = new AtItemView(mRootView,this.getActivity(),this);
        mAtMeItemView.initModule();
        mAtMeItemController = new AtItemController(this,mRlMain);
        mAtMeItemView.setOnClickListener(mAtMeItemController);
        mAtMeItemView.setOnTouchListner(mAtMeItemController);

        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (null == activeInfo) {
            mConvListView.showHeaderView();
        } else {
            mConvListView.dismissHeaderView();
            mConvListView.showLoadingHeader();
            mBackgroundHandler.sendEmptyMessageDelayed(DISMISS_REFRESH_HEADER, 1000);
        }
        initReceiver();

    }

    private void initReceiver() {
        mReceiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mReceiver, filter);
    }

    //监听网络状态的广播
    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeInfo = manager.getActiveNetworkInfo();
                if (null == activeInfo) {
                    mConvListView.showHeaderView();
                } else {
                    mConvListView.dismissHeaderView();
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setAtRedDot(boolean b){
        mAtMeItemView.setRedDot(b);
    }

    public void showPopWindow() {
        mMenuPopWindow.setTouchable(true);
        mMenuPopWindow.setOutsideTouchable(true);
        mMenuPopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        if (mMenuPopWindow.isShowing()) {
            mMenuPopWindow.dismiss();
        } else {
            mMenuPopWindow.showAsDropDown(mRootView.findViewById(R.id.create_group_btn), -10, -5);
        }
    }

    /**
     * 收到消息
     */
    public void onEvent(MessageEvent event) {
        mConvListView.setUnReadMsg(JMessageClient.getAllUnReadMsgCount());
        Message msg = event.getMessage();
        if (msg.getTargetType() == ConversationType.group) {
            long groupId = ((GroupInfo) msg.getTargetInfo()).getGroupID();
            Conversation conv = JMessageClient.getGroupConversation(groupId);
            if (conv != null && mConvListController != null) {
                if (msg.isAtMe()) {
                    StApplication.isAtMe.put(groupId, true);
                    mConvListController.getAdapter().putAtConv(conv, msg.getId());
                    //增加一条at消息记录
                    StApplication.atMeMsg.add(new StApplication.GroupMsg(msg,(GroupInfo) msg.getTargetInfo()));
                    //TODO:暂时这么写
                    mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(SHOW_AT_MSG_RED_DOT,
                            conv));
                    //setAtRedDot(true);
                }
                if (msg.isAtAll()) {
                    StApplication.isAtall.put(groupId, true);
                    mConvListController.getAdapter().putAtAllConv(conv, msg.getId());
                    //增加一条at消息记录
                    StApplication.atAllMsg.add(new StApplication.GroupMsg(msg,(GroupInfo) msg.getTargetInfo()));
                    mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(SHOW_AT_MSG_RED_DOT,
                            conv));
                    //TODO:暂时这么写
                    //setAtRedDot(true);
                }
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(REFRESH_CONVERSATION_LIST,
                        conv));

            }
        } else {
            final UserInfo userInfo = (UserInfo) msg.getTargetInfo();
            String targetId = userInfo.getUserName();
            Conversation conv = JMessageClient.getSingleConversation(targetId, userInfo.getAppKey());
            if (conv != null && mConvListController != null) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(userInfo.getAvatar())) {
                            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                                    if (responseCode == 0) {
                                        mConvListController.getAdapter().notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(REFRESH_CONVERSATION_LIST, conv));
            }
        }
    }

    /**
     * 接收离线消息
     *
     * @param event 离线消息事件
     */
    public void onEvent(OfflineMessageEvent event) {
        Conversation conv = event.getConversation();
        if (!conv.getTargetId().equals("feedback_Android") && conv.getType() != ConversationType.chatroom) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(REFRESH_CONVERSATION_LIST, conv));
        }
    }

    /**
     * 消息撤回
     */
    public void onEvent(MessageRetractEvent event) {
        Conversation conversation = event.getConversation();
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(REFRESH_CONVERSATION_LIST, conversation));
    }

    /**
     * 消息已读事件
     */
    public void onEventMainThread(MessageReceiptStatusChangeEvent event) {
        mConvListController.getAdapter().notifyDataSetChanged();
    }

    /**
     * 消息漫游完成事件
     *
     * @param event 漫游完成后， 刷新会话事件
     */
    public void onEvent(ConversationRefreshEvent event) {
        Conversation conv = event.getConversation();
        if (!conv.getTargetId().equals("feedback_Android") && conv.getType() != ConversationType.chatroom) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(REFRESH_CONVERSATION_LIST, conv));
            //多端在线未读数改变时刷新
            if (event.getReason().equals(ConversationRefreshEvent.Reason.UNREAD_CNT_UPDATED)) {
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(REFRESH_CONVERSATION_LIST, conv));
            }
        }
    }

    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_AT_MSG_RED_DOT:
                    setAtRedDot(true);
                    break;
                case REFRESH_CONVERSATION_LIST:
                    Conversation conv = (Conversation) msg.obj;
                    if (conv.getType() != ConversationType.chatroom) {
                        mConvListController.getAdapter().setToTop(conv);
                    }
                    break;
                case DISMISS_REFRESH_HEADER:
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mConvListView.dismissLoadingHeader();
                        }
                    });
                    break;
                case ROAM_COMPLETED:
                    conv = (Conversation) msg.obj;
                    mConvListController.getAdapter().addAndSort(conv);
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) { ;
        switch (event.getType()) {
            case createConversation:
                Conversation conv = event.getConversation();
                if (conv != null) {
                    mConvListController.getAdapter().addNewConversation(conv);
                }
                break;
            case deleteConversation:
                conv = event.getConversation();
                if (null != conv) {
                    mConvListController.getAdapter().deleteConversation(conv);
                }
                break;
            //收到保存为草稿事件
            case draft:
                conv = event.getConversation();
                String draft = event.getDraft();
                //如果草稿内容不为空，保存，并且置顶该会话
                if (!TextUtils.isEmpty(draft)) {
                    mConvListController.getAdapter().putDraftToMap(conv, draft);
                    mConvListController.getAdapter().setToTop(conv);
                    //否则删除
                } else {
                    mConvListController.getAdapter().delDraftFromMap(conv);
                }
                break;
            case addFriend:
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        dismissPopWindow();
        //mMenuItemView.showAddFriend();
        if (StApplication.delConversation != null) {
            mConvListController.delConversation();
        }
        mConvListController.getAdapter().notifyDataSetChanged();
    }

    public void dismissPopWindow() {
        if (mMenuPopWindow.isShowing()) {
            mMenuPopWindow.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mContext.unregisterReceiver(mReceiver);
        mBackgroundHandler.removeCallbacksAndMessages(null);
        mThread.getLooper().quit();
        super.onDestroy();
    }

    public void sortConvList() {
        if (mConvListController != null) {
            mConvListController.getAdapter().sortConvList();
        }
    }

}
