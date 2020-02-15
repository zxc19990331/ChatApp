package com.stellaris.stchat.view;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.activity.AtMeActivity;
import com.stellaris.stchat.adapter.AtMeMsgListAdapter;

import java.util.ArrayList;
import java.util.List;


public class AtMeView {
    private TextView mTitle;
    private AtMeActivity mContext;
    private View mView;
    private ViewPager viewPager;
    private ImageButton mReturn;
    private View lineAtMe;
    private View lineAtAll;
    private TextView tvAtMe;
    private TextView tvAtAll;

    private
    final List<View> views = new ArrayList<>();

    View viewAtMe;
    View viewAtAll;
    ListView listViewAtMe;
    ListView listViewAtAll;

    public AtMeView(AtMeActivity context,View view){
        mContext = context;
        mView = view;
    }

    void setPageSelected(int position){
        switch(position){
            case 0:
                lineAtMe.setVisibility(View.VISIBLE);
                lineAtAll.setVisibility(View.GONE);
                viewPager.setCurrentItem(0);
                break;
            case 1:
                lineAtMe.setVisibility(View.GONE);
                lineAtAll.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1);
                break;
        }
    }

    public void initModule(){
        mTitle = (TextView)mView.findViewById(R.id.title_at_me);
        mTitle.setText("@我的");
        mReturn = (ImageButton)mView.findViewById(R.id.return_btn);
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.finish();
            }
        });

        lineAtMe = mView.findViewById(R.id.line_at_me);
        lineAtAll = mView.findViewById(R.id.line_at_all);
        tvAtMe = mView.findViewById(R.id.tv_at_me);
        tvAtAll = mView.findViewById(R.id.tv_at_all);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.tv_at_me:
                        setPageSelected(0);
                        break;
                    case R.id.tv_at_all:
                        setPageSelected(1);
                        break;
                }
            }
        };



        tvAtMe.setOnClickListener(onClickListener);
        tvAtAll.setOnClickListener(onClickListener);

        viewAtMe =  LayoutInflater.from(mContext).inflate(R.layout.fragment_at_me_msg,null);
        viewAtAll =  LayoutInflater.from(mContext).inflate(R.layout.fragment_at_me_msg,null);
        listViewAtMe = viewAtMe.findViewById(R.id.at_msg_list_view);
        listViewAtAll = viewAtAll.findViewById(R.id.at_msg_list_view);

        views.add(viewAtMe);
        views.add(viewAtAll);



        viewPager = (ViewPager) mView.findViewById(R.id.viewpager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                setPageSelected(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return views.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                viewPager.addView(views.get(position));
                return views.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //这个方法从viewPager中移动当前的view。（划过的时候）
                viewPager.removeView(views.get(position));
            }
        });


        setPageSelected(0);
    }

    public void setAtMeAdapter(AtMeMsgListAdapter atMeMsgListAdapter){
        listViewAtMe.setAdapter(atMeMsgListAdapter);
    }

    public void setAtAllAdapter(AtMeMsgListAdapter atMeMsgListAdapter){
        listViewAtAll.setAdapter(atMeMsgListAdapter);
    }


}
