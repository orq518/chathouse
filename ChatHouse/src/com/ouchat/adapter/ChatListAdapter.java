package com.ouchat.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ouchat.BaseApplication;
import com.ouchat.BaseObjectListAdapter;
import com.ouchat.activity.message.MessageItem;
import com.ouchat.entity.Entity;
import com.ouchat.entity.Message;

public class ChatListAdapter extends BaseObjectListAdapter {

	public ChatListAdapter(BaseApplication application, Context context,
			List<? extends Entity> datas) {
		super(application, context, datas);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message msg = (Message) getItem(position);
		MessageItem messageItem = MessageItem.getInstance(msg, mContext);
		messageItem.fillContent();
		View view = messageItem.getRootView();
		return view;
	}
}
