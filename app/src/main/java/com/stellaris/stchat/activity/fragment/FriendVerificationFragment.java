package com.stellaris.stchat.activity.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.adapter.FriendRecommendAdapter;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.database.FriendRecommendEntry;
import com.stellaris.stchat.database.UserEntry;
import com.stellaris.stchat.entity.FriendInvitation;
import com.stellaris.stchat.utils.SharePreferenceManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendVerificationFragment extends BaseFragment {

    @BindView(R.id.friend_recommend_list_view)
    ListView mListView;
    Unbinder unbinder;
    private Activity mContext;
    private FriendRecommendAdapter mAdapter;
    private List<FriendRecommendEntry> mList;

    public FriendVerificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_friend_verification, container, false);
        unbinder = ButterKnife.bind(this, view);
        initData();
        return view;
    }

    private void initData() {
        UserEntry user = StApplication.getUserEntry();
        if (null != user) {
            mList = user.getRecommends();
            mAdapter = new FriendRecommendAdapter(this, mList, mDensity, mWidth);
            mListView.setAdapter(mAdapter);
        } else {
            Log.e("FriendRecommendActivity", "Unexpected error: User table null");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case StApplication.RESULT_BUTTON:
                int position = data.getIntExtra("position", -1);
                int btnState = data.getIntExtra("btn_state", -1);
                FriendRecommendEntry entry = mList.get(position);
                if (btnState == 2) {
                    entry.state = FriendInvitation.ACCEPTED.getValue();
                    entry.save();
                } else if (btnState == 1) {
                    entry.state = FriendInvitation.REFUSED.getValue();
                    entry.save();
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        refreshRedDotCount();
    }

    public void refreshRedDotCount(){
        //刷新小红点的数量显示,小红点显示为发送过后未同意也未拒绝
        int count = 0;
        for(FriendRecommendEntry entry : mList){
            if(entry.state.equals(FriendInvitation.INVITED.getValue())){
                count += 1;
            }
        }
        //Log.d("FriendRec","red dot num:"+count);
        SharePreferenceManager.setCachedNewFriendNum(count);
    }
}
