package com.ouchat.smack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ouchat.R;
import com.ouchat.exception.XXException;
import com.ouchat.util.ChatConfig;
import com.ouchat.util.Logout;
import com.ouchat.util.PreferenceUtils;
import com.ouchat.util.StatusMode;
import com.ouchat.xmpplistener.XmppIQListener;
import com.ouchat.xmpplistener.XmppMessageListener;
import com.ouchat.xmpplistener.XmppPresenceListener;
import com.way.db.ChatProvider;
import com.way.db.ChatProvider.ChatConstants;
import com.way.db.RosterProvider;
import com.way.db.RosterProvider.RosterConstants;
import com.way.service.XXService;

public class SmackImpl implements Smack {
	// 客户端名称和类型。主要是向服务器登记，有点类似QQ显示iphone或者Android手机在线的功能
	public static final String XMPP_IDENTITY_NAME = "HAPPY";// 客户端名称
	public static final String XMPP_IDENTITY_TYPE = "phone";// 客户端类型

	private static final int PACKET_TIMEOUT = 30000;// 超时时间
	// 发送离线消息的字段
	final static private String[] SEND_OFFLINE_PROJECTION = new String[] {
			ChatConstants._ID, ChatConstants.JID, ChatConstants.MESSAGE,
			ChatConstants.DATE, ChatConstants.PACKET_ID, ChatConstants.NICKNAME };
	// 发送离线消息的搜索数据库条件，自己发出去的OUTGOING，并且状态为DS_NEW
	final static private String SEND_OFFLINE_SELECTION = ChatConstants.DIRECTION
			+ " = "
			+ ChatConstants.OUTGOING
			+ " AND "
			+ ChatConstants.DELIVERY_STATUS + " = " + ChatConstants.DS_NEW;

	static {
		registerSmackProviders();
	}

	// 做一些基本的配置
	static void registerSmackProviders() {

		ProviderManager pm = ProviderManager.getInstance();

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());
		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());
		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());
		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		// Service Discovery # Items //解析房间列表
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info //某一个房间的信息
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());
		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());
		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());
		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		// Service Discovery # Items //解析房间列表
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info //某一个房间的信息
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());

		// add delayed delivery notifications
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInfoProvider());
		// add carbons and forwarding
		pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE,
				new Forwarded.Provider());
		pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
		pm.addExtensionProvider("received", Carbon.NAMESPACE,
				new Carbon.Provider());
		// add delivery receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
		try {
			Class.forName("org.jivesoftware.smackx.ServiceDiscoveryManager",
					true, SmackImpl.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServiceDiscoveryManager.setIdentityName(XMPP_IDENTITY_NAME);
		ServiceDiscoveryManager.setIdentityType(XMPP_IDENTITY_TYPE);
	}

	private ConnectionConfiguration mXMPPConfig;// 连接配置
	private XMPPConnection mXMPPConnection;// 连接对象
	private XXService mService;// 主服务
	private Roster mRoster;// 联系人对象
	private final ContentResolver mContentResolver;// 数据库操作对象

	private RosterListener mRosterListener;// 联系人动态监听
	/**
	 * 消息动态监听
	 */
	private XmppMessageListener mMessagePacketListener;
	/**
	 * 出席消息动态监听
	 */
	private XmppPresenceListener mPresencePacketListener;
	/**
	 * IQ消息动态监听
	 */
	private XmppIQListener mIQPacketListener;
	private PacketListener mSendFailureListener;// 消息发送失败动态监听
	private PacketListener mPongListener;// ping pong服务器动态监听

	// ping-pong服务器
	private String mPingID;// ping服务器的id
	private long mPingTimestamp;// 时间戳
	private PendingIntent mPingAlarmPendIntent;// 是通过闹钟来控制ping服务器的时间间隔
	private PendingIntent mPongTimeoutAlarmPendIntent;// 判断服务器连接超时的闹钟
	private static final String PING_ALARM = "com.way.xx.PING_ALARM";// ping服务器闹钟BroadcastReceiver的Action
	private static final String PONG_TIMEOUT_ALARM = "com.way.xx.PONG_TIMEOUT_ALARM";// 判断连接超时的闹钟BroadcastReceiver的Action
	private Intent mPingAlarmIntent = new Intent(PING_ALARM);
	private Intent mPongTimeoutAlarmIntent = new Intent(PONG_TIMEOUT_ALARM);
	private PongTimeoutAlarmReceiver mPongTimeoutAlarmReceiver = new PongTimeoutAlarmReceiver();
	private BroadcastReceiver mPingAlarmReceiver = new PingAlarmReceiver();

	// ping-pong服务器

	public SmackImpl(XXService service) {
		String customServer = ChatConfig.XMPP_HOST;// 用户手动设置的服务器名称，本来打算给用户指定服务器的
		int port = ChatConfig.XMPP_PORT;// 端口号，也是留给用户手动设置的

		String server = ChatConfig.XMPP_SERVICE;// 默认的服务器
		boolean smackdebug = PreferenceUtils.getPrefBoolean(service,
				ChatConfig.SMACKDEBUG, false);// 是否需要smack debug
		boolean requireSsl = PreferenceUtils.getPrefBoolean(service,
				ChatConfig.REQUIRE_TLS, false);// 是否需要ssl安全配置
		// 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
		Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
		this.mXMPPConfig = new ConnectionConfiguration(customServer, port,
				server);
		mXMPPConfig.setSelfSignedCertificateEnabled(false);
		mXMPPConfig.setVerifyChainEnabled(false);
		this.mXMPPConfig.setReconnectionAllowed(false);
		this.mXMPPConfig.setSendPresence(false);
		this.mXMPPConfig.setCompressionEnabled(false); // disable for now
		this.mXMPPConfig.setDebuggerEnabled(smackdebug);
		mXMPPConfig.setSASLAuthenticationEnabled(false);
		if (requireSsl)
			this.mXMPPConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.required);

		this.mXMPPConnection = new XMPPConnection(mXMPPConfig);
		this.mService = service;
		mContentResolver = service.getContentResolver();
	}

	/**
	 * 获取mXMPPConnection
	 * 
	 * @return
	 */
	public XMPPConnection getXMPPConnection() {
		return mXMPPConnection;
	}

	/**
	 * 获取服务
	 * 
	 * @return
	 */
	public XXService getService() {
		return mService;
	}

	@Override
	public boolean login(String account, String password) throws XXException {// 登陆实现
		try {

			if (mXMPPConnection.isConnected()) {// 首先判断是否还连接着服务器，需要先断开
				try {
					mXMPPConnection.disconnect();
				} catch (Exception e) {
					Logout.d("conn.disconnect() failed: " + e);
				}
			}
			SmackConfiguration.setPacketReplyTimeout(PACKET_TIMEOUT);// 设置超时时间
			SmackConfiguration.setKeepAliveInterval(-1);
			SmackConfiguration.setDefaultPingInterval(0);
			registerRosterListener();// 监听联系人动态变化
			mXMPPConnection.connect();
			if (!mXMPPConnection.isConnected()) {
				Log.d("ouou", "无连接");
				throw new XXException("SMACK connect failed without exception!");
			}
			mXMPPConnection.addConnectionListener(new ConnectionListener() {
				public void connectionClosedOnError(Exception e) {
					mService.postConnectionFailed(e.getMessage());// 连接关闭时，动态反馈给服务
				}

				public void connectionClosed() {
				}

				public void reconnectingIn(int seconds) {
				}

				public void reconnectionFailed(Exception e) {
				}

				public void reconnectionSuccessful() {
				}
			});
			initServiceDiscovery();// 与服务器交互消息监听,发送消息需要回执，判断是否发送成功
			// SMACK auto-logins if we were authenticated before
			if (!mXMPPConnection.isAuthenticated()) {
				String ressource = PreferenceUtils.getPrefString(mService,
						ChatConfig.RESSOURCE, XMPP_IDENTITY_NAME);

				mXMPPConnection.login(account, password, ressource);
			}
			setStatusFromConfig();// 更新在线状态
			getAllGroupsInService();//测试   获取服务器上的聚会列表
		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage(),
					e.getWrappedThrowable());
		} catch (Exception e) {
			// actually we just care for IllegalState or NullPointer or XMPPEx.
			Logout.e(SmackImpl.class, "login(): " + Log.getStackTraceString(e));
			throw new XXException(e.getLocalizedMessage(), e.getCause());
		}
		registerAllListener();// 注册监听其他的事件，比如新消息

		return mXMPPConnection.isAuthenticated();
	}

	/**
	 * 注册所有的监听
	 */
	private void registerAllListener() {
		// actually, authenticated must be true now, or an exception must have
		// been thrown.
		if (isAuthenticated()) {
			registerMessageListener();// 注册Message、Persence和IQ消息监听
			registerMessageSendFailureListener();// 注册消息发送失败监听
			registerPongListener();// 注册服务器回应ping消息监听
			sendOfflineMessages();// 发送离线消息
			if (mService == null) {
				mXMPPConnection.disconnect();
				return;
			}
			// we need to "ping" the service to let it know we are actually
			// connected, even when no roster entries will come in
			mService.rosterChanged();
		}
	}

	/************ 注册Message、Persence和IQ消息监听 ********************/
	private void registerMessageListener() {
		// do not register multiple packet listeners
		// 消息
		if (mMessagePacketListener != null) {
			mXMPPConnection.removePacketListener(mMessagePacketListener);
		}
		PacketTypeFilter filter = new PacketTypeFilter(Message.class);
		mMessagePacketListener = new XmppMessageListener(this);
		mXMPPConnection.addPacketListener(mMessagePacketListener, filter);

		// 出席消息
		if (mPresencePacketListener != null) {
			mXMPPConnection.removePacketListener(mPresencePacketListener);
		}
		PacketTypeFilter filterpersencs = new PacketTypeFilter(Presence.class);
		mPresencePacketListener = new XmppPresenceListener(this);
		mXMPPConnection.addPacketListener(mPresencePacketListener,
				filterpersencs);

		// IQ消息
		if (mIQPacketListener != null) {
			mXMPPConnection.removePacketListener(mIQPacketListener);
		}
		PacketTypeFilter filteriq = new PacketTypeFilter(Presence.class);
		mIQPacketListener = new XmppIQListener(this);
		mXMPPConnection.addPacketListener(mIQPacketListener, filteriq);

	}

	/**
	 * 将消息添加到数据库
	 * 
	 * @param direction
	 *            是否为收到的消息INCOMING为收到，OUTGOING为自己发出
	 * @param JID
	 *            此消息对应的jid
	 * @param message
	 *            消息内容
	 * @param delivery_status
	 *            消息状态 DS_NEW为新消息，DS_SENT_OR_READ为自己发出或者已读的消息
	 * @param ts
	 *            消息时间戳
	 * @param packetID
	 *            服务器为了区分每一条消息生成的消息包的id
	 * @param nickName
	 *            昵称
	 */
	public void addChatMessageToDB(int direction, String JID, String message,
			int delivery_status, long ts, String packetID, String nickName) {
		ContentValues values = new ContentValues();

		values.put(ChatConstants.DIRECTION, direction);
		values.put(ChatConstants.JID, JID);
		values.put(ChatConstants.MESSAGE, message);
		values.put(ChatConstants.DELIVERY_STATUS, delivery_status);
		values.put(ChatConstants.DATE, ts);
		values.put(ChatConstants.PACKET_ID, packetID);
		values.put(ChatConstants.NICKNAME, nickName);
		mContentResolver.insert(ChatProvider.CONTENT_URI, values);
	}

	/************ end 新消息处理 ********************/

	/***************** start 处理消息发送失败状态 ***********************/
	private void registerMessageSendFailureListener() {
		// do not register multiple packet listeners
		if (mSendFailureListener != null)
			mXMPPConnection
					.removePacketSendFailureListener(mSendFailureListener);

		PacketTypeFilter filter = new PacketTypeFilter(Message.class);

		mSendFailureListener = new PacketListener() {
			public void processPacket(Packet packet) {
				try {
					if (packet instanceof Message) {
						Message msg = (Message) packet;
						String chatMessage = msg.getBody();

						Log.d("SmackableImp",
								"message "
										+ chatMessage
										+ " could not be sent (ID:"
										+ (msg.getPacketID() == null ? "null"
												: msg.getPacketID()) + ")");
						changeMessageDeliveryStatus(msg.getPacketID(),
								ChatConstants.DS_NEW);// 当消息发送失败时，将此消息标记为新消息，下次再发送
					}
				} catch (Exception e) {
					// SMACK silently discards exceptions dropped from
					// processPacket :(
					Logout.e("failed to process packet:");
					e.printStackTrace();
				}
			}
		};

		mXMPPConnection.addPacketSendFailureListener(mSendFailureListener,
				filter);// 这句也是关键啦！
	}

	/**
	 * 改变消息状态
	 * 
	 * @param packetID
	 *            消息的id
	 * @param new_status
	 *            新状态类型
	 */
	public void changeMessageDeliveryStatus(String packetID, int new_status) {
		ContentValues cv = new ContentValues();
		cv.put(ChatConstants.DELIVERY_STATUS, new_status);
		Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
				+ ChatProvider.TABLE_NAME);
		mContentResolver.update(rowuri, cv, ChatConstants.PACKET_ID
				+ " = ? AND " + ChatConstants.DIRECTION + " = "
				+ ChatConstants.OUTGOING, new String[] { packetID });
	}

	/***************** end 处理消息发送失败状态 ***********************/

	/***************** start 处理ping服务器消息 ***********************/
	private void registerPongListener() {
		// reset ping expectation on new connection
		mPingID = null;// 初始化ping的id

		if (mPongListener != null)
			mXMPPConnection.removePacketListener(mPongListener);// 先移除之前监听对象

		mPongListener = new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				if (packet == null)
					return;

				if (packet.getPacketID().equals(mPingID)) {// 如果服务器返回的消息为ping服务器时的消息，说明没有掉线
					Log.d("ouou",
							String.format(
									"Ping: server latency %1.3fs",
									(System.currentTimeMillis() - mPingTimestamp) / 1000.));
					mPingID = null;
					((AlarmManager) mService
							.getSystemService(Context.ALARM_SERVICE))
							.cancel(mPongTimeoutAlarmPendIntent);// 取消超时闹钟
				}
			}

		};

		mXMPPConnection.addPacketListener(mPongListener, new PacketTypeFilter(
				IQ.class));// 正式开始监听
		mPingAlarmPendIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(), 0, mPingAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);// 定时ping服务器，以此来确定是否掉线
		mPongTimeoutAlarmPendIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(), 0, mPongTimeoutAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);// 超时闹钟
		mService.registerReceiver(mPingAlarmReceiver, new IntentFilter(
				PING_ALARM));// 注册定时ping服务器广播接收者
		mService.registerReceiver(mPongTimeoutAlarmReceiver, new IntentFilter(
				PONG_TIMEOUT_ALARM));// 注册连接超时广播接收者
		((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis()
								+ AlarmManager.INTERVAL_FIFTEEN_MINUTES,
						AlarmManager.INTERVAL_FIFTEEN_MINUTES,
						mPingAlarmPendIntent);// 15分钟ping以此服务器
	}

	/**
	 * BroadcastReceiver to trigger reconnect on pong timeout.
	 */
	private class PongTimeoutAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			Logout.d("Ping: timeout for " + mPingID);
			mService.postConnectionFailed(XXService.PONG_TIMEOUT);
			logout();// 超时就断开连接
		}
	}

	/**
	 * BroadcastReceiver to trigger sending pings to the server
	 */
	private class PingAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			if (mXMPPConnection.isAuthenticated()) {
				sendServerPing();// 收到ping服务器的闹钟，即ping一下服务器
			} else
				Logout.d("Ping: alarm received, but not connected to server.");
		}
	}

	/***************** end 处理ping服务器消息 ***********************/

	/***************** start 发送离线消息 ***********************/
	public void sendOfflineMessages() {
		Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI,
				SEND_OFFLINE_PROJECTION, SEND_OFFLINE_SELECTION, null, null);// 查询数据库获取离线消息游标
		final int _ID_COL = cursor.getColumnIndexOrThrow(ChatConstants._ID);
		final int JID_COL = cursor.getColumnIndexOrThrow(ChatConstants.JID);
		final int MSG_COL = cursor.getColumnIndexOrThrow(ChatConstants.MESSAGE);
		final int TS_COL = cursor.getColumnIndexOrThrow(ChatConstants.DATE);
		final int PACKETID_COL = cursor
				.getColumnIndexOrThrow(ChatConstants.PACKET_ID);
		ContentValues mark_sent = new ContentValues();
		mark_sent.put(ChatConstants.DELIVERY_STATUS,
				ChatConstants.DS_SENT_OR_READ);
		while (cursor.moveToNext()) {// 遍历之后将离线消息发出
			int _id = cursor.getInt(_ID_COL);
			String toJID = cursor.getString(JID_COL);
			String message = cursor.getString(MSG_COL);
			String packetID = cursor.getString(PACKETID_COL);
			long ts = cursor.getLong(TS_COL);
			Logout.d("sendOfflineMessages: " + toJID + " > " + message);
			final Message newMessage = new Message(toJID, Message.Type.chat);
			newMessage.setBody(message);
			DelayInformation delay = new DelayInformation(new Date(ts));
			newMessage.addExtension(delay);
			newMessage.addExtension(new DelayInfo(delay));
			newMessage.addExtension(new DeliveryReceiptRequest());
			if ((packetID != null) && (packetID.length() > 0)) {
				newMessage.setPacketID(packetID);
			} else {
				packetID = newMessage.getPacketID();
				mark_sent.put(ChatConstants.PACKET_ID, packetID);
			}
			Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
					+ ChatProvider.TABLE_NAME + "/" + _id);
			// 将消息标记为已发送再调用发送，因为，假设此消息又未发送成功，有SendFailListener重新标记消息
			mContentResolver.update(rowuri, mark_sent, null, null);
			mXMPPConnection.sendPacket(newMessage); // must be after marking
													// delivered, otherwise it
													// may override the
													// SendFailListener
		}
		cursor.close();
	}

	/**
	 * 作为离线消息存储起来，当自己掉线时调用
	 * 
	 * @param cr
	 * @param toJID
	 * @param message
	 */
	public static void saveAsOfflineMessage(ContentResolver cr, String toJID,
			String message) {
		ContentValues values = new ContentValues();
		values.put(ChatConstants.DIRECTION, ChatConstants.OUTGOING);
		values.put(ChatConstants.JID, toJID);
		values.put(ChatConstants.MESSAGE, message);
		values.put(ChatConstants.DELIVERY_STATUS, ChatConstants.DS_NEW);
		values.put(ChatConstants.DATE, System.currentTimeMillis());
		cr.insert(ChatProvider.CONTENT_URI, values);
	}

	/***************** end 发送离线消息 ***********************/
	/******************************* start 联系人数据库事件处理 **********************************/
	private void registerRosterListener() {
		mRoster = mXMPPConnection.getRoster();
		mRosterListener = new RosterListener() {
			private boolean isFristRoter;

			@Override
			public void presenceChanged(Presence presence) {// 联系人状态改变，比如在线或离开、隐身之类
				Log.d("ouou", "presenceChanged(" + presence.getFrom() + "): "
						+ presence);
				String jabberID = getJabberID(presence.getFrom());
				RosterEntry rosterEntry = mRoster.getEntry(jabberID);
				updateRosterEntryInDB(rosterEntry);// 更新联系人数据库
				mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
			}

			@Override
			public void entriesUpdated(Collection<String> entries) {// 更新数据库，第一次登陆
				// TODO Auto-generated method stub
				Log.d("ouou", "entriesUpdated(" + entries + ")");
				for (String entry : entries) {
					RosterEntry rosterEntry = mRoster.getEntry(entry);
					updateRosterEntryInDB(rosterEntry);
				}
				mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
			}

			@Override
			public void entriesDeleted(Collection<String> entries) {// 有好友删除时，
				Log.d("ouou", "entriesDeleted(" + entries + ")");
				for (String entry : entries) {
					deleteRosterEntryFromDB(entry);
				}
				mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
			}

			@Override
			public void entriesAdded(Collection<String> entries) {// 有人添加好友时，我这里没有弹出对话框确认，直接添加到数据库
				Log.d("ouou", "entriesAdded(" + entries + ")");
				ContentValues[] cvs = new ContentValues[entries.size()];
				int i = 0;
				for (String entry : entries) {
					RosterEntry rosterEntry = mRoster.getEntry(entry);
					cvs[i++] = getContentValuesForRosterEntry(rosterEntry);
				}
				mContentResolver.bulkInsert(RosterProvider.CONTENT_URI, cvs);
				if (isFristRoter) {
					isFristRoter = false;
					mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
				}
			}
		};
		mRoster.addRosterListener(mRosterListener);
	}

	public String getJabberID(String from) {
		String[] res = from.split("/");
		return res[0].toLowerCase();
	}

	/**
	 * 更新联系人数据库
	 * 
	 * @param entry
	 *            联系人RosterEntry对象
	 */
	private void updateRosterEntryInDB(final RosterEntry entry) {
		final ContentValues values = getContentValuesForRosterEntry(entry);

		if (mContentResolver.update(RosterProvider.CONTENT_URI, values,
				RosterConstants.JID + " = ?", new String[] { entry.getUser() }) == 0)// 如果数据库无此好友
			addRosterEntryToDB(entry);// 则添加到数据库
	}

	/**
	 * 添加到数据库
	 * 
	 * @param entry
	 *            联系人RosterEntry对象
	 */
	private void addRosterEntryToDB(final RosterEntry entry) {
		ContentValues values = getContentValuesForRosterEntry(entry);
		Uri uri = mContentResolver.insert(RosterProvider.CONTENT_URI, values);
		Log.d("ouou", "addRosterEntryToDB: Inserted " + uri);
	}

	/**
	 * 将联系人从数据库中删除
	 * 
	 * @param jabberID
	 */
	private void deleteRosterEntryFromDB(final String jabberID) {
		int count = mContentResolver.delete(RosterProvider.CONTENT_URI,
				RosterConstants.JID + " = ?", new String[] { jabberID });
		Log.d("ouou", "deleteRosterEntryFromDB: Deleted " + count + " entries");
	}

	/**
	 * 将联系人RosterEntry转化成ContentValues，方便存储数据库
	 * 
	 * @param entry
	 * @return
	 */
	private ContentValues getContentValuesForRosterEntry(final RosterEntry entry) {
		final ContentValues values = new ContentValues();

		values.put(RosterConstants.JID, entry.getUser());
		values.put(RosterConstants.ALIAS, getName(entry));

		Presence presence = mRoster.getPresence(entry.getUser());
		values.put(RosterConstants.STATUS_MODE, getStatusInt(presence));
		values.put(RosterConstants.STATUS_MESSAGE, presence.getStatus());
		values.put(RosterConstants.GROUP, getGroup(entry.getGroups()));

		return values;
	}

	/**
	 * 遍历获取组名
	 * 
	 * @param groups
	 * @return
	 */
	private String getGroup(Collection<RosterGroup> groups) {
		for (RosterGroup group : groups) {
			return group.getName();
		}
		return "";
	}

	/**
	 * 获取联系人名称
	 * 
	 * @param rosterEntry
	 * @return
	 */
	private String getName(RosterEntry rosterEntry) {
		String name = rosterEntry.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		name = StringUtils.parseName(rosterEntry.getUser());
		if (name.length() > 0) {
			return name;
		}
		return rosterEntry.getUser();
	}

	/**
	 * 获取状态
	 * 
	 * @param presence
	 * @return
	 */
	private StatusMode getStatus(Presence presence) {
		if (presence.getType() == Presence.Type.available) {
			if (presence.getMode() != null) {
				return StatusMode.valueOf(presence.getMode().name());
			}
			return StatusMode.available;
		}
		return StatusMode.offline;
	}

	private int getStatusInt(final Presence presence) {
		return getStatus(presence).ordinal();
	}

	/******************************* end 联系人数据库事件处理 **********************************/

	/**
	 * 与服务器交互消息监听,发送消息需要回执，判断对方是否已读此消息
	 */
	private void initServiceDiscovery() {
		// register connection features
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(mXMPPConnection);
		if (sdm == null)
			sdm = new ServiceDiscoveryManager(mXMPPConnection);

		sdm.addFeature("http://jabber.org/protocol/disco#info");

		// reference PingManager, set ping flood protection to 10s
		PingManager.getInstanceFor(mXMPPConnection).setPingMinimumInterval(
				10 * 1000);
		// reference DeliveryReceiptManager, add listener

		DeliveryReceiptManager dm = DeliveryReceiptManager
				.getInstanceFor(mXMPPConnection);
		dm.enableAutoReceipts();
		dm.registerReceiptReceivedListener(new DeliveryReceiptManager.ReceiptReceivedListener() {
			public void onReceiptReceived(String fromJid, String toJid,
					String receiptId) {
				Logout.d(SmackImpl.class, "got delivery receipt for "
						+ receiptId);
				changeMessageDeliveryStatus(receiptId, ChatConstants.DS_ACKED);// 标记为对方已读，实际上遇到了点问题，所以其实没有用上此状态
			}
		});
	}

	@Override
	public void setStatusFromConfig() {// 设置自己的当前状态，供外部服务调用
		boolean messageCarbons = PreferenceUtils.getPrefBoolean(mService,
				ChatConfig.MESSAGE_CARBONS, true);
		String statusMode = PreferenceUtils.getPrefString(mService,
				ChatConfig.STATUS_MODE, ChatConfig.AVAILABLE);
		String statusMessage = PreferenceUtils.getPrefString(mService,
				ChatConfig.STATUS_MESSAGE,
				mService.getString(R.string.status_online));
		int priority = PreferenceUtils.getPrefInt(mService,
				ChatConfig.PRIORITY, 0);
		if (messageCarbons)
			CarbonManager.getInstanceFor(mXMPPConnection).sendCarbonsEnabled(
					true);

		Presence presence = new Presence(Presence.Type.available);
		Mode mode = Mode.valueOf(statusMode);
		presence.setMode(mode);
		presence.setStatus(statusMessage);
		presence.setPriority(priority);
		mXMPPConnection.sendPacket(presence);
	}

	@Override
	public boolean isAuthenticated() {// 是否与服务器连接上，供本类和外部服务调用
		if (mXMPPConnection != null) {
			return (mXMPPConnection.isConnected() && mXMPPConnection
					.isAuthenticated());
		}
		return false;
	}

	/*
	 * 发送消息
	 */
	@Override
	public void sendMessage(String toJID, String message) {
		// TODO Auto-generated method stub
		final Message newMessage = new Message(toJID, Message.Type.chat);
		newMessage.setBody(message);
		newMessage.addExtension(new DeliveryReceiptRequest());
		if (isAuthenticated()) {
			newMessage.setProperty(ChatConstants.NICKNAME,
					String.valueOf(mService.nickName));
			addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
					ChatConstants.DS_SENT_OR_READ, System.currentTimeMillis(),
					newMessage.getPacketID(), getNameForJID(toJID));//

			mXMPPConnection.sendPacket(newMessage);
		} else {
			// send offline -> store to DB
			addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
					ChatConstants.DS_NEW, System.currentTimeMillis(),
					newMessage.getPacketID(), getNameForJID(toJID));//
		}
	}

	@Override
	public void sendServerPing() {
		if (mPingID != null) {// 此时说明上一次ping服务器还未回应，直接返回，直到连接超时
			Logout.d("Ping: requested, but still waiting for " + mPingID);
			return; // a ping is still on its way
		}
		Ping ping = new Ping();
		ping.setType(Type.GET);
		ping.setTo(ChatConfig.XMPP_HOST);
		mPingID = ping.getPacketID();// 此id其实是随机生成，但是唯一的
		mPingTimestamp = System.currentTimeMillis();
		Logout.d("Ping: sending ping " + mPingID);
		mXMPPConnection.sendPacket(ping);// 发送ping消息

		// register ping timeout handler: PACKET_TIMEOUT(30s) + 3s
		((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ PACKET_TIMEOUT + 3000, mPongTimeoutAlarmPendIntent);// 此时需要启动超时判断的闹钟了，时间间隔为30+3秒
	}

	@Override
	public String getNameForJID(String jid) {
		if (null != this.mRoster.getEntry(jid)
				&& null != this.mRoster.getEntry(jid).getName()
				&& this.mRoster.getEntry(jid).getName().length() > 0) {
			return this.mRoster.getEntry(jid).getName();
		} else {
			return jid;
		}
	}

	/**
	 * 设置昵称
	 */
	public boolean setNickName(String JID, String nickName) {
		if (mXMPPConnection.isConnected()) {
			VCard vcard = new VCard();
			try {
				vcard.load(mXMPPConnection, JID);
				Log.d("ouou", "vcard:" + vcard);
				if (vcard != null) {
					vcard.setNickName(nickName);
					Log.d("ouou", "vcard.getNickName():" + vcard.getNickName());
				}
			} catch (XMPPException e) {
				e.printStackTrace();
				Log.d("ouou", "vcard--->e:" + e);
				return false;
			}
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 设置昵称
	 */
	public String getNickName(String JID) {
		String nickNameString = JID;
		VCard vcard = new VCard();
		try {
			vcard.load(mXMPPConnection, JID);
			Log.d("ouou", "vcard:" + vcard);
			if (vcard != null) {
				nickNameString = vcard.getNickName();
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return nickNameString;
	}

	@Override
	public boolean logout() {// 注销登录
		Logout.d("unRegisterCallback()");
		// remove callbacks _before_ tossing old connection
		try {
			mXMPPConnection.getRoster().removeRosterListener(mRosterListener);
			mXMPPConnection.removePacketListener(mMessagePacketListener);
			mXMPPConnection
					.removePacketSendFailureListener(mSendFailureListener);
			mXMPPConnection.removePacketListener(mPongListener);
			((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
					.cancel(mPingAlarmPendIntent);
			((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
					.cancel(mPongTimeoutAlarmPendIntent);
			mService.unregisterReceiver(mPingAlarmReceiver);
			mService.unregisterReceiver(mPongTimeoutAlarmReceiver);
		} catch (Exception e) {
			// ignore it!
			return false;
		}

		if (mXMPPConnection.isConnected()) {
			// work around SMACK's #%&%# blocking disconnect()
			new Thread() {
				public void run() {
					Logout.d("shutDown thread started");
					mXMPPConnection.disconnect();
					Logout.d("shutDown thread finished");
				}
			}.start();
		}
		// setStatusOffline();
		this.mService = null;
		return true;
	}

	/**
	 * 将所有联系人标记为离线状态
	 */
	public void setStatusOffline() {
		ContentValues values = new ContentValues();
		values.put(RosterConstants.STATUS_MODE, StatusMode.offline.ordinal());
		mContentResolver.update(RosterProvider.CONTENT_URI, values, null, null);
	}

	public void register(final String name, final String password) {

		if (mXMPPConnection.isAuthenticated()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					Log.e("ouou", " 注册");

					String newUsername = name;
					String newPassword = password;
					Registration registration = new Registration();
					PacketFilter packetFilter = new AndFilter(
							new PacketIDFilter(registration.getPacketID()),
							new PacketTypeFilter(IQ.class));
					PacketListener packetListener = new PacketListener() {
						@Override
						public void processPacket(Packet packet) {
							Log.d("ouou", "packet:" + packet);
							// 服务器回复客户端
							if (packet instanceof IQ) {
								IQ response = (IQ) packet;
								if (response.getType() == IQ.Type.ERROR) { // 注册失败
									if (!response.getError().toString()
											.contains("409")) {
										Log.e("ouou",
												" 注册失败--Unknown error while registering XMPP account! "
														+ response.getError()
																.getCondition());
									}
								} else if (response.getType() == IQ.Type.RESULT) { // 注册成功
									Log.e("ouou", " 注册成功 ");
								}
							}
						}
					};
					// 给注册的Packet设置Listener，因为只有等到正真注册成功后，我们才可以交流
					mXMPPConnection.addPacketListener(packetListener,
							packetFilter);
					Log.e("ouou", " 注册 ");
					registration.setType(IQ.Type.SET);
					registration.addAttribute("username", newUsername);
					registration.addAttribute("password", newPassword);
					registration.addAttribute("android",
							"geolo_createUser_android");// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧！！！！！
					// 向服务器端，发送注册Packet包，注意其中Registration是Packet的子类
					mXMPPConnection.sendPacket(registration);

					// try {
					// mXMPPConnection.getAccountManager().createAccount("ceshi222",
					// "111111");
					// } catch (XMPPException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// } //创建一个用户

				}
			}).start();
		}

	}

	private void addRosterEntry(String user, String alias) throws XXException {
		if (isAuthenticated()) {
			mRoster = mXMPPConnection.getRoster();
			try {
				mRoster.createEntry(user + "@" + ChatConfig.XMPP_SERVICE,
						alias, new String[] { "好友" });
			} catch (XMPPException e) {
				throw new XXException(e.getLocalizedMessage());
			}
		} else {
			Log.d("ouou", "断开连接了");
		}

	}

	private void removeRosterEntry(String user) throws XXException {
		mRoster = mXMPPConnection.getRoster();
		try {
			RosterEntry rosterEntry = mRoster.getEntry(user);

			if (rosterEntry != null) {
				mRoster.removeEntry(rosterEntry);
			}
		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage());
		}
	}

	/**
	 * 重命名联系人，供外部服务调用
	 */
	@Override
	public void renameRosterItem(String user, String newName)
			throws XXException {
		mRoster = mXMPPConnection.getRoster();
		RosterEntry rosterEntry = mRoster.getEntry(user);

		if (!(newName.length() > 0) || (rosterEntry == null)) {
			throw new XXException("JabberID to rename is invalid!");
		}
		rosterEntry.setName(newName);
	}

	/**
	 * 添加联系人，供外部服务调用
	 */
	@Override
	public void addRosterItem(String user, String alias) throws XXException {
		addRosterEntry(user, alias);
	}

	/**
	 * 删除联系人，供外部服务调用
	 */
	@Override
	public void removeRosterItem(String user) throws XXException {
		// TODO Auto-generated method stub
		Log.d("ouou", "删除联系人(" + user + ")");
		removeRosterEntry(user);
		mService.rosterChanged();
	}

	/**
	 * 向对方发出添加好友申请
	 */
	@Override
	public boolean requestAddFriend(String user) {
		Presence response = new Presence(Presence.Type.subscribe);
		response.setTo(user + "@" + ChatConfig.XMPP_SERVICE);
		if (isAuthenticated()) {
			mXMPPConnection.sendPacket(response);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 拒绝添加好友
	 */
	@Override
	public boolean refusedAddFriend(String user) {
		if (!mXMPPConnection.isAuthenticated()) {
			return false;
		}
		// TODO Auto-generated method stub
		String P_receiver = user;
		Presence presence_back = new Presence(Presence.Type.unsubscribed);
		presence_back.setTo(P_receiver);
		mXMPPConnection.sendPacket(presence_back);
		return true;
	}

	/**
	 * 同意添加此人为好友
	 */
	@Override
	public boolean acceptAddFriend(String user) {
		if (!mXMPPConnection.isAuthenticated()) {
			return false;
		}
		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(user);
		mXMPPConnection.sendPacket(presence);
		return true;
	}

	/**
	 * 获取服务器上的房间列表
	 */
	public void getAllGroupsInService() {

		ArrayList<DiscoverItems.Item> listDiscoverItems = new ArrayList<DiscoverItems.Item>();
		// 获得与XMPPConnection相关的ServiceDiscoveryManager
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
				.getInstanceFor(mXMPPConnection);
		// 获得指定XMPP实体的项目
		// 这个例子获得与在线目录服务相关的项目
		DiscoverItems discoItems;
		try {
			discoItems = discoManager.discoverItems("conference."
					+ ChatConfig.XMPP_SERVICE);
			// 获得被查询的XMPP实体的要查看的项目
			Iterator it = discoItems.getItems();
			// 显示远端XMPP实体的项目
			while (it.hasNext()) {
				DiscoverItems.Item item = (DiscoverItems.Item) it.next();
				Log.d("ouou", "@@@item.getEntityID():" + item.getEntityID());
				Log.d("ouou", "@@@item.getName():" + item.getName());

				MultiUserChat muc = new MultiUserChat(mXMPPConnection,
						item.getEntityID());
				// 聊天室服务将会决定要接受的历史记录数量
				DiscussionHistory history = new DiscussionHistory();
				history.setMaxStanzas(0);
				// 用户加入聊天室
				muc.join(mXMPPConnection.getUser(), "", history,
						SmackConfiguration.getPacketReplyTimeout());

				mXMPPConnection.sendPacket(joinXml(item.getEntityID(),
						item.getName()));
				// System.out.println(item.getEntityID());
				// System.out.println(item.getName());
				listDiscoverItems.add(item);
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户加入时向服务器发送的报文
	 * 
	 * @return
	 */
	public IQ joinXml(final String groupJid, final String groupName) {

		IQ iq = new IQ() {
			public String getChildElementXML() {
				StringBuilder buf = new StringBuilder();
				buf.append("<query xmlns=\"jabber:iq:private\">");
				buf.append("<storage xmlns=\"storage:bookmarks\">");
				buf.append("<").append("conference").append(" name=\"")
						.append(groupName).append("\" autojoin=\"true\"");
				buf.append(" jid=\"").append(groupJid).append("\"").append("/>");
				buf.append("</storage>");
				buf.append("</query>");
				return buf.toString();
			}
		};
		Log.d("ouou", "iq:" + iq.toXML());
		iq.setType(IQ.Type.SET);
		// 方法如名，这里是设置这份报文来至那个JID,后边的/smack是这段信息来至哪个端，如spark端就是/spark，android就是/Smack
		iq.setTo(ChatConfig.XMPP_HOST);
		return iq;
	}
	// private static void configure(ProviderManager pm) {
	// // Service Discovery # Items
	// pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
	// new DiscoverItemsProvider());
	// // Service Discovery # Info
	// pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
	// new DiscoverInfoProvider());
	//
	// // Service Discovery # Items
	// pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
	// new DiscoverItemsProvider());
	// // Service Discovery # Info
	// pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
	// new DiscoverInfoProvider());
	//
	// // Offline Message Requests
	// pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
	// new OfflineMessageRequest.Provider());
	// // Offline Message Indicator
	// pm.addExtensionProvider("offline",
	// "http://jabber.org/protocol/offline",
	// new OfflineMessageInfo.Provider());
	//
	// // vCard
	// pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
	//
	// // FileTransfer
	// pm.addIQProvider("si", "http://jabber.org/protocol/si",
	// new StreamInitiationProvider());
	// pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
	// new BytestreamsProvider());
	// // Data Forms
	// pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
	// // Html
	// pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
	// new XHTMLExtensionProvider());
	// // Ad-Hoc Command
	// pm.addIQProvider("command", "http://jabber.org/protocol/commands",
	// new AdHocCommandDataProvider());
	// // Chat State
	// ChatStateExtension.Provider chatState = new
	// ChatStateExtension.Provider();
	// pm.addExtensionProvider("active",
	// "http://jabber.org/protocol/chatstates", chatState);
	// pm.addExtensionProvider("composing",
	// "http://jabber.org/protocol/chatstates", chatState);
	// pm.addExtensionProvider("paused",
	// "http://jabber.org/protocol/chatstates", chatState);
	// pm.addExtensionProvider("inactive",
	// "http://jabber.org/protocol/chatstates", chatState);
	// pm.addExtensionProvider("gone",
	// "http://jabber.org/protocol/chatstates", chatState);
	// // MUC User,Admin,Owner
	// pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
	// new MUCUserProvider());
	// pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
	// new MUCAdminProvider());
	// pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
	// new MUCOwnerProvider());
	// }

	// private static void initFeatures(XMPPConnection xmppConnection) {
	// ServiceDiscoveryManager.setIdentityName("Android_IM");
	// ServiceDiscoveryManager.setIdentityType("phone");
	// ServiceDiscoveryManager sdm = ServiceDiscoveryManager
	// .getInstanceFor(xmppConnection);
	// if (sdm == null) {
	// sdm = new ServiceDiscoveryManager(xmppConnection);
	// }
	// sdm.addFeature("http://jabber.org/protocol/disco#info");
	// sdm.addFeature("http://jabber.org/protocol/caps");
	// sdm.addFeature("urn:xmpp:avatar:metadata");
	// sdm.addFeature("urn:xmpp:avatar:metadata+notify");
	// sdm.addFeature("urn:xmpp:avatar:data");
	// sdm.addFeature("http://jabber.org/protocol/nick");
	// sdm.addFeature("http://jabber.org/protocol/nick+notify");
	// sdm.addFeature("http://jabber.org/protocol/xhtml-im");
	// sdm.addFeature("http://jabber.org/protocol/muc");
	// sdm.addFeature("http://jabber.org/protocol/commands");
	// sdm.addFeature("http://jabber.org/protocol/si/profile/file-transfer");
	// sdm.addFeature("http://jabber.org/protocol/si");
	// sdm.addFeature("http://jabber.org/protocol/bytestreams");
	// sdm.addFeature("http://jabber.org/protocol/ibb");
	// sdm.addFeature("http://jabber.org/protocol/feature-neg");
	// sdm.addFeature("jabber:iq:privacy");
	// }
}
