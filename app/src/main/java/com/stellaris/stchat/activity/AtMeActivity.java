package com.stellaris.stchat.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.stellaris.stchat.R;
import com.stellaris.stchat.adapter.AtMeMsgListAdapter;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.database.UserEntry;
import com.stellaris.stchat.view.AtMeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import com.stellaris.stchat.application.StApplication.GroupMsg;

public class AtMeActivity extends BaseActivity {

    private AtMeView mView;
    private List<GroupMsg> mAtMeMsg = new ArrayList<>();
    private List<GroupMsg> mAtAllMsg = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_me);
        initAtMsg();
        initView();
    }

    public void initView(){
        mView = new AtMeView(this,findViewById(R.id.at_me));
        mView.initModule();
        mView.setAtMeAdapter(new AtMeMsgListAdapter(this,mAtMeMsg));
        mView.setAtAllAdapter(new AtMeMsgListAdapter(this,mAtAllMsg));
        sortMsgByTime();
    }

    public void initAtMsg(){
        if(StApplication.atMeMsg.size() == 0 && StApplication.atAllMsg.size() == 0){
            List<Conversation> convs = JMessageClient.getConversationList();
            for(Conversation conv : convs){
                if(conv.getType().equals(ConversationType.group)) {
                    GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
                    List<Message> msgs = conv.getAllMessage();
                    for (Message msg : msgs) {
                        if (msg.isAtMe()){
                            StApplication.atMeMsg.add(new GroupMsg(msg,groupInfo));
                        } else if (msg.isAtAll()) {
                            StApplication.atAllMsg.add(new GroupMsg(msg,groupInfo));
                        }
                    }
                }
            }
        }
        mAtMeMsg = StApplication.atMeMsg;
        mAtAllMsg = StApplication.atAllMsg;
        Log.d("AtMe","#####" + mAtMeMsg.size() + " " + mAtAllMsg.size());
    }



    public void sortMsgByTime(){
        Comparator<GroupMsg> cmp = new Comparator<GroupMsg>() {
            @Override
            public int compare(GroupMsg o1, GroupMsg o2) {
                Message msg1 = o1.getMsg();
                Message msg2 = o2.getMsg();
                if(msg1.getCreateTime()<msg2.getCreateTime()){
                    return 1;
                }
                return -1;
            }
        };
        Collections.sort(mAtMeMsg, cmp);
        Collections.sort(mAtAllMsg,cmp);
    }

    public void sortMsgByGroup(){

    }



}
