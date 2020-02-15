package com.stellaris.stchat.controller;

import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.AtMeActivity;
import com.stellaris.stchat.activity.fragment.ConversationListFragment;

public class AtItemController  implements View.OnTouchListener,View.OnClickListener{
    private ConversationListFragment mFragment;
    private RelativeLayout mRlayout;
    int lastX = 0,lastY = 0;
    boolean move = false;
    public AtItemController(ConversationListFragment fragment,RelativeLayout relativeLayout){
        this.mFragment = fragment;
        mRlayout = relativeLayout;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.at_me_rl:
                break;
            case R.id.at_button_top:
                Log.d("button_at","######click");
                intent = new Intent(mFragment.getActivity(), AtMeActivity.class);
                mFragment.setAtRedDot(false);
                mFragment.startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.at_button_top:
                break;
            case R.id.at_me_rl:

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        move = true;
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;

                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                        int l =layoutParams.leftMargin + dx;
                        int t = layoutParams.topMargin + dy;
                        int b =mRlayout.getHeight()- t-v.getHeight();
                        int r =mRlayout.getWidth()- l-v.getWidth();
                        if (l < 0) {//处理按钮被移动到上下左右四个边缘时的情况，决定着按钮不会被移动到屏幕外边去
                            l = 0;
                            r =mRlayout.getWidth()-v.getWidth();
                        }
                        if (t < 0) {
                            t = 0;
                            b = mRlayout.getHeight()-v.getHeight();
                        }

                        if (r<0) {
                            r =0;
                            l = mRlayout.getWidth()-v.getWidth();
                        }
                        if (b<0) {
                            b = 0;
                            t = mRlayout.getHeight()-v.getHeight();
                        }
                        layoutParams.leftMargin = l;
                        layoutParams.topMargin = t;
                        layoutParams.bottomMargin = b;
                        layoutParams.rightMargin = r;
                        v.setLayoutParams(layoutParams);

                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        v.postInvalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!move) {
                            Intent intent = new Intent(mFragment.getActivity(), AtMeActivity.class);
                            mFragment.setAtRedDot(false);
                            mFragment.startActivity(intent);
                        }
                        move = false;
                        break;
                }
        }
        return false;
    }
}
