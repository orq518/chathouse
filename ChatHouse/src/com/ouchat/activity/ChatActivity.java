package com.ouchat.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ouchat.BaseActivity;
import com.ouchat.BaseApplication;
import com.ouchat.R;
import com.ouchat.adapter.ChatAdapter;
import com.ouchat.entity.Message;
import com.ouchat.entity.Message.CONTENT_TYPE;
import com.ouchat.entity.Message.MESSAGE_TYPE;
import com.ouchat.ui.swipeback.SwipeBackActivity;
import com.ouchat.ui.view.HandyTextView;
import com.ouchat.ui.xlistview.MsgListView;
import com.ouchat.ui.xlistview.MsgListView.IXListViewListener;
import com.ouchat.util.ChatConfig;
import com.ouchat.util.FileUtils;
import com.ouchat.util.Logout;
import com.ouchat.util.MyToast;
import com.ouchat.util.PhotoUtils;
import com.ouchat.util.PreferenceUtils;
import com.ouchat.util.StatusMode;
import com.ouchat.util.XMPPHelper;
import com.way.db.ChatProvider;
import com.way.db.ChatProvider.ChatConstants;
import com.way.db.RosterProvider;
import com.way.service.IConnectionStatusCallback;
import com.way.service.XXService;
import com.way.ui.emoji.EmojiKeyboard;
import com.way.ui.emoji.EmojiKeyboard.EventListener;

public class ChatActivity extends SwipeBackActivity implements OnTouchListener,
		OnClickListener, IXListViewListener, IConnectionStatusCallback {
	public static final String INTENT_EXTRA_USERNAME = ChatActivity.class
			.getName() + ".username";// 昵称对应的key
	private MsgListView mMsgListView;// 对话ListView
	private ViewPager mFaceViewPager;// 表情选择ViewPager
	private int mCurrentPage = 0;// 当前表情页
	private boolean mIsFaceShow = false;// 是否显示表情
	private Button mSendMsgBtn;// 发送消息button
	private ImageButton mFaceSwitchBtn;// 切换键盘和表情的button
	private TextView mTitleNameView;// 标题栏
	private EditText mChatEditText;// 消息输入框
	private EmojiKeyboard mFaceRoot;// 表情父容器
	ImageButton chat_add_picture, chat_add_phpto, chat_add_location;
	protected String mCameraImagePath;
	/**
	 * 添加图片，位置等的按钮父布局
	 */
	LinearLayout addLayout;
	FrameLayout bottom_layout;
	private WindowManager.LayoutParams mWindowNanagerParams;
	private InputMethodManager mInputMethodManager;
	private List<String> mFaceMapKeys;// 表情对应的字符串数组
	private String mWithJabberID = null;// 当前聊天用户的ID

	private static final String[] PROJECTION_FROM = new String[] {
			ChatProvider.ChatConstants._ID, ChatProvider.ChatConstants.DATE,
			ChatProvider.ChatConstants.DIRECTION,
			ChatProvider.ChatConstants.JID, ChatProvider.ChatConstants.MESSAGE,
			ChatProvider.ChatConstants.DELIVERY_STATUS,
			ChatProvider.ChatConstants.NICKNAME };// 查询字段

	private ContentObserver mContactObserver = new ContactObserver();// 联系人数据监听，主要是监听对方在线状态
	private XXService mXxService;// Main服务
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(ChatActivity.this);
			// 如果没有连接上，则重新连接xmpp服务器
			if (!mXxService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(ChatActivity.this,
						ChatConfig.ACCOUNT, "");
				String password = PreferenceUtils.getPrefString(
						ChatActivity.this, ChatConfig.PASSWORD, "");
				mXxService.Login(usr, password);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	/**
	 * 解绑服务
	 */
	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
		} catch (IllegalArgumentException e) {
			Logout.e("Service wasn't bound!");
		}
	}

	/**
	 * 绑定服务
	 */
	private void bindXMPPService() {
		Intent mServiceIntent = new Intent(this, XXService.class);
		Uri chatURI = Uri.parse(mWithJabberID);
		mServiceIntent.setData(chatURI);
		bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		initData();// 初始化数据
		initView();// 初始化view
		// initFacePage();// 初始化表情
		setChatWindowAdapter();// 初始化对话数据
		getContentResolver().registerContentObserver(
				RosterProvider.CONTENT_URI, true, mContactObserver);// 开始监听联系人数据库
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateContactStatus();// 更新联系人状态
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// 查询联系人数据库字段
	private static final String[] STATUS_QUERY = new String[] {
			RosterProvider.RosterConstants.STATUS_MODE,
			RosterProvider.RosterConstants.STATUS_MESSAGE, };

	private void updateContactStatus() {
		Cursor cursor = getContentResolver().query(RosterProvider.CONTENT_URI,
				STATUS_QUERY, RosterProvider.RosterConstants.JID + " = ?",
				new String[] { mWithJabberID }, null);
		int MODE_IDX = cursor
				.getColumnIndex(RosterProvider.RosterConstants.STATUS_MODE);
		int MSG_IDX = cursor
				.getColumnIndex(RosterProvider.RosterConstants.STATUS_MESSAGE);

		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			int status_mode = cursor.getInt(MODE_IDX);
			String status_message = cursor.getString(MSG_IDX);
			Logout.d("contact status changed: " + status_mode + " "
					+ status_message);
			mTitleNameView.setText(XMPPHelper.splitJidAndServer(getIntent()
					.getStringExtra(INTENT_EXTRA_USERNAME)));
			int statusId = StatusMode.values()[status_mode].getDrawableId();
			// if (statusId != -1) {// 如果对应离线状态
			// // Drawable icon = getResources().getDrawable(statusId);
			// // mTitleNameView.setCompoundDrawablesWithIntrinsicBounds(icon,
			// // null,
			// // null, null);
			// mTitleStatusView.setImageResource(statusId);
			// mTitleStatusView.setVisibility(View.VISIBLE);
			// } else {
			// mTitleStatusView.setVisibility(View.GONE);
			// }
		}
		cursor.close();
	}

	/**
	 * 联系人数据库变化监听
	 * 
	 */
	private class ContactObserver extends ContentObserver {
		public ContactObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Logout.d("ContactObserver.onChange: " + selfChange);
			updateContactStatus();// 联系人状态变化时，刷新界面
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (hasWindowFocus())
			unbindXMPPService();// 解绑服务
		getContentResolver().unregisterContentObserver(mContactObserver);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// 窗口获取到焦点时绑定服务，失去焦点将解绑
		if (hasFocus)
			bindXMPPService();
		else
			unbindXMPPService();
	}

	private void initData() {
		mWithJabberID = getIntent().getDataString().toLowerCase();// 获取聊天对象的id
		// 将表情map的key保存在数组中
		Set<String> keySet = BaseApplication.getInstance().getFaceMap()
				.keySet();
		mFaceMapKeys = new ArrayList<String>();
		mFaceMapKeys.addAll(keySet);
	}

	/**
	 * 设置聊天的Adapter
	 */
	private void setChatWindowAdapter() {
		String selection = ChatConstants.JID + "='" + mWithJabberID + "'";
		// 异步查询数据库
		new AsyncQueryHandler(getContentResolver()) {

			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				// ListAdapter adapter = new ChatWindowAdapter(cursor,
				// PROJECTION_FROM, PROJECTION_TO, mWithJabberID);
				ListAdapter adapter = new ChatAdapter(ChatActivity.this,
						cursor, PROJECTION_FROM);
				mMsgListView.setAdapter(adapter);
				mMsgListView.setSelection(adapter.getCount() - 1);
			}

		}.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM,
				selection, null, null);
		// 同步查询数据库，建议停止使用,如果数据庞大时，导致界面失去响应
		// Cursor cursor = managedQuery(ChatProvider.CONTENT_URI,
		// PROJECTION_FROM,
		// selection, null, null);
		// ListAdapter adapter = new ChatWindowAdapter(cursor, PROJECTION_FROM,
		// PROJECTION_TO, mWithJabberID);
		// mMsgListView.setAdapter(adapter);
		// mMsgListView.setSelection(adapter.getCount() - 1);
	}

	ImageButton ib_add;
	FrameLayout fatherView;

	private void initView() {
		mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mWindowNanagerParams = getWindow().getAttributes();
		fatherView = (FrameLayout) findViewById(R.id.father);
		mMsgListView = (MsgListView) findViewById(R.id.msg_listView);
		ib_add = (ImageButton) findViewById(R.id.chat_add);
		ib_add.setOnClickListener(this);
		// 触摸ListView隐藏表情和输入法
		mMsgListView.setOnTouchListener(this);
		mMsgListView.setPullLoadEnable(false);
		mMsgListView.setXListViewListener(this);
		mSendMsgBtn = (Button) findViewById(R.id.send);
		mFaceSwitchBtn = (ImageButton) findViewById(R.id.face_switch_btn);
		mChatEditText = (EditText) findViewById(R.id.input);
		mFaceRoot = (EmojiKeyboard) findViewById(R.id.face_ll);
		mFaceRoot.setEventListener(new EventListener() {

			@Override
			public void onEmojiSelected(String res) {
				// TODO Auto-generated method stub
				EmojiKeyboard.input(mChatEditText, res);
			}

			@Override
			public void onBackspace() {
				// TODO Auto-generated method stub
				EmojiKeyboard.backspace(mChatEditText);
			}
		});
		addLayout = (LinearLayout) findViewById(R.id.add_view);
		bottom_layout = (FrameLayout) findViewById(R.id.bottom_layout);
		chat_add_picture = (ImageButton) findViewById(R.id.chat_add_picture);
		chat_add_phpto = (ImageButton) findViewById(R.id.chat_add_phpto);
		chat_add_location = (ImageButton) findViewById(R.id.chat_add_location);
		chat_add_picture.setOnClickListener(this);
		chat_add_phpto.setOnClickListener(this);
		chat_add_location.setOnClickListener(this);
		// mFaceViewPager = (ViewPager) findViewById(R.id.face_pager);
		mChatEditText.setOnTouchListener(this);
		mTitleNameView = (TextView) findViewById(R.id.ivTitleName);
		mChatEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (mWindowNanagerParams.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
							|| mIsFaceShow) {
						hideFace_or_Button();
						mFaceRoot.setVisibility(View.GONE);
						mIsFaceShow = false;
						// imm.showSoftInput(msgEt, 0);
						return true;
					}
				}
				return false;
			}
		});
		mChatEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					mSendMsgBtn.setEnabled(true);
				} else {
					mSendMsgBtn.setEnabled(false);
				}
			}
		});
		mFaceSwitchBtn.setOnClickListener(this);
		mSendMsgBtn.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case PhotoUtils.INTENT_REQUEST_CODE_ALBUM:
			if (data == null) {
				return;
			}
			if (resultCode == RESULT_OK) {
				if (data.getData() == null) {
					return;
				}
				if (!FileUtils.isSdcardExist()) {
					showCustomToast("SD卡不可用,请检查");
					return;
				}
				Uri uri = data.getData();
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(uri, proj, null, null, null);
				if (cursor != null) {
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {
						String path = cursor.getString(column_index);
						Bitmap bitmap = PhotoUtils.getBitmapFromFile(path);
						if (PhotoUtils.bitmapIsLarge(bitmap)) {
							PhotoUtils.cropPhoto(this, this, path);
						} else {
							if (path != null) {
								// mMessages.add(new Message(
								// "nearby_people_other", System
								// .currentTimeMillis(), "0.12km",
								// path, CONTENT_TYPE.IMAGE,
								// MESSAGE_TYPE.SEND));
								// mAdapter.notifyDataSetChanged();
								// mClvList.setSelection(mMessages.size());
							}
						}
					}
				}
			}
			break;

		case PhotoUtils.INTENT_REQUEST_CODE_CAMERA:
			if (resultCode == RESULT_OK) {
				if (mCameraImagePath != null) {
					// mCameraImagePath = PhotoUtils
					// .savePhotoToSDCard(PhotoUtils.CompressionPhoto(
					// mScreenWidth, mCameraImagePath, 2));
					// PhotoUtils.fliterPhoto(this, this, mCameraImagePath);
				}
			}
			mCameraImagePath = null;
			break;

		case PhotoUtils.INTENT_REQUEST_CODE_CROP:
			if (resultCode == RESULT_OK) {
				String path = data.getStringExtra("path");
				if (path != null) {
					// mMessages.add(new Message("nearby_people_other", System
					// .currentTimeMillis(), "0.12km", path,
					// CONTENT_TYPE.IMAGE, MESSAGE_TYPE.SEND));
					// mAdapter.notifyDataSetChanged();
					// mClvList.setSelection(mMessages.size());
				}
			}
			break;

		case PhotoUtils.INTENT_REQUEST_CODE_FLITER:
			if (resultCode == RESULT_OK) {
				String path = data.getStringExtra("path");
				if (path != null) {
					// mMessages.add(new Message("nearby_people_other", System
					// .currentTimeMillis(), "0.12km", path,
					// CONTENT_TYPE.IMAGE, MESSAGE_TYPE.SEND));
					// mAdapter.notifyDataSetChanged();
					// mClvList.setSelection(mMessages.size());
				}
			}
			break;
		}
	}

	/** 显示自定义Toast提示(来自String) **/
	protected void showCustomToast(String text) {
		View toastRoot = LayoutInflater.from(this).inflate(
				R.layout.common_toast, null);
		((HandyTextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(toastRoot);
		toast.show();
	}

	@Override
	public void onRefresh() {
		mMsgListView.stopRefresh();
	}

	@Override
	public void onLoadMore() {
		// do nothing
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.face_switch_btn:
			if (!mIsFaceShow) {
				mInputMethodManager.hideSoftInputFromWindow(
						mChatEditText.getWindowToken(), 0);
				try {
					Thread.sleep(80);// 解决此时会黑一下屏幕的问题
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// mFaceRoot.setVisibility(View.VISIBLE);
				showFace_or_Button(0);
				mFaceSwitchBtn.setImageResource(R.drawable.aio_keyboard);
				mIsFaceShow = true;
			} else {
				// mFaceRoot.setVisibility(View.GONE);
				hideFace_or_Button();
				mInputMethodManager.showSoftInput(mChatEditText, 0);
				mFaceSwitchBtn
						.setImageResource(R.drawable.qzone_edit_face_drawable);
				mIsFaceShow = false;
			}
			break;
		case R.id.send:// 发送消息
			sendMessageIfNotNull();
			break;
		case R.id.chat_add:// 发送位置、图片等消息父布局显示
			showFace_or_Button(1);
			break;
		case R.id.chat_add_picture:// 发送图片消息
			PhotoUtils.selectPhoto(ChatActivity.this);
			break;
		case R.id.chat_add_phpto:// 发送照相消息
			mCameraImagePath = PhotoUtils.takePicture(ChatActivity.this);
			break;
		case R.id.chat_add_location:// 发送位置消息
			break;
		default:
			break;
		}
	}

	public void showFace_or_Button(int type) {
		bottom_layout.setVisibility(View.VISIBLE);
		if (type == 0) {// 显示表情
			mFaceRoot.setVisibility(View.VISIBLE);
			addLayout.setVisibility(View.GONE);
		} else if (type == 1) {// 显示按钮
			addLayout.setVisibility(View.VISIBLE);
			mFaceRoot.setVisibility(View.GONE);
		}

	}

	public void hideFace_or_Button() {
		bottom_layout.setVisibility(View.GONE);

	}

	private void sendMessageIfNotNull() {
		if (mChatEditText.getText().length() >= 1) {
			if (mXxService != null) {
				mXxService.sendMessage(mWithJabberID, mChatEditText.getText()
						.toString());
				if (!mXxService.isAuthenticated())
					MyToast.showShort(this, "未登录,消息已经保存随后发送");
			}
			mChatEditText.setText("");
			mSendMsgBtn.setEnabled(false);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.msg_listView:
			mInputMethodManager.hideSoftInputFromWindow(
					mChatEditText.getWindowToken(), 0);
			mFaceSwitchBtn
					.setImageResource(R.drawable.qzone_edit_face_drawable);
			// mFaceRoot.setVisibility(View.GONE);
			hideFace_or_Button();
			mIsFaceShow = false;
			break;
		case R.id.input:
			mInputMethodManager.showSoftInput(mChatEditText, 0);
			mFaceSwitchBtn
					.setImageResource(R.drawable.qzone_edit_face_drawable);
			// mFaceRoot.setVisibility(View.GONE);
			hideFace_or_Button();
			mIsFaceShow = false;
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		// TODO Auto-generated method stub

	}

}
