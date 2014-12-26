package com.ouchat.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.ouchat.BaseFragmentActivity;
import com.ouchat.R;
import com.ouchat.fragment.ContactsFragment;
import com.ouchat.fragment.HouseFragment;
import com.ouchat.fragment.MineFragment;
import com.ouchat.fragment.NearByFragment;
import com.ouchat.fragment.RecentChatFragment;
import com.ouchat.ui.view.AddRosterItemDialog;
import com.ouchat.ui.view.ChangeLog;
import com.ouchat.ui.view.TitleLayout;
import com.ouchat.util.ChatConfig;
import com.ouchat.util.DrawerArrowDrawable;
import com.ouchat.util.DummyTabContent;
import com.ouchat.util.FragmentCallBack;
import com.ouchat.util.MyToast;
import com.ouchat.util.PreferenceUtils;
import com.way.broadcast.XXBroadcastReceiver;
import com.way.service.XXService;
import com.way.service.XmppConnectSucceed;

public class MainActivity extends BaseFragmentActivity implements FragmentCallBack,
		OnClickListener, XmppConnectSucceed {

	ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
	public static HashMap<String, Integer> mStatusMap;
	static {
		mStatusMap = new HashMap<String, Integer>();
		mStatusMap.put(ChatConfig.OFFLINE, -1);
		mStatusMap.put(ChatConfig.DND, R.drawable.status_shield);
		mStatusMap.put(ChatConfig.XA, R.drawable.status_invisible);
		mStatusMap.put(ChatConfig.AWAY, R.drawable.status_leave);
		mStatusMap.put(ChatConfig.AVAILABLE, R.drawable.status_online);
		mStatusMap.put(ChatConfig.CHAT, R.drawable.status_qme);
	}
	// private Handler mainHandler = new Handler();
	private XXService mXxService;
	// private ContentObserver mRosterObserver = new RosterObserver();
	public int mLongPressGroupId, mLongPressChildId;
	/**
	 * 通讯录的view
	 */
	ContactsFragment contactsFragment;
	/**
	 * 最近联系人
	 */
	RecentChatFragment recentChatFragment;
	/**
	 * 附近
	 */
	NearByFragment nearbyFragment;
	/**
	 * 房源
	 */
	HouseFragment houseFragment;
	/**
	 * 我
	 */
	MineFragment mineFragment;
	/**
	 * 连续按两次返回键就退出
	 */
	private long firstTime;

	private TabHost.TabSpec mTabSpec1;
	private TabHost.TabSpec mTabSpec2;
	private TabHost.TabSpec mTabSpec3;
	private TabHost.TabSpec mTabSpec4;
	private TabHost.TabSpec mTabSpec5;

	private View mView1;
	private View mView2;
	private View mView3;
	private View mView4;
	private View mView5;
	/**
	 * 标题
	 */
	String[] titlesArray;
	private TextView mTv1, mTv2, mTv3, mTv4, mTv5;
	private ImageView mIMG1, mIMG2, mIMG3, mIMG4, mIMG5;
	ViewPager viewPager;
	private TabHost mTabHost;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerXmppConnectSucceed(MainActivity.this);
			mXxService.registerConnectionStatusCallback(contactsFragment);
			Log.d("ouou", "onServiceConnected-->mXxService.isAuthenticated():"
					+ mXxService.isAuthenticated());
			// 开始连接xmpp服务器
			if (!mXxService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(MainActivity.this,
						ChatConfig.ACCOUNT, "");
				String password = PreferenceUtils.getPrefString(
						MainActivity.this, ChatConfig.PASSWORD, "");
				mXxService.Login(usr, password);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(MainActivity.this, XXService.class));
		setContentView(R.layout.home_view);
		Resources res =getResources();
		titlesArray=res.getStringArray(R.array.title);
		initViewPager();
		initTabAndFragment();// 设置tabhost

	}
	private void initViewPager() {
		houseFragment = HouseFragment.newInstance();
		nearbyFragment = NearByFragment.newInstance();
		recentChatFragment = RecentChatFragment.newInstance();
		contactsFragment = ContactsFragment.newInstance();
		mineFragment = MineFragment.newInstance();

		fragmentList.add(houseFragment);
		fragmentList.add(nearbyFragment);
		fragmentList.add(recentChatFragment);
		fragmentList.add(contactsFragment);
		fragmentList.add(mineFragment);

		viewPager = (ViewPager) findViewById(R.id.fragment_tabmain_viewPager);
		viewPager.setAdapter(indicatorPagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				int pos = viewPager.getCurrentItem();
				setTextColorByPos(pos);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});// 页面变化时的监听器
		viewPager.setCurrentItem(0);// 设置当前显示标签页为第一页
	}

	private void initTabAndFragment() {
		LayoutInflater inflater = getLayoutInflater();
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();

		TabWidget tabWidget = mTabHost.getTabWidget();
		tabWidget.setDividerDrawable(null);

		mView1 = inflater.inflate(R.layout.tab_btn, null);
		mTv1 = (TextView) mView1.findViewById(R.id.tab_text);
		mIMG1 = (ImageView) mView1.findViewById(R.id.tab_image);
		mTabSpec1 = mTabHost.newTabSpec("tab1");
		mTv1.setText(titlesArray[0]);
		mTabSpec1.setIndicator(mView1);
		mTabSpec1.setContent(new DummyTabContent(getBaseContext()));
		mTabHost.addTab(mTabSpec1);
		mView1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int pos = 0;
				viewPager.setCurrentItem(pos, false);
			}
		});

		mView2 = inflater.inflate(R.layout.tab_btn, null);
		mTv2 = (TextView) mView2.findViewById(R.id.tab_text);
		mIMG2 = (ImageView) mView2.findViewById(R.id.tab_image);
		mTv2.setText(titlesArray[1]);
		mTabSpec2 = mTabHost.newTabSpec("tab2");
		mTabSpec2.setIndicator(mView2);
		mTabSpec2.setContent(new DummyTabContent(getBaseContext()));
		mTabHost.addTab(mTabSpec2);
		mView2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = 1;
				viewPager.setCurrentItem(pos, false);
			}
		});

		mView3 = inflater.inflate(R.layout.tab_btn, null);
		mTv3 = (TextView) mView3.findViewById(R.id.tab_text);
		mIMG3 = (ImageView) mView3.findViewById(R.id.tab_image);
		mTv3.setText(titlesArray[2]);
		mTabSpec3 = mTabHost.newTabSpec("tab3");
		mTabSpec3.setIndicator(mView3);
		mTabSpec3.setContent(new DummyTabContent(getBaseContext()));
		mTabHost.addTab(mTabSpec3);
		mView3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = 2;
				viewPager.setCurrentItem(pos, false);
			}
		});

		mView4 = inflater.inflate(R.layout.tab_btn, null);
		mTv4 = (TextView) mView4.findViewById(R.id.tab_text);
		mIMG4 = (ImageView) mView4.findViewById(R.id.tab_image);
		mTabSpec4 = mTabHost.newTabSpec("tab4");
		mTabSpec4.setIndicator(mView4);
		mTv4.setText(titlesArray[3]);
		mTabSpec4.setContent(new DummyTabContent(getBaseContext()));
		mTabHost.addTab(mTabSpec4);
		mView4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = 3;
				viewPager.setCurrentItem(pos, false);
			}
		});

		mView5 = inflater.inflate(R.layout.tab_btn, null);
		mTv5 = (TextView) mView5.findViewById(R.id.tab_text);
		mIMG5 = (ImageView) mView5.findViewById(R.id.tab_image);
		mTabSpec5 = mTabHost.newTabSpec("tab5");
		mTabSpec5.setIndicator(mView5);
		mTv5.setText(titlesArray[4]);
		mTabSpec5.setContent(new DummyTabContent(getBaseContext()));
		mTabHost.addTab(mTabSpec5);
		mView5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = 4;
				viewPager.setCurrentItem(pos, false);
			}
		});

		setTextColorByPos(0);
	}

	/**
	 * 根据不同的选中位置改变字体颜色
	 * 
	 * @param pos
	 *            - 被选中的tab的位置
	 * */
	private void setTextColorByPos(int pos) {
		mTv1.setTextColor(Color.GRAY);
		mTv2.setTextColor(Color.GRAY);
		mTv3.setTextColor(Color.GRAY);
		mTv4.setTextColor(Color.GRAY);
		mTv5.setTextColor(Color.GRAY);
		mIMG1.setImageResource(R.drawable.tab_house2);
		mIMG2.setImageResource(R.drawable.tab_nearby2);
		mIMG3.setImageResource(R.drawable.tab_chat2);
		mIMG4.setImageResource(R.drawable.tab_contacts2);
		mIMG5.setImageResource(R.drawable.tab_my2);
		switch (pos) {
		case 0:
			mTv1.setTextColor(getResources().getColor(R.color.blue));
			mIMG1.setImageResource(R.drawable.tab_house1);
			break;
		case 1:
			mTv2.setTextColor(getResources().getColor(R.color.blue));
			mIMG2.setImageResource(R.drawable.tab_nearby1);
			break;
		case 2:
			mTv3.setTextColor(getResources().getColor(R.color.blue));
			mIMG3.setImageResource(R.drawable.tab_chat1);
			break;
		case 3:
			mTv4.setTextColor(getResources().getColor(R.color.blue));
			mIMG4.setImageResource(R.drawable.tab_contacts1);
			break;
		case 4:
			mTv5.setTextColor(getResources().getColor(R.color.blue));
			mIMG5.setImageResource(R.drawable.tab_my1);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstTime < 3000) {
			finish();
		} else {
			firstTime = System.currentTimeMillis();
			MyToast.showShort(this, R.string.press_again_backrun);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindXMPPService();
		XXBroadcastReceiver.mListeners.add(contactsFragment);
		ChangeLog cl = new ChangeLog(MainActivity.this);
		if (cl != null && cl.firstRun()) {
			cl.getFullLogDialog().show();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindXMPPService();
		XXBroadcastReceiver.mListeners.remove(contactsFragment);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			Log.d("ouou", "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			Log.d("ouou", "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		Log.d("ouou", "[SERVICE] bindXMPPService");
		bindService(new Intent(MainActivity.this, XXService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
	}

	public boolean isConnected() {
		return mXxService != null && mXxService.isAuthenticated();
	}

	/**
	 * 获取通讯录的fragment对象
	 * 
	 * @return
	 */
	public ContactsFragment getAddressFragment() {
		return contactsFragment;
	}

	@Override
	public XXService getService() {
		return mXxService;
	}

	@Override
	public MainActivity getMainActivity() {
		return this;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivTitleBtnRightImage:
			if (v == null || !mXxService.isAuthenticated()) {
				return;
			}
			new AddRosterItemDialog(this, mXxService).show();// 添加联系人
			break;

		default:
			break;
		}

	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String nameString = (String) msg.obj;
			if (nameString == null || "".equals(nameString)) {
				eText = new EditText(MainActivity.this);
				new AlertDialog.Builder(MainActivity.this).setTitle("请输入设置昵称")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(eText)
						.setPositiveButton("确定", new Dialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								boolean isSetName = mXxService.setNickName(
										mXxService.getXMPPConnection()
												.getUser(), eText.getText()
												.toString());
								if (isSetName) {
									mXxService.unRegisterXmppConnectSucceed();
								}
							}
						}).setNegativeButton("取消", null).show();
			}

		}
	};
	EditText eText;

	@Override
	public void connectionSucceed(String nickName) {// 只关心登陆成功
		if ((nickName == null || "".equals(nickName))
				|| isApplicationBroughtToBackground(this)) {
			Message msg = new Message();
			msg.obj = nickName;
			mHandler.sendMessage(msg);
		}

	}

	public static boolean isApplicationBroughtToBackground(final Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}
	private FragmentStatePagerAdapter indicatorPagerAdapter = new FragmentStatePagerAdapter (
			this.getSupportFragmentManager()) {

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			return fragmentList.get(position);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fragmentList.size();
		}

	};
}
