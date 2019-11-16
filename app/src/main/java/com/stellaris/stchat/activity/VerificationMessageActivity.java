package com.stellaris.stchat.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.jaeger.library.StatusBarUtil;
import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.fragment.FriendVerificationFragment;
import com.stellaris.stchat.activity.fragment.GroupVerificationFragment;
import com.stellaris.stchat.adapter.ViewPagerAdapter;
import com.stellaris.stchat.view.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerificationMessageActivity extends FragmentActivity {

    @BindView(R.id.rg_verification)
    RadioGroup mRg;
    @BindView(R.id.verification_viewpager)
    NoScrollViewPager mPager;
    @BindView(R.id.return_btn)
    ImageButton mBtn_return;
    private int mCurTabIndex;
    private List<Fragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_message);
        ButterKnife.bind(this);
        initView();
    }

    public void initView() {
        mFragments.add(new FriendVerificationFragment());
        mFragments.add(new GroupVerificationFragment());
        mPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), mFragments));

        mRg.check(R.id.rb_friend);
        mRg.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_friend:
                    mCurTabIndex = 0;
                    break;
                case R.id.rb_group:
                    mCurTabIndex = 1;
                    break;
                default:
                    break;
            }
            mPager.setCurrentItem(mCurTabIndex, false);
        });
        StatusBarUtil.setColor(this,getResources().getColor(R.color.jmui_jpush_blue));
    }

    @OnClick(R.id.return_btn)
    public void onViewClicked() {
        finish();
    }
}
