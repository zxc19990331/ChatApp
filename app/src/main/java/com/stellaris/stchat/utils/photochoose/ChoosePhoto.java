package com.stellaris.stchat.utils.photochoose;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.api.BasicCallback;
import com.stellaris.stchat.activity.PersonalActivity;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.utils.DialogCreator;
import com.stellaris.stchat.utils.HandleResponseCode;
import com.stellaris.stchat.utils.SharePreferenceManager;
import com.stellaris.stchat.utils.ToastUtil;

/**
 * Created by ${chenyn} on 2017/3/3.
 */

public class ChoosePhoto {
    public PhotoUtils photoUtils;
    private Activity mContext;
    private boolean isFromPersonal;

    public void setInfo(PersonalActivity personalActivity, boolean isFromPersonal) {
        this.mContext = personalActivity;
        this.isFromPersonal = isFromPersonal;
    }

    public void showPhotoDialog(final Context context) {
        photoUtils.selectPicture((Activity) context);
    }


    public void setPortraitChangeListener(final Context context, final ImageView iv_photo, final int count) {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(final Uri uri) {
                Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
                //图片设置给控件
                iv_photo.setImageBitmap(bitmap);
                if (count == 1) {
                    SharePreferenceManager.setRegisterAvatarPath(uri.getPath());
                } else {
                    SharePreferenceManager.setCachedAvatarPath(uri.getPath());
                }
                if (isFromPersonal) {
                    Dialog dialog = DialogCreator.createLoadingDialog(mContext,"loading");
                    JMessageClient.updateUserAvatar(new File(uri.getPath()), new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            dialog.dismiss();
                            if (responseCode == 0) {
                                ToastUtil.shortToast(mContext, "更新成功");
                            } else {
                                ToastUtil.shortToast(mContext, "更新失败" + responseMessage);
                            }
                        }
                    });
                }
            }

            @Override
            public void onPhotoCancel() {
            }
        });
    }

    //更新群组头像
    public void setGroupAvatarChangeListener(final Activity context, final long groupId) {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(final Uri uri) {
                Dialog dialog = DialogCreator.createLoadingDialog(mContext,"loading");
                JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, GroupInfo groupInfo) {
                        if (i == 0) {
                            groupInfo.updateAvatar(new File(uri.getPath()), "", new BasicCallback() {
                                @Override
                                public void gotResult(int i, String s) {
                                    dialog.dismiss();
                                     if (i == 0) {
                                        Intent intent = new Intent();
                                        intent.putExtra("groupAvatarPath", uri.getPath());
                                        context.setResult(Activity.RESULT_OK, intent);
                                        ToastUtil.shortToast(context, "更新成功");
                                        context.finish();
                                    } else {
                                        ToastUtil.shortToast(context, "更新失败");
                                        context.finish();
                                    }
                                }
                            });
                        } else {
                            HandleResponseCode.onHandle(context, i, false);
                        }
                    }
                });

            }

            @Override
            public void onPhotoCancel() {
            }
        });
    }

    public void getCreateGroupAvatar(ImageView iv_groupAvatar) {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                iv_groupAvatar.setImageBitmap(BitmapFactory.decodeFile(uri.getPath()));
                StApplication.groupAvatarPath = uri.getPath();
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

}
