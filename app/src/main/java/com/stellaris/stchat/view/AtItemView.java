package com.stellaris.stchat.view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.fragment.ConversationListFragment;

public class AtItemView {
    private Context mContext;
    private View mConvListFragment;
    private ConversationListFragment mFragment;
    private RelativeLayout mAtMe;
    private ImageButton mAtMeBtn;
    private ImageView mRedDotItem;
    private ImageView mRedDotBtn;

    public AtItemView(View view, Context context, ConversationListFragment fragment){
        this.mConvListFragment = view;
        this.mContext = context;
        this.mFragment = fragment;
    }

    public void initModule(){
        mAtMe = (RelativeLayout) mConvListFragment.findViewById(R.id.at_me_rl);
        mAtMeBtn = (ImageButton) mConvListFragment.findViewById(R.id.at_button_top);
        mRedDotItem = (ImageView)mConvListFragment.findViewById(R.id.at_red_dot);
        mRedDotBtn = (ImageView)mConvListFragment.findViewById(R.id.at_red_dot_btn);
    }

    public void setOnClickListener(View.OnClickListener onClickListener){
        mAtMe.setOnClickListener(onClickListener);
        mAtMeBtn.setOnClickListener(onClickListener);
    }

    public void setOnTouchListner(View.OnTouchListener onTouchListner){
        mAtMe.setOnTouchListener(onTouchListner);
        //mAtMeBtn.setOnTouchListener(onTouchListner);
    }

    public void setRedDot(boolean b){
        mRedDotBtn.setVisibility(b?View.VISIBLE:View.GONE);
        mRedDotItem.setVisibility(b?View.VISIBLE:View.GONE);
    }
}
