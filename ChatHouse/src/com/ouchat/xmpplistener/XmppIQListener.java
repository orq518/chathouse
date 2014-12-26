/**
 * 
 */
package com.ouchat.xmpplistener;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import com.ouchat.smack.SmackImpl;

/**
 * @ClassName: XmppMessageListener
 * @Description: TODO  IQ消息类型
 * @author ou
 * @date 2014年12月5日 下午1:37:57
 * 
 */
public class XmppIQListener implements PacketListener {
	
	SmackImpl mSmack;

	public XmppIQListener(SmackImpl smack) {
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
		// TODO Auto-generated method stub

	}

}
