package com.ouchat.fragment;

import android.app.Activity;
import android.content.ContentResolver;
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
import android.widget.ListView;

import com.ouchat.R;
import com.ouchat.activity.ChatActivity;
import com.ouchat.adapter.RecentChatAdapter;
import com.ouchat.ui.view.TitleLayout;
import com.ouchat.util.FragmentCallBack;
import com.ouchat.util.XMPPHelper;
import com.way.db.ChatProvider;
import com.way.db.ChatProvider.ChatConstants;

public class RecentChatFragment extends Fragment implements OnClickListener {

	private Handler mainHandler = new Handler();
	private ContentObserver mChatObserver = new ChatObserver();
	private ContentResolver mContentResolver;
	private ListView mListView;
	private RecentChatAdapter mRecentChatAdapter;
	private FragmentCallBack mFragmentCallBack;

	public static RecentChatFragment newInstance() {
		RecentChatFragment newFragment = new RecentChatFragment();
		// Bundle bundle = new Bundle();
		// newFragment.setArguments(bundle);
		// //bundle还可以在每个标签里传送数据
		return newFragment;

	}

	Activity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.activity = activity;
			mFragmentCallBack = (FragmentCallBack) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentResolver = getActivity().getContentResolver();
		mRecentChatAdapter = new RecentChatAdapter(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		mRecentChatAdapter.requery();
		mContentResolver.registerContentObserver(ChatProvider.CONTENT_URI,
				true, mChatObserver);
	}

	@Override
	public void onPause() {
		super.onPause();
		mContentResolver.unregisterContentObserver(mChatObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.recent_chat_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
		initTitle(view);
	}

	/**
	 * 标题的布局
	 */
	TitleLayout titleLayout;

	// 标题
	private void initTitle(View view) {
		titleLayout = (TitleLayout) view.findViewById(R.id.title);
		titleLayout
				.setTitle(activity.getResources().getString(R.string.title3));
	}

	private void initView(View view) {
		mListView = (ListView) view.findViewById(R.id.recent_listview);
		mListView.setEmptyView(view.findViewById(R.id.recent_empty));
		mListView.setAdapter(mRecentChatAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("ouou", "arg2:" + arg2);
				Cursor clickCursor = mRecentChatAdapter.getCursor();
				clickCursor.moveToPosition(arg2);
				String jid = clickCursor.getString(clickCursor
						.getColumnIndex(ChatConstants.JID));
				Uri userNameUri = Uri.parse(jid);
				Intent toChatIntent = new Intent(getActivity(),
						ChatActivity.class);
				toChatIntent.setData(userNameUri);
				String nickNameString = clickCursor.getString(clickCursor
						.getColumnIndex(ChatProvider.ChatConstants.NICKNAME));
				if (nickNameString != null) {
					toChatIntent.putExtra(ChatActivity.INTENT_EXTRA_USERNAME,
							nickNameString);
				} else {
					toChatIntent.putExtra(ChatActivity.INTENT_EXTRA_USERNAME,
							XMPPHelper.splitJidAndServer(jid));
				}

				startActivity(toChatIntent);

			}
		});

	}

	public void updateRoster() {
		mRecentChatAdapter.requery();
	}

	private class ChatObserver extends ContentObserver {
		public ChatObserver() {
			super(mainHandler);
		}

		public void onChange(boolean selfChange) {
			updateRoster();
			Log.d("ouou", "selfChange" + selfChange);
		}
	}

	@Override
	public void onClick(View v) {

	}

}
