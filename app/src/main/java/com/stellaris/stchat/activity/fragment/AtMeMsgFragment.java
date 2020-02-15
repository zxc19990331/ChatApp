package com.stellaris.stchat.activity.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stellaris.stchat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AtMeMsgFragment extends Fragment {


    public AtMeMsgFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_at_me_msg, container, false);
    }

}
