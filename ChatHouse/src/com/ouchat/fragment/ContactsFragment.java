package com.ouchat.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ouchat.R;
import com.ouchat.activity.ChatActivity;
import com.ouchat.activity.MainActivity;
import com.ouchat.ui.sortlistview.CharacterParser;
import com.ouchat.ui.sortlistview.PinyinComparator;
import com.ouchat.ui.sortlistview.SideBar;
import com.ouchat.ui.sortlistview.SortAdapter;
import com.ouchat.ui.sortlistview.SortModel;
import com.ouchat.ui.sortlistview.SideBar.OnTouchingLetterChangedListener;
import com.ouchat.ui.view.TitleLayout;
import com.ouchat.util.FragmentCallBack;
import com.ouchat.util.Logout;
import com.ouchat.util.MyToast;
import com.ouchat.util.NetUtil;
import com.way.broadcast.XXBroadcastReceiver.EventHandler;
import com.way.db.RosterProvider;
import com.way.db.RosterProvider.RosterConstants;
import com.way.service.IConnectionStatusCallback;
import com.way.service.XXService;

public class ContactsFragment extends Fragment implements OnClickListener,
		IConnectionStatusCallback, EventHandler {

	private MainActivity mainActivity;
	View mainView;
	public View mNetErrorView;
	public static final int ID_CHAT = 0;
	public static final int ID_AVAILABLE = 1;
	public static final int ID_AWAY = 2;
	public static final int ID_XA = 3;
	public static final int ID_DND = 4;
	private ContentObserver mRosterObserver = new RosterObserver();
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<SortModel> contactList = new ArrayList<SortModel>();

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;
	/**
	 * 新朋友和群聚会
	 */
	LinearLayout newFriends, groups;

	public static ContactsFragment newInstance() {
		ContactsFragment newFragment = new ContactsFragment();
		return newFragment;

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mainActivity = ((FragmentCallBack) activity).getMainActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainActivity.getContentResolver().registerContentObserver(
				RosterProvider.CONTENT_URI, true, mRosterObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.main_center_layout, container,
				false);
		initViews(mainView);
		initTitle();
		return mainView;
	}

	/**
	 * 标题的布局
	 */
	TitleLayout titleLayout;

	// 标题
	private void initTitle() {
		titleLayout = (TitleLayout) mainView.findViewById(R.id.title);
		titleLayout.setTitle(mainActivity.getResources().getString(
				R.string.title4));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	private void initViews(View view) {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) view.findViewById(R.id.sidrbar);
		dialog = (TextView) view.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}
			}
		});
		mNetErrorView = view.findViewById(R.id.net_status_bar_top);
		sortListView = (ListView) view.findViewById(R.id.iphone_tree_view);
		sortListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SortModel sortModel = contactList.get(position - 1);
				String userJid = sortModel.getJID();
				String userName = sortModel.getName();
				startChatActivity(userJid, userName);
			}
		});
		LinearLayout headerLayout = (LinearLayout) LayoutInflater.from(
				mainActivity).inflate(R.layout.contacts_top_layout, null);
		sortListView.addHeaderView(headerLayout, null, false);
		sortListView.setEmptyView(view.findViewById(R.id.empty));
		adapter = new SortAdapter((Activity) mainActivity, contactList);
		sortListView.setAdapter(adapter);

		newFriends = (LinearLayout) headerLayout.findViewById(R.id.newfriend);
		newFriends.setOnClickListener(this);
		groups = (LinearLayout) headerLayout.findViewById(R.id.groups);
		groups.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mNetErrorView != null) {
			if (NetUtil.getNetworkState(mainActivity) == NetUtil.NETWORN_NONE)
				mNetErrorView.setVisibility(View.VISIBLE);
			else
				mNetErrorView.setVisibility(View.GONE);
		}
		qurryAllRoster();
	}

	/**
	 * 查询所有联系人
	 */
	public void qurryAllRoster() {
		Cursor cursor = mainActivity.getContentResolver().query(
				RosterProvider.CONTENT_URI, null, null, null, null);// 查询数据库获取
		Log.d("ouou", "查询所有联系人cursor:" + cursor.getCount());
		contactList.clear();
		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SortModel contactItem = new SortModel();
				contactItem.setJID(cursor.getString(cursor
						.getColumnIndexOrThrow(RosterConstants.JID)));
				contactItem.setName(cursor.getString(cursor
						.getColumnIndexOrThrow(RosterConstants.ALIAS)));
				Log.d("ouou",
						"JID:"
								+ cursor.getString(cursor
										.getColumnIndexOrThrow(RosterConstants.JID)));
				Log.d("ouou",
						"ALIAS:"
								+ cursor.getString(cursor
										.getColumnIndexOrThrow(RosterConstants.ALIAS)));
				contactList.add(contactItem);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// 根据a-z进行排序源数据
		filledData(contactList);
		Collections.sort(contactList, pinyinComparator);
		adapter.notifyDataSetChanged();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mainActivity.getContentResolver().unregisterContentObserver(
				mRosterObserver);
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<SortModel> filledData(List<SortModel> mList) {

		for (int i = 0; i < mList.size(); i++) {
			SortModel sortModel = mList.get(i);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(sortModel.getName());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}
		}
		return mList;

	}

	public void startChatActivity(String userJid, String userName) {

		Intent chatIntent = new Intent(mainActivity, ChatActivity.class);
		Uri userNameUri = Uri.parse(userJid);
		chatIntent.setData(userNameUri);
		chatIntent.putExtra(ChatActivity.INTENT_EXTRA_USERNAME, userName);
		mainActivity.startActivity(chatIntent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivTitleName:
			break;
		case R.id.newfriend:// 新朋友
			Log.d("ouou", "新朋友");
			break;
		case R.id.groups:// 群组
			Log.d("ouou", "群组");
			break;
		}
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
		switch (connectedState) {
		case XXService.CONNECTED:
			Log.d("ouou", "XXService.CONNECTED---已经连接xmpp");
			break;
		case XXService.CONNECTING:
			Log.d("ouou", "XXService.CONNECTING---正在连接");
			break;
		case XXService.DISCONNECTED:
			MyToast.showLong(mainActivity, reason);
			Log.d("ouou", "XXService.DISCONNECTED断开连接：" + reason);
			break;
		default:
			break;
		}
	}

	public void removeRosterItemDialog(final String JID, final String userName) {
		new AlertDialog.Builder(mainActivity)
				.setTitle(R.string.deleteRosterItem_title)
				.setMessage(
						getString(R.string.deleteRosterItem_text, userName, JID))
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mainActivity.getService().removeRosterItem(JID);
							}
						}).setNegativeButton(android.R.string.no, null)
				.create().show();
	}

	@Override
	public void onNetChange() {

		if (NetUtil.getNetworkState(mainActivity) == NetUtil.NETWORN_NONE) {
			MyToast.showShort(mainActivity, R.string.net_error_tip);
			mNetErrorView.setVisibility(View.VISIBLE);
		} else {
			mNetErrorView.setVisibility(View.GONE);
		}

	}

	private Handler mainHandler = new Handler();

	private class RosterObserver extends ContentObserver {
		public RosterObserver() {
			super(mainHandler);
		}

		public void onChange(boolean selfChange) {
			Logout.d(MainActivity.class, "RosterObserver.onChange: "
					+ selfChange);
			mainHandler.postDelayed(new Runnable() {
				public void run() {
					qurryAllRoster();
				}
			}, 100);
		}
	}

}
