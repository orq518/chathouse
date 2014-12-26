/**
 * 
 */
package com.ouchat.xmpplistener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.ouchat.smack.SmackImpl;
import com.ouchat.util.Logout;

import android.os.Bundle;

/**
 * @ClassName: XmppMessageListener
 * @Description: TODO 出席消息类型
 * @author ou
 * @date 2014年12月5日 下午1:37:57
 * 
 */
public class XmppPresenceListener implements PacketListener {

	SmackImpl mSmack;

	public XmppPresenceListener(SmackImpl smack) {
		mSmack = smack;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.
	 * smack.packet.Packet)
	 */
	@Override
	public void processPacket(Packet arg0) {
		// 出席消息类型
		Presence presence = (Presence) arg0;
		// available -- （默认）用户空闲状态
		// unavailable -- 用户没空看消息
		// subscribe -- 请求订阅别人，即请求加对方为好友
		// subscribed -- 统一被别人订阅，也就是确认被对方加为好友
		// unsubscribe -- 他取消订阅别人，请求删除某好友
		// unsubscribed -- 拒绝被别人订阅，即拒绝对放的添加请求
		// error -- 当前状态packet有错误
		
		if (presence.getType().equals(Presence.Type.subscribe)) {// 好友申请
			Logout.d("ouou", "好友申请 :" +presence.getType());
		} else if (presence.getType().equals(Presence.Type.subscribed)) {// 同意添加好友
			Logout.d("ouou", "同意添加好友 :" +presence.getType());
		} else if (presence.getType().equals(Presence.Type.unsubscribe)) {// 拒绝添加好友和删除好友、 别人删除我
			Logout.d("ouou", "拒绝添加好友和删除好友、 别人删除我 :" +presence.getType());
		} else if (presence.getType().equals(Presence.Type.unsubscribed)) {//拒绝被别人订阅，即拒绝对放的添加请求
			Logout.d("ouou", "拒绝被别人订阅，即拒绝对放的添加请求:" +presence.getType());
		} else if (presence.getType().equals(Presence.Type.unavailable)) {// 好友下线
			Logout.d("ouou", "好友下线:" +presence.getType());
		} else if (presence.getType().equals(Presence.Type.available)) {// 聚会上線
			Logout.d("ouou", "聚会上線:" +presence.getType());
		}

	}

}
