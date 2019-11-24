package com.stellaris.stchat.controller;

import android.content.Intent;
import android.view.View;

import com.stellaris.stchat.R;
//import com.stellaris.stchat.activity.CommonScanActivity;
import com.stellaris.stchat.activity.CreateGroupActivity;
import com.stellaris.stchat.activity.SearchAddOpenGroupActivity;
import com.stellaris.stchat.activity.SearchForAddFriendActivity;
import com.stellaris.stchat.activity.fragment.ConversationListFragment;
import com.stellaris.stchat.model.Constant;

/**
 * Created by ${chenyn} on 2017/4/9.
 */

public class MenuItemController implements View.OnClickListener {
    private ConversationListFragment mFragment;

    public MenuItemController(ConversationListFragment fragment) {
        this.mFragment = fragment;
    }

    //会话界面的加号
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.create_group:
                mFragment.dismissPopWindow();
                intent = new Intent(mFragment.getContext(), CreateGroupActivity.class);
                mFragment.getContext().startActivity(intent);
                break;
            case R.id.join_group:
                mFragment.dismissPopWindow();
                intent = new Intent(mFragment.getContext(), SearchAddOpenGroupActivity.class);
                mFragment.getContext().startActivity(intent);
                break;
            default:
                break;
        }

    }
}
