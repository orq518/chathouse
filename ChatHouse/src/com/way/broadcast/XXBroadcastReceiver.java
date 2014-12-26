package com.way.broadcast;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.ouchat.util.ChatConfig;
import com.ouchat.util.Logout;
import com.ouchat.util.PreferenceUtils;
import com.way.service.XXService;

public class XXBroadcastReceiver extends BroadcastReceiver {
	public static final String BOOT_COMPLETED_ACTION = "com.way.action.BOOT_COMPLETED";
	public static ArrayList<EventHandler> mListeners = new ArrayList<EventHandler>();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d("ouou","action = " + action);
		if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (mListeners.size() > 0)// 通知接口完成加载
				for (EventHandler handler : mListeners) {
					handler.onNetChange();
				}
		} else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Logout.d("System shutdown, stopping service.");
			Intent xmppServiceIntent = new Intent(context, XXService.class);
			context.stopService(xmppServiceIntent);
		} else {
			if (!TextUtils.isEmpty(PreferenceUtils.getPrefString(context,
					ChatConfig.PASSWORD, ""))
					&& PreferenceUtils.getPrefBoolean(context,
							ChatConfig.AUTO_START, true)) {
				Intent i = new Intent(context, XXService.class);
				i.setAction(BOOT_COMPLETED_ACTION);
				context.startService(i);
			}
		}
	}

	public static abstract interface EventHandler {

		public abstract void onNetChange();
	}
}
