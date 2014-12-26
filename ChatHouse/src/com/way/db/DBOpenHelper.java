/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.way.db;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
	
	private static DBOpenHelper instance;
	
	private static final int DATABASE_VERSION = 1;
	// 数据库名
	private static final String DATABASE_NAME = "ouchat.db";

	private DBOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DBOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DBOpenHelper(context.getApplicationContext());
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建房间的表
		db.execSQL("CREATE TABLE " + ChatRoomConfig.TABNAME + " (" 
	            + ChatRoomConfig._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ChatRoomConfig.ROOM_JID + " TEXT,"
				+ ChatRoomConfig.ROOM_NAME + " TEXT,"
				+ ChatRoomConfig.ROOM_BRIEF+ " TEXT," 
				+ ChatRoomConfig.ROOM_PSW + " TEXT,"
				+ ChatRoomConfig.ROOM_CREAT_TIME + " TEXT);");
		//创建各类邀请消息的表
		db.execSQL("CREATE TABLE " + ChatRequestConfig.TABNAME + " (" 
			     + ChatRequestConfig._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				 + ChatRequestConfig.FORM_JID + " TEXT,"
				 + ChatRequestConfig.TO_JID + " TEXT,"
				 + ChatRequestConfig.TYPE+ " TEXT," 
				 + ChatRequestConfig.STATE + " TEXT,"
				 + ChatRequestConfig.TIME + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //int upgradeVersion = oldVersion;
		// if (18 == upgradeVersion) {
		// // Create table C
		// String sql = "CREATE TABLE ...";
		// db.execSQL(sql);
		// upgradeVersion = 19;
		// }
		//
		// if (20 == upgradeVersion) {
		// // Modify table C
		// upgradeVersion = 20;
		// }
		//
		// if (upgradeVersion != newVersion) {
		// // Drop tables
		// db.execSQL("DROP TABLE IF EXISTS " + tableName);
		// // Create tables
		// onCreate(db);
		// }
	}

	public void closeDB() {
		if (instance != null) {
			try {
				SQLiteDatabase db = instance.getWritableDatabase();
				if (db.isOpen()) {
					db.close();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			instance = null;
		}
	}
	//
	// /**
	// * Upgrade tables. In this method, the sequence is: <b>
	// * <p>
	// * [1] Rename the specified table as a temporary table.
	// * <p>
	// * [2] Create a new table which name is the specified name.
	// * <p>
	// * [3] Insert data into the new created table, data from the temporary
	// * table.
	// * <p>
	// * [4] Drop the temporary table. </b>
	// *
	// * @param db
	// * The database.
	// * @param tableName
	// * The table name.
	// * @param columns
	// * The columns range, format is "ColA, ColB, ColC, ... ColN";
	// */
	// protected void upgradeTables(SQLiteDatabase db, String tableName,
	// String columns) {
	// try {
	// db.beginTransaction();
	//
	// // 1, Rename table.
	// String tempTableName = tableName + "_temp";
	// String sql = "ALTER TABLE " + tableName + " RENAME TO "
	// + tempTableName;
	// db.execSQL(sql, null);
	//
	// // 2, Create table.
	// onCreate(db);
	//
	// // 3, Load data
	// sql = "INSERT INTO " + tableName + " (" + columns + ") "
	// + " SELECT " + columns + " FROM " + tempTableName;
	//
	// db.execSQL(sql, null);
	//
	// // 4, Drop the temporary table.
	// db.execSQL("DROP TABLE IF EXISTS " + tempTableName, null);
	//
	// db.setTransactionSuccessful();
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// db.endTransaction();
	// }
	// }
	//
	// protected String[] getColumnNames(SQLiteDatabase db, String tableName) {
	// String[] columnNames = null;
	// Cursor c = null;
	//
	// try {
	// c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
	// if (null != c) {
	// int columnIndex = c.getColumnIndex("name");
	// if (-1 == columnIndex) {
	// return null;
	// }
	//
	// int index = 0;
	// columnNames = new String[c.getCount()];
	// for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
	// columnNames[index] = c.getString(columnIndex);
	// index++;
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// c.close();
	// c = null;
	// }
	//
	// return columnNames;
	// }
}
