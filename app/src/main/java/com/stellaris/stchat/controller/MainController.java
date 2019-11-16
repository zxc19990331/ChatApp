package com.stellaris.stchat.controller;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.MainActivity;
import com.stellaris.stchat.activity.fragment.ContactsFragment;
import com.stellaris.stchat.activity.fragment.ConversationListFragment;
import com.stellaris.stchat.activity.fragment.MeFragment;
import com.stellaris.stchat.adapter.ViewPagerAdapter;
import com.stellaris.stchat.view.MainView;


import java.util.ArrayList;
import java.util.List;


public class MainController implements View.OnClickListener, ViewPager.OnPageChangeListener{
    private MainView mMainView;
    private MainActivity mContext;
    private ConversationListFragment mConvListFragment;
    private MeFragment mMeFragment;
    private ContactsFragment mContactsFragment;

    public MainController(MainView mMainView, MainActivity context) {
        this.mMainView = mMainView;
        this.mContext = context;
        setViewPager();
    }

    private void setViewPager() {
        final List<Fragment> fragments = new ArrayList<>();
        // init Fragment
        mConvListFragment = new ConversationListFragment();
        mContactsFragment = new ContactsFragment();
        mMeFragment = new MeFragment();

        fragments.add(mConvListFragment);
        fragments.add(mContactsFragment);
        fragments.add(mMeFragment);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(mContext.getSupportFragmentManager(),
                fragments);
        mMainView.setViewPagerAdapter(viewPagerAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_msg_btn:
                mMainView.setCurrentItem(0, false);
                break;
            case R.id.actionbar_contact_btn:
                mMainView.setCurrentItem(1, false);
                break;
            case R.id.actionbar_me_btn:
                mMainView.setCurrentItem(2, false);
                break;
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mMainView.setButtonColor(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void sortConvList() {
        //mConvListFragment.sortConvList();
    }

}
