package com.stellaris.stchat.controller;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;
import com.stellaris.stchat.R;
//import com.stellaris.stchat.activity.DownLoadActivity;
import com.stellaris.stchat.activity.FriendInfoActivity;
import com.stellaris.stchat.activity.GroupNotFriendActivity;
import com.stellaris.stchat.adapter.ChattingListAdapter;
import com.stellaris.stchat.adapter.ChattingListAdapter.ViewHolder;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.pickerimage.utils.BitmapDecoder;
import com.stellaris.stchat.utils.FileHelper;
import com.stellaris.stchat.utils.FileUtils;
import com.stellaris.stchat.utils.HandleResponseCode;
//import com.stellaris.stchat.utils.SimpleCommonUtils;
import com.stellaris.stchat.utils.ToastUtil;


public class ChatItemController {

    private ChattingListAdapter mAdapter;
    private Activity mContext;
    private Conversation mConv;
    private List<Message> mMsgList;
    private ChattingListAdapter.ContentLongClickListener mLongClickListener;
    private float mDensity;
    public Animation mSendingAnim;
    private boolean mSetData = false;
    private final MediaPlayer mp = new MediaPlayer();
    private AnimationDrawable mVoiceAnimation;
    private int mPosition = -1;// 和mSetData一起组成判断播放哪条录音的依据
    private List<Integer> mIndexList = new ArrayList<Integer>();//语音索引
    private FileInputStream mFIS;
    private FileDescriptor mFD;
    private boolean autoPlay = false;
    private int nextPlayPosition = 0;
    private boolean mIsEarPhoneOn;
    private int mSendMsgId;
    private Queue<Message> mMsgQueue = new LinkedList<Message>();
    private UserInfo mUserInfo;
    private Map<Integer, UserInfo> mUserInfoMap = new HashMap<>();

    public ChatItemController(ChattingListAdapter adapter, Activity context, Conversation conv, List<Message> msgList,
                              float density, ChattingListAdapter.ContentLongClickListener longClickListener) {
        this.mAdapter = adapter;
        this.mContext = context;
        this.mConv = conv;
        if (mConv.getType() == ConversationType.single) {
            mUserInfo = (UserInfo) mConv.getTargetInfo();
        }
        this.mMsgList = msgList;
        this.mLongClickListener = longClickListener;
        this.mDensity = density;
        mSendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.jmui_rotate);
        LinearInterpolator lin = new LinearInterpolator();
        mSendingAnim.setInterpolator(lin);

        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }
        mp.setAudioStreamType(AudioManager.STREAM_RING);
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void handleBusinessCard(final Message msg, final ViewHolder holder, int position) {
        final TextContent[] textContent = {(TextContent) msg.getContent()};
        final String[] mUserName = {textContent[0].getStringExtra("userName")};
        final String mAppKey = textContent[0].getStringExtra("appKey");
        holder.ll_businessCard.setTag(position);
        int key = (mUserName[0] + mAppKey).hashCode();
        UserInfo userInfo = mUserInfoMap.get(key);
        if (userInfo != null) {
            String name = userInfo.getNickname();
            //如果没有昵称,名片上面的位置显示用户名
            //如果有昵称,上面显示昵称,下面显示用户名
            if (TextUtils.isEmpty(name)) {
                holder.tv_userName.setText("");
                holder.tv_nickUser.setText(mUserName[0]);
            } else {
                holder.tv_nickUser.setText(name);
                holder.tv_userName.setText("用户名: " + mUserName[0]);
            }
            if (userInfo.getAvatarFile() != null) {
                holder.business_head.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile().getAbsolutePath()));
            } else {
                holder.business_head.setImageResource(R.drawable.jmui_head_icon);
            }
        } else {
            JMessageClient.getUserInfo(mUserName[0], mAppKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int i, String s, UserInfo userInfo) {
                    if (i == 0) {
                        mUserInfoMap.put((mUserName[0] + mAppKey).hashCode(), userInfo);
                        String name = userInfo.getNickname();
                        //如果没有昵称,名片上面的位置显示用户名
                        //如果有昵称,上面显示昵称,下面显示用户名
                        if (TextUtils.isEmpty(name)) {
                            holder.tv_userName.setText("");
                            holder.tv_nickUser.setText(mUserName[0]);
                        } else {
                            holder.tv_nickUser.setText(name);
                            holder.tv_userName.setText("用户名: " + mUserName[0]);
                        }
                        if (userInfo.getAvatarFile() != null) {
                            holder.business_head.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile().getAbsolutePath()));
                        } else {
                            holder.business_head.setImageResource(R.drawable.jmui_head_icon);
                        }
                    } else {
                        HandleResponseCode.onHandle(mContext, i, false);
                    }
                }
            });
        }

        holder.ll_businessCard.setOnLongClickListener(mLongClickListener);
        holder.ll_businessCard.setOnClickListener(new BusinessCard(mUserName[0], mAppKey, holder));
        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case created:
                    if (null != mUserInfo) {
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                        holder.text_receipt.setVisibility(View.GONE);
                    }
                    break;
                case send_success:
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
            }
        } else {
            if (mConv.getType() == ConversationType.group) {
                if (msg.isAtMe()) {
                    mConv.updateMessageExtra(msg, "isRead", true);
                }
                if (msg.isAtAll()) {
                    mConv.updateMessageExtra(msg, "isReadAtAll", true);
                }
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }
        }
        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    private class BusinessCard implements View.OnClickListener {
        private String userName;
        private String appKey;
        private ViewHolder mHolder;

        public BusinessCard(String name, String appKey, ViewHolder holder) {
            this.userName = name;
            this.appKey = appKey;
            this.mHolder = holder;
        }

        @Override
        public void onClick(View v) {
            if (mHolder.ll_businessCard != null && v.getId() == mHolder.ll_businessCard.getId()) {
                JMessageClient.getUserInfo(userName, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        Intent intent = new Intent();
                        if (i == 0) {
                            if (userInfo.isFriend()) {
                                intent.setClass(mContext, FriendInfoActivity.class);
                            } else {
                                intent.setClass(mContext, GroupNotFriendActivity.class);
                            }
                            intent.putExtra(StApplication.TARGET_APP_KEY, appKey);
                            intent.putExtra(StApplication.TARGET_ID, userName);
                            intent.putExtra("fromSearch", true);
                            mContext.startActivity(intent);
                        }else {
                            ToastUtil.shortToast(mContext, "获取信息失败,稍后重试");
                        }
                    }
                });
            }

        }
    }

    public void handleTextMsg(final Message msg, final ViewHolder holder, int position) {
        final String content = ((TextContent) msg.getContent()).getText();
        //SimpleCommonUtils.spannableEmoticonFilter(holder.txtContent, content);
        holder.txtContent.setText(content);
        holder.txtContent.setTag(position);
        holder.txtContent.setOnLongClickListener(mLongClickListener);
        // 检查发送状态，发送方有重发机制
        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case created:
                    if (null != mUserInfo) {
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                        holder.text_receipt.setVisibility(View.GONE);
                    }
                    break;
                case send_success:
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                default:
            }

        } else {
            if (mConv.getType() == ConversationType.group) {
                if (msg.isAtMe()) {
                    mConv.updateMessageExtra(msg, "isRead", true);
                }
                if (msg.isAtAll()) {
                    mConv.updateMessageExtra(msg, "isReadAtAll", true);
                }
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }
        }
        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    // 处理图片
//    public void handleImgMsg(final Message msg, final ViewHolder holder, final int position) {
//        final ImageContent imgContent = (ImageContent) msg.getContent();
//        final String jiguang = imgContent.getStringExtra("jiguang");
//        // 先拿本地缩略图
//        final String path = imgContent.getLocalThumbnailPath();
//        if (path == null) {
//            //从服务器上拿缩略图
//            imgContent.downloadThumbnailImage(msg, new DownloadCompletionCallback() {
//                @Override
//                public void onComplete(int status, String desc, File file) {
//                    if (status == 0) {
//                        ImageView imageView = setPictureScale(jiguang, msg, file.getPath(), holder.picture);
//                        Picasso.with(mContext).load(file).into(imageView);
//                    }
//                }
//            });
//        } else {
//            ImageView imageView = setPictureScale(jiguang, msg, path, holder.picture);
//            Picasso.with(mContext).load(new File(path)).into(imageView);
//        }
//
//        // 接收图片
//        if (msg.getDirect() == MessageDirect.receive) {
//            //群聊中显示昵称
//            if (mConv.getType() == ConversationType.group) {
//                holder.displayName.setVisibility(View.VISIBLE);
//                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
//                    holder.displayName.setText(msg.getFromUser().getUserName());
//                } else {
//                    holder.displayName.setText(msg.getFromUser().getNickname());
//                }
//            }
//
//            switch (msg.getStatus()) {
//                case receive_fail:
//                    holder.picture.setImageResource(R.drawable.jmui_fetch_failed);
//                    holder.resend.setVisibility(View.VISIBLE);
//                    holder.resend.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            imgContent.downloadOriginImage(msg, new DownloadCompletionCallback() {
//                                @Override
//                                public void onComplete(int i, String s, File file) {
//                                    if (i == 0) {
//                                        ToastUtil.shortToast(mContext, "下载成功");
//                                        holder.sendingIv.setVisibility(View.GONE);
//                                        mAdapter.notifyDataSetChanged();
//                                    } else {
//                                        ToastUtil.shortToast(mContext, "下载失败" + s);
//                                    }
//                                }
//                            });
//                        }
//                    });
//                    break;
//                default:
//            }
//            // 发送图片方，直接加载缩略图
//        } else {
////            try {
////                setPictureScale(path, holder.picture);
////                Picasso.with(mContext).load(new File(path)).into(holder.picture);
////            } catch (NullPointerException e) {
////                Picasso.with(mContext).load(IdHelper.getDrawable(mContext, "jmui_picture_not_found"))
////                        .into(holder.picture);
////            }
//            //检查状态
//            switch (msg.getStatus()) {
//                case created:
//                    holder.picture.setEnabled(false);
//                    holder.resend.setEnabled(false);
//                    holder.text_receipt.setVisibility(View.GONE);
//                    holder.sendingIv.setVisibility(View.VISIBLE);
//                    holder.resend.setVisibility(View.GONE);
//                    holder.progressTv.setText("0%");
//                    break;
//                case send_success:
//                    holder.picture.setEnabled(true);
//                    holder.sendingIv.clearAnimation();
//                    holder.text_receipt.setVisibility(View.VISIBLE);
//                    holder.sendingIv.setVisibility(View.GONE);
//                    holder.picture.setAlpha(1.0f);
//                    holder.progressTv.setVisibility(View.GONE);
//                    holder.resend.setVisibility(View.GONE);
//                    break;
//                case send_fail:
//                    holder.resend.setEnabled(true);
//                    holder.picture.setEnabled(true);
//                    holder.sendingIv.clearAnimation();
//                    holder.sendingIv.setVisibility(View.GONE);
//                    holder.text_receipt.setVisibility(View.GONE);
//                    holder.picture.setAlpha(1.0f);
//                    holder.progressTv.setVisibility(View.GONE);
//                    holder.resend.setVisibility(View.VISIBLE);
//                    break;
//                case send_going:
//                    holder.picture.setEnabled(false);
//                    holder.resend.setEnabled(false);
//                    holder.text_receipt.setVisibility(View.GONE);
//                    holder.resend.setVisibility(View.GONE);
//                    sendingImage(msg, holder);
//                    break;
//                default:
//                    holder.picture.setAlpha(0.75f);
//                    holder.sendingIv.setVisibility(View.VISIBLE);
//                    holder.sendingIv.startAnimation(mSendingAnim);
//                    holder.progressTv.setVisibility(View.VISIBLE);
//                    holder.progressTv.setText("0%");
//                    holder.resend.setVisibility(View.GONE);
//                    //从别的界面返回聊天界面，继续发送
//                    if (!mMsgQueue.isEmpty()) {
//                        Message message = mMsgQueue.element();
//                        if (message.getId() == msg.getId()) {
//                            MessageSendingOptions options = new MessageSendingOptions();
//                            options.setNeedReadReceipt(true);
//                            JMessageClient.sendMessage(message, options);
//                            mSendMsgId = message.getId();
//                            sendingImage(message, holder);
//                        }
//                    }
//            }
//        }
//        if (holder.picture != null) {
//            // 点击预览图片
//            holder.picture.setOnClickListener(new BtnOrTxtListener(position, holder));
//            holder.picture.setTag(position);
//            holder.picture.setOnLongClickListener(mLongClickListener);
//
//        }
//        if (msg.getDirect().equals(MessageDirect.send) && holder.resend != null) {
//            holder.resend.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    mAdapter.showResendDialog(holder, msg);
//                }
//            });
//        }
//    }
//
//    private void sendingImage(final Message msg, final ViewHolder holder) {
//        holder.picture.setAlpha(0.75f);
//        holder.sendingIv.setVisibility(View.VISIBLE);
//        holder.sendingIv.startAnimation(mSendingAnim);
//        holder.progressTv.setVisibility(View.VISIBLE);
//        holder.progressTv.setText("0%");
//        holder.resend.setVisibility(View.GONE);
//        //如果图片正在发送，重新注册上传进度Callback
//        if (!msg.isContentUploadProgressCallbackExists()) {
//            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
//                @Override
//                public void onProgressUpdate(double v) {
//                    String progressStr = (int) (v * 100) + "%";
//                    holder.progressTv.setText(progressStr);
//                }
//            });
//        }
//        if (!msg.isSendCompleteCallbackExists()) {
//            msg.setOnSendCompleteCallback(new BasicCallback() {
//                @Override
//                public void gotResult(final int status, String desc) {
//                    if (!mMsgQueue.isEmpty() && mMsgQueue.element().getId() == mSendMsgId) {
//                        mMsgQueue.poll();
//                        if (!mMsgQueue.isEmpty()) {
//                            Message nextMsg = mMsgQueue.element();
//                            MessageSendingOptions options = new MessageSendingOptions();
//                            options.setNeedReadReceipt(true);
//                            JMessageClient.sendMessage(nextMsg, options);
//                            mSendMsgId = nextMsg.getId();
//                        }
//                    }
//                    holder.picture.setAlpha(1.0f);
//                    holder.sendingIv.clearAnimation();
//                    holder.sendingIv.setVisibility(View.GONE);
//                    holder.progressTv.setVisibility(View.GONE);
//                    if (status == 803008) {
//                        CustomContent customContent = new CustomContent();
//                        customContent.setBooleanValue("blackList", true);
//                        Message customMsg = mConv.createSendMessage(customContent);
//                        mAdapter.addMsgToList(customMsg);
//                    } else if (status != 0) {
//                        holder.resend.setVisibility(View.VISIBLE);
//                    }
//
//                    Message message = mConv.getMessage(msg.getId());
//                    mMsgList.set(mMsgList.indexOf(msg), message);
////                    notifyDataSetChanged();
//                }
//            });
//
//        }
//    }

    //正在发送文字或语音
    private void sendingTextOrVoice(final ViewHolder holder, final Message msg) {
        holder.text_receipt.setVisibility(View.GONE);
        holder.resend.setVisibility(View.GONE);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mSendingAnim);
        //消息正在发送，重新注册一个监听消息发送完成的Callback
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, final String desc) {
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    if (status == 803008) {
                        CustomContent customContent = new CustomContent();
                        customContent.setBooleanValue("blackList", true);
                        Message customMsg = mConv.createSendMessage(customContent);
                        mAdapter.addMsgToList(customMsg);
                    } else if (status == 803005) {
                        holder.resend.setVisibility(View.VISIBLE);
                        ToastUtil.shortToast(mContext, "发送失败, 你不在该群组中");
                    } else if (status != 0) {
                        holder.resend.setVisibility(View.VISIBLE);
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
    }




    public void handleGroupChangeMsg(Message msg, ViewHolder holder) {
        String extraMsg = msg.getContent().getStringExtra("msg");
        if (extraMsg != null) { // 聊天室通知事件消息
            holder.groupChange.setText(extraMsg);
            holder.groupChange.setVisibility(View.VISIBLE);
            holder.msgTime.setVisibility(View.GONE);
            return;
        }
        String content = ((EventNotificationContent) msg.getContent()).getEventText();
        EventNotificationContent.EventNotificationType type = ((EventNotificationContent) msg
                .getContent()).getEventNotificationType();
        switch (type) {
            case group_member_added:
            case group_member_exit:
            case group_member_removed:
            case group_info_updated:
            case group_member_keep_silence:
            case group_member_keep_silence_cancel:
                holder.groupChange.setText(content);
                holder.groupChange.setVisibility(View.VISIBLE);
                holder.msgTime.setVisibility(View.GONE);
                break;
        }
    }

    public void handlePromptMsg(Message msg, ViewHolder holder) {
        String promptText = ((PromptContent) msg.getContent()).getPromptText();
        holder.groupChange.setText(promptText);
        holder.groupChange.setVisibility(View.VISIBLE);
        holder.msgTime.setVisibility(View.GONE);
    }

    public void handleCustomMsg(Message msg, ViewHolder holder) {
        CustomContent content = (CustomContent) msg.getContent();
        Boolean isBlackListHint = content.getBooleanValue("blackList");
        Boolean notFriendFlag = content.getBooleanValue("notFriend");
        //TODO:2019/04/09 会话列表滑动时自定义消息这里groupChange会出现null的情况
        if (holder.groupChange != null) {
            if (isBlackListHint != null && isBlackListHint) {
                holder.groupChange.setText(R.string.jmui_server_803008);
                holder.groupChange.setVisibility(View.VISIBLE);
            } else {
                holder.groupChange.setVisibility(View.GONE);
            }
            holder.groupChange.setVisibility(View.GONE);
        }

//        if (notFriendFlag != null && notFriendFlag) {
//            holder.groupChange.setText(IdHelper.getString(mContext, "send_target_is_not_friend"));
//            holder.groupChange.setVisibility(View.VISIBLE);
//        } else {
//            holder.groupChange.setVisibility(View.GONE);
//        }
    }

    public void handleUnSupportMsg(Message msg, ViewHolder holder) {
        if (holder.groupChange != null) {
            holder.groupChange.setText(R.string.unsupported_msg);
        }
    }

    public class BtnOrTxtListener implements View.OnClickListener {

        private int position;
        private ViewHolder holder;

        public BtnOrTxtListener(int index, ViewHolder viewHolder) {
            this.position = index;
            this.holder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            Message msg = mMsgList.get(position);
            MessageDirect msgDirect = msg.getDirect();
            switch (msg.getContentType()) {
//                case image:
//                    if (holder.picture != null && v.getId() == holder.picture.getId()) {
//                        Intent intent = new Intent();
//                        String targetId = "";
//                        intent.putExtra("msgId", msg.getId());
//                        Object targetInfo = mConv.getTargetInfo();
//                        switch (mConv.getType()) {
//                            case single:
//                                targetId = ((UserInfo) targetInfo).getUserName();
//                                break;
//                            case group:
//                                targetId = String.valueOf(((GroupInfo) targetInfo).getGroupID());
//                                break;
//                            default:
//                        }
//                        intent.putExtra(StApplication.CONV_TYPE, mConv.getType());
//                        intent.putExtra(StApplication.TARGET_ID, targetId);
//                        intent.putExtra(StApplication.TARGET_APP_KEY, mConv.getTargetAppKey());
//                        intent.putExtra("msgCount", mMsgList.size());
//                        intent.putIntegerArrayListExtra(StApplication.MsgIDs, getImgMsgIDList());
//                        intent.putExtra("fromChatActivity", true);
//                        intent.setClass(mContext, BrowserViewPagerActivity.class);
//                        mContext.startActivity(intent);
//                    }
//                    break;
            }

        }
    }
    /**
     * 设置图片最小宽高
     *
     * @param path      图片路径
     * @param imageView 显示图片的View
     */
    private ImageView setPictureScale(String extra, Message message, String path, final ImageView imageView) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);


        //计算图片缩放比例
        double imageWidth = opts.outWidth;
        double imageHeight = opts.outHeight;
        return setDensity(extra, message, imageWidth, imageHeight, imageView);
    }

    private ImageView setDensity(String extra, Message message, double imageWidth, double imageHeight, ImageView imageView) {
        if (extra != null) {
            imageWidth = 200;
            imageHeight = 200;
        } else {
            if (imageWidth > 350) {
                imageWidth = 550;
                imageHeight = 250;
            } else if (imageHeight > 450) {
                imageWidth = 300;
                imageHeight = 450;
            } else if ((imageWidth < 50 && imageWidth > 20) || (imageHeight < 50 && imageHeight > 20)) {
                imageWidth = 200;
                imageHeight = 300;
            } else if (imageWidth < 20 || imageHeight < 20) {
                imageWidth = 100;
                imageHeight = 150;
            } else {
                imageWidth = 300;
                imageHeight = 450;
            }
        }

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) imageWidth;
        params.height = (int) imageHeight;
        imageView.setLayoutParams(params);

        return imageView;
    }


    private DisplayImageOptions options = createImageOptions();

    private boolean hasLoaded = false;

    private static final DisplayImageOptions createImageOptions() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }




    private void addToListAndSort(int position) {
        mIndexList.add(position);
        Collections.sort(mIndexList);
    }

    private ArrayList<Integer> getImgMsgIDList() {
        ArrayList<Integer> imgMsgIDList = new ArrayList<Integer>();
        for (Message msg : mMsgList) {
            if (msg.getContentType() == ContentType.image) {
                imgMsgIDList.add(msg.getId());
            }
        }
        return imgMsgIDList;
    }

    private String getImsgMsgListJson() {
        List<Message> messages = new ArrayList<>();
        for (Message msg : mMsgList) {
            if (msg.getContentType() == ContentType.image) {
                messages.add(msg);
            }
        }
        return Message.collectionToJson(messages);
    }

    private void browseDocument(String fileName, String path) {
        try {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(ext);
            File file = new File(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.file_not_support_hint, Toast.LENGTH_SHORT).show();
        }
    }

}
