package com.ouchat.activity;

import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ouchat.BaseActivity;
import com.ouchat.R;
import com.ouchat.adapter.SimpleListDialogAdapter;
import com.ouchat.dialog.SimpleListDialog;
import com.ouchat.dialog.SimpleListDialog.onSimpleListItemClickListener;
import com.ouchat.ui.view.HandyTextView;
import com.ouchat.ui.view.HeaderLayout;
import com.ouchat.ui.view.HeaderLayout.HeaderStyle;
import com.ouchat.util.ChatConfig;
import com.ouchat.util.Logout;
import com.ouchat.util.MyToast;
import com.ouchat.util.PreferenceUtils;
import com.ouchat.util.ToolsTextUtils;
import com.way.service.IConnectionStatusCallback;
import com.way.service.XXService;

public class LoginActivity extends BaseActivity implements OnClickListener,
		onSimpleListItemClickListener, IConnectionStatusCallback {
	public static final String LOGIN_ACTION = "com.way.action.LOGIN";
	private HeaderLayout mHeaderLayout;
	private EditText mEtAccount;
	private EditText mEtPwd;
	private HandyTextView mHtvForgotPassword;
	private Button mBtnBack;
	private Button mBtnLogin;
	private CheckBox mAutoSavePasswordCK;
	private CheckBox mHideLoginCK;
	private CheckBox mUseTlsCK;
	private CheckBox mSilenceLoginCK;
	private static final String[] DEFAULT_ACCOUNTS = new String[] {
			"+8612345678901", "86930007@qq.com", "86930007" };
	private static final String DEFAULT_PASSWORD = "123456";
	private String mAreaCode = "+86";
	private String mAccount;
	private String mPassword;

	private SimpleListDialog mSimpleListDialog;
	private String[] mCountryCodes;

	private static final int LOGIN_OUT_TIME = 0;
	private XXService mXxService;
	private Dialog mLoginDialog;
	private ConnectionOutTimeProcess mLoginOutTimeProcess;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOGIN_OUT_TIME:
				if (mLoginOutTimeProcess != null
						&& mLoginOutTimeProcess.running)
					mLoginOutTimeProcess.stop();
				if (mLoginDialog != null && mLoginDialog.isShowing())
					mLoginDialog.dismiss();
				MyToast.showShort(LoginActivity.this,
						R.string.timeout_try_again);
				break;

			default:
				break;
			}
		}

	};
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(LoginActivity.this);
			// 开始连接xmpp服务器
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		startService(new Intent(LoginActivity.this, XXService.class));
		bindXMPPService();
		initViews();
		initEvents();
	}

	@Override
	protected void initViews() {
		mAutoSavePasswordCK = (CheckBox) findViewById(R.id.auto_save_password);
		mHideLoginCK = (CheckBox) findViewById(R.id.hide_login);
		mSilenceLoginCK = (CheckBox) findViewById(R.id.silence_login);
		mUseTlsCK = (CheckBox) findViewById(R.id.use_tls);
		mHeaderLayout = (HeaderLayout) findViewById(R.id.login_header);
		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
		mHeaderLayout.setDefaultTitle("登录", null);
		mEtAccount = (EditText) findViewById(R.id.login_et_account);
		mEtPwd = (EditText) findViewById(R.id.login_et_pwd);
		mHtvForgotPassword = (HandyTextView) findViewById(R.id.login_htv_forgotpassword);
		ToolsTextUtils.addUnderlineText(this, mHtvForgotPassword, 0,
				mHtvForgotPassword.getText().length());
		mBtnBack = (Button) findViewById(R.id.login_btn_back);
		mBtnLogin = (Button) findViewById(R.id.login_btn_login);
	}

	@Override
	protected void initEvents() {
		mHtvForgotPassword.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);
		mBtnLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_htv_forgotpassword:
			startActivity(FindPwdTabsActivity.class);
			break;


		case R.id.login_btn_back:
			finish();
			break;

		case R.id.login_btn_login:
			// login();
			LoginClick();
			break;
		}
	}

	@Override
	public void onItemClick(int position) {
		mAccount = null;
		String text = ToolsTextUtils.getCountryCodeBracketsInfo(
				mCountryCodes[position], mAreaCode);
		mEtAccount.requestFocus();
		mEtAccount.setText(text);
		mEtAccount.setSelection(text.length());

	}

	private boolean matchEmail(String text) {
		if (Pattern.compile("\\w[\\w.-]*@[\\w.]+\\.\\w+").matcher(text)
				.matches()) {
			return true;
		}
		return false;
	}

	private boolean matchPhone(String text) {
		if (Pattern.compile("(\\d{11})|(\\+\\d{3,})").matcher(text).matches()) {
			return true;
		}
		return false;
	}

	private boolean matchMoMo(String text) {
		if (Pattern.compile("\\d{7,9}").matcher(text).matches()) {
			return true;
		}
		return false;
	}

	private boolean isNull(EditText editText) {
		String text = editText.getText().toString().trim();
		if (text != null && text.length() > 0) {
			return false;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindXMPPService();
		if (mLoginOutTimeProcess != null) {
			mLoginOutTimeProcess.stop();
			mLoginOutTimeProcess = null;
		}
	}

	public void LoginClick() {
		mAccount = mEtAccount.getText().toString();
		mPassword = mEtPwd.getText().toString();
		if (TextUtils.isEmpty(mAccount)) {
			MyToast.showShort(this, R.string.null_account_prompt);
			return;
		}
		if (TextUtils.isEmpty(mPassword)) {
			MyToast.showShort(this, R.string.password_input_prompt);
			return;
		}
		if (mLoginOutTimeProcess != null && !mLoginOutTimeProcess.running)
			mLoginOutTimeProcess.start();
		if (mLoginDialog != null && !mLoginDialog.isShowing())
			mLoginDialog.show();
		if (mXxService != null) {
			if (!mXxService.isAuthenticated()) {
				mXxService.Login(mAccount, mPassword);
			}else{
				startActivity(new Intent(this, MainActivity.class));
				finish();
			}
			
		}

	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			Log.d("ouou", "LoginActivity.class, ----[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			Logout.e(LoginActivity.class, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		Log.d("ouou", "LoginActivity.class----[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, XXService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void save2Preferences() {
		boolean isAutoSavePassword = mAutoSavePasswordCK.isChecked();
		boolean isUseTls = mUseTlsCK.isChecked();
		boolean isSilenceLogin = mSilenceLoginCK.isChecked();
		boolean isHideLogin = mHideLoginCK.isChecked();
		PreferenceUtils.setPrefString(this, ChatConfig.ACCOUNT, mAccount);// 帐号是一直保存的
		if (isAutoSavePassword)
			PreferenceUtils.setPrefString(this, ChatConfig.PASSWORD, mPassword);
		else
			PreferenceUtils.setPrefString(this, ChatConfig.PASSWORD, "");

		PreferenceUtils.setPrefBoolean(this, ChatConfig.REQUIRE_TLS, isUseTls);
		PreferenceUtils.setPrefBoolean(this, ChatConfig.SCLIENTNOTIFY,
				isSilenceLogin);
		if (isHideLogin)
			PreferenceUtils.setPrefString(this, ChatConfig.STATUS_MODE,
					ChatConfig.XA);
		else
			PreferenceUtils.setPrefString(this, ChatConfig.STATUS_MODE,
					ChatConfig.AVAILABLE);
	}

	// 登录超时处理线程
	class ConnectionOutTimeProcess implements Runnable {
		public boolean running = false;
		private long startTime = 0L;
		private Thread thread = null;

		ConnectionOutTimeProcess() {
		}

		public void run() {
			while (true) {
				if (!this.running)
					return;
				if (System.currentTimeMillis() - this.startTime > 20 * 1000L) {
					mHandler.sendEmptyMessage(LOGIN_OUT_TIME);
				}
				try {
					Thread.sleep(10L);
				} catch (Exception localException) {
				}
			}
		}

		public void start() {
			try {
				this.thread = new Thread(this);
				this.running = true;
				this.startTime = System.currentTimeMillis();
				this.thread.start();
			} finally {
			}
		}

		public void stop() {
			try {
				this.running = false;
				this.thread = null;
				this.startTime = 0L;
			} finally {
			}
		}
	}

	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		if (mLoginDialog != null && mLoginDialog.isShowing())
			mLoginDialog.dismiss();
		if (mLoginOutTimeProcess != null && mLoginOutTimeProcess.running) {
			mLoginOutTimeProcess.stop();
			mLoginOutTimeProcess = null;
		}
		if (connectedState == XXService.CONNECTED) {
			save2Preferences();
			startActivity(new Intent(this, MainActivity.class));
			finish();
		} else if (connectedState == XXService.DISCONNECTED)
			MyToast.showLong(LoginActivity.this,
					getString(R.string.request_failed) + reason);

	}
}
