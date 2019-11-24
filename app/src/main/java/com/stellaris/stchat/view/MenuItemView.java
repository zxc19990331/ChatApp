package com.stellaris.stchat.view;

import android.view.View;
import android.widget.RelativeLayout;

import com.stellaris.stchat.R;


public class MenuItemView {

    private View mView;
    private RelativeLayout mCreateGroupLl;
    private RelativeLayout mAdd_open_group;

    public MenuItemView(View view) {
        this.mView = view;
    }

    public void initModule() {
        mCreateGroupLl = mView.findViewById(R.id.create_group);

        mAdd_open_group = mView.findViewById(R.id.join_group);
    }

    public void setListeners(View.OnClickListener listener) {
        mCreateGroupLl.setOnClickListener(listener);
        mAdd_open_group.setOnClickListener(listener);
    }


}
