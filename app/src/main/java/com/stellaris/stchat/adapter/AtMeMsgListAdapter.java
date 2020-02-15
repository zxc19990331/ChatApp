package com.stellaris.stchat.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stellaris.stchat.R;
import com.stellaris.stchat.application.StApplication;
import com.stellaris.stchat.utils.TimeFormat;
import com.stellaris.stchat.utils.ViewHolder;

import java.security.acl.Group;
import java.util.List;

import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Message;
import com.stellaris.stchat.application.StApplication.GroupMsg;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class AtMeMsgListAdapter extends BaseAdapter  {
    private Context context;
    private List<GroupMsg> msglist;

    public AtMeMsgListAdapter(Context c,List<GroupMsg> l){
        context = c;
        msglist = l;
    }

    @Override
    public int getCount() {
        return msglist.size();
    }

    @Override
    public GroupMsg getItem(int position) {
        if(msglist == null)
            return null;
        return msglist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GroupMsg msgItem = msglist.get(position);
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_at_msg,null);
        }
        final ImageView headIcon = ViewHolder.get(convertView,R.id.jmui_avatar_iv);
        TextView displayName = ViewHolder.get(convertView,R.id.display_name);
        TextView fromGroup = ViewHolder.get(convertView,R.id.from_group);
        TextView date = ViewHolder.get(convertView,R.id.jmui_send_time_txt);
        TextView content = ViewHolder.get(convertView,R.id.jmui_msg_content);
        Message msg = getItem(position).getMsg();
        displayName.setText(msg.getFromUser().getDisplayName());
        date.setText(new TimeFormat(context,msg.getCreateTime()).getTime());
        content.setText(((TextContent)msg.getContent()).getText());
        fromGroup.setText(getItem(position).getGroupInfo().getGroupName());
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return msglist.isEmpty();
    }
}
