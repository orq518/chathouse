/**
 * 
 */
package com.way.db;


/**
 * @ClassName: ChatRequestConfig
 * @Description: TODO 请求信息的 比如添加好友，请求加群
 * @author ou
 * @date 2014年12月25日 上午11:06:09
 */
public class ChatRequestConfig{
	public static final String TABNAME = "CHATREQUEST";
	public static final String _ID = "_id";// 自增的ID列
	public static final String FORM_JID = "form_jid";// 消息发出者   
	public static final String TO_JID = "to_jid";// 消息接收者
	public static final String TYPE = "type";// 消息类型   是好友请求还是加入聚会请求
	public static final String STATE = "state";// 消息状态   已经接受 或者其他的
	public static final String TIME = "time";//消息发出时间

}
