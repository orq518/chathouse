package com.ouchat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.ouchat.BaseFragmentActivity;
import com.ouchat.R;
import com.ouchat.util.ChatConfig;
import com.ouchat.util.Logout;
import com.ouchat.util.PreferenceUtils;
import com.way.service.IConnectionStatusCallback;
import com.way.service.XXService;

@SuppressWarnings("all")
public class SettingActivity extends BaseFragmentActivity implements OnClickListener,
		IConnectionStatusCallback {
	public static final String LOGIN_ACTION = "com.way.action.REGISTER";

	private XXService mXxService;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(SettingActivity.this);
			// 开始连接xmpp服务器
			if (!mXxService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(SettingActivity.this,
						ChatConfig.ACCOUNT, "");
				String password = PreferenceUtils.getPrefString(
						SettingActivity.this, ChatConfig.PASSWORD, "");
				mXxService.Login(usr, password);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		startService(new Intent(this, XXService.class));
		bindXMPPService();
		setContentView(R.layout.setting_layout);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindXMPPService();
	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			Log.d("ouou", "setting.class, ----[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
		}
	}

	private void bindXMPPService() {
		Log.d("ouou", "SettingActivity.class----[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, XXService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void login() {
		Intent intent = new Intent();
		intent.setClass(SettingActivity.this, LoginActivity.class);
		startActivity(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.way.service.IConnectionStatusCallback#connectionStatusChanged(int,
	 * java.lang.String)
	 */
	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}


	
}