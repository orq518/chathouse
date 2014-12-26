/**
 * 
 */
package com.ouchat.xmpplistener;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.packet.DelayInfo;

import com.ouchat.smack.SmackImpl;
import com.ouchat.util.Logout;
import com.way.db.ChatProvider.ChatConstants;

/**
 * @ClassName: XmppMessageListener
 * @Description: TODO 处理消息的
 * @author ou
 * @date 2014年12月5日 下午1:37:57
 * 
 */
public class XmppMessageListener implements PacketListener {
	SmackImpl mSmack;

	public XmppMessageListener(SmackImpl smack) {
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

		try {
			if (arg0 instanceof Message) {// 如果是消息类型
				Message msg = (Message) arg0;
				String chatMessage = msg.getBody();
				
				Logout.d("ouou", "11收到的新消息处理 :" + msg.toXML());
				
				// try to extract a carbon
				Carbon cc = CarbonManager.getCarbon(msg);
				
				if (cc != null
						&& cc.getDirection() == Carbon.Direction.received) {// 收到的消息
					Logout.d("ouou", "收到的新消息处理 :" + cc.toXML());
					msg = (Message) cc.getForwarded().getForwardedPacket();
					chatMessage = msg.getBody();
					// fall through
				} else if (cc != null
						&& cc.getDirection() == Carbon.Direction.sent) {// 如果是自己发送的消息，则添加到数据库后直接返回
					Logout.d("ouou", "我大宋的消息处理 :" + cc.toXML());
					msg = (Message) cc.getForwarded().getForwardedPacket();
					chatMessage = msg.getBody();
					if (chatMessage == null)
						return;
					String fromJID = mSmack.getJabberID(msg.getTo());

					mSmack.addChatMessageToDB(ChatConstants.OUTGOING, fromJID,
							chatMessage, ChatConstants.DS_SENT_OR_READ,
							System.currentTimeMillis(), msg.getPacketID(), "");
					// always return after adding
					return;// 记得要返回
				}

				if (chatMessage == null) {
					return;// 如果消息为空，直接返回了
				}

				if (msg.getType() == Message.Type.error) {
					chatMessage = "<Error> " + chatMessage;// 错误的消息类型
				}

				long ts;// 消息时间戳
				DelayInfo timestamp = (DelayInfo) msg.getExtension("delay",
						"urn:xmpp:delay");
				if (timestamp == null)
					timestamp = (DelayInfo) msg.getExtension("x",
							"jabber:x:delay");
				if (timestamp != null)
					ts = timestamp.getStamp().getTime();
				else
					ts = System.currentTimeMillis();

				String fromJID = mSmack.getJabberID(msg.getFrom());// 消息来自对象
				Object object = msg.getProperty(ChatConstants.NICKNAME);
				String nickNameString = "";
				if (object != null) {
					nickNameString = (String) object;
				}

				mSmack.addChatMessageToDB(ChatConstants.INCOMING, fromJID,
						chatMessage, ChatConstants.DS_NEW, ts,
						msg.getPacketID(), nickNameString);// 存入数据库，并标记为新消息DS_NEW
				mSmack.getService().newMessage(fromJID, chatMessage);// 通知service，处理是否需要显示通知栏，
			}
		} catch (Exception e) {
			// SMACK silently discards exceptions dropped from
			// processPacket :(
			Logout.e("failed to process packet:");
			e.printStackTrace();
		}

	}

}
