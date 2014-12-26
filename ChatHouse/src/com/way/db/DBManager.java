/**
 * 
 */
package com.way.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @ClassName: DBManager
 * @Description: TODO 数据库管理
 * @author ou
 * @date 2014年12月25日 下午3:25:29
 * 
 */
public class DBManager {
	static Context mContext;

	DBManager(Context context) {
		mContext = context;
	}

	public static final String ROOM_BRIEF = "room_brief";// 房间简介
	public static final String ROOM_PSW = "room_psw";// 房间密码
	public static final String ROOM_CREAT_TIME = "room_creat_time";// 房间创建时间

	/**
	 * 本地保存聊天室
	 * @param Jid 聊天室id
	 * @param roomName 聊天室 名称
	 * @param roomBrief  聊天室简介
	 * @param psw 密码
	 * @param time 创建时间
	 */
	public static void addRoomToDB(String Jid, String roomName,
			String roomBrief, String psw, String time) {
		SQLiteDatabase db = DBOpenHelper.getInstance(mContext)
				.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ChatRoomConfig.ROOM_JID, Jid);
		cv.put(ChatRoomConfig.ROOM_NAME, roomName);
		cv.put(ChatRoomConfig.ROOM_BRIEF, roomBrief);
		cv.put(ChatRoomConfig.ROOM_PSW, psw);
		cv.put(ChatRoomConfig.ROOM_CREAT_TIME, time);
		db.insert(ChatRoomConfig.TABNAME, null, cv);
	}
}
