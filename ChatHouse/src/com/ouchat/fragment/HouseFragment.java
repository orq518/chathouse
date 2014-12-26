/**
 * 
 */
package com.ouchat.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ouchat.R;
import com.ouchat.baidumap.OverlayMapActivity;
import com.ouchat.expandtabview.ExpandTabView;
import com.ouchat.expandtabview.ViewLeft;
import com.ouchat.expandtabview.ViewMiddle;
import com.ouchat.expandtabview.ViewRight;
import com.ouchat.ui.view.TitleLayout;
import com.ouchat.ui.xlistview.MsgListView;

/**
 * @ClassName: NearByFragment
 * @Description: TODO 附近
 * @author ou
 * @date 2014年11月21日 下午1:22:22
 * 
 */
@SuppressLint("NewApi")
public class HouseFragment extends Fragment {

	public static HouseFragment newInstance() {
		HouseFragment newFragment = new HouseFragment();
		// Bundle bundle = new Bundle();
		// bundle.putString("hello", s);
		// newFragment.setArguments(bundle);
		return newFragment;

	}

	private ExpandTabView expandTabView;
	private ArrayList<View> mViewArray;
	private ViewLeft viewLeft;
	private ViewMiddle viewMiddle;
	private ViewRight viewRight;
	Context mContext;
	/**
	 * 内容的listview
	 */
	private MsgListView mListView;
	/**
	 * 数据
	 */
	ArrayList<String> dataList = new ArrayList<String>();
	View mainView;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ouou", "HouseFragment-----onCreateView");
		mainView = inflater.inflate(R.layout.house_fragment, container, false);
		initTitle();
		initView();
		initVaule();
		initListener();
		return mainView;

	}

	/**
	 * 标题的布局
	 */
	TitleLayout titleLayout;

	// 标题
	private void initTitle() {
		titleLayout = (TitleLayout) mainView.findViewById(R.id.title);
		titleLayout
				.setTitle(mContext.getResources().getString(R.string.title1));
		titleLayout.setRightImageResource(R.drawable.btn_map_selector);
		OnClickListener rightListener = new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(mContext, OverlayMapActivity.class);
				mContext.startActivity(intent);
			}
		};
		titleLayout.setRightClickLinstener(rightListener);
	}

	public void setTitle(String title) {
		titleLayout.setTitle(title);
	}

	private void initView() {

		expandTabView = (ExpandTabView) mainView
				.findViewById(R.id.expandtab_view);
		viewLeft = new ViewLeft(mContext);
		viewMiddle = new ViewMiddle(mContext);
		viewRight = new ViewRight(mContext);
		mListView = (MsgListView) mainView.findViewById(R.id.listView);
		for (int i = 0; i < 10; i++) {
			dataList.add("" + i);
		}
		MyAdapter adapter = new MyAdapter();
		mListView.setAdapter(adapter);
	}

	private void initVaule() {
		mViewArray = new ArrayList<View>();
		mViewArray.add(viewLeft);
		mViewArray.add(viewMiddle);
		mViewArray.add(viewRight);
		ArrayList<String> mTextArray = new ArrayList<String>();
		mTextArray.add("距离");
		mTextArray.add("区域");
		mTextArray.add("距离");
		expandTabView.setValue(mTextArray, mViewArray);
		expandTabView.setTitle(viewLeft.getShowText(), 0);
		expandTabView.setTitle(viewMiddle.getShowText(), 1);
		expandTabView.setTitle(viewRight.getShowText(), 2);

	}

	private void initListener() {

		viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {

			@Override
			public void getValue(String distance, String showText) {
				onRefresh(viewLeft, showText);
			}
		});

		viewMiddle.setOnSelectListener(new ViewMiddle.OnSelectListener() {

			@Override
			public void getValue(String showText) {
				onRefresh(viewMiddle, showText);

			}
		});

		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {

			@Override
			public void getValue(String distance, String showText) {
				onRefresh(viewRight, showText);
			}
		});

	}

	private void onRefresh(View view, String showText) {

		expandTabView.onPressBack();
		int position = getPositon(view);
		if (position >= 0 && !expandTabView.getTitle(position).equals(showText)) {
			expandTabView.setTitle(showText, position);
		}
		Toast.makeText(mContext, showText, Toast.LENGTH_SHORT).show();

	}

	private int getPositon(View tView) {
		for (int i = 0; i < mViewArray.size(); i++) {
			if (mViewArray.get(i) == tView) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("ouou", "house---》onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.d("ouou", "house---》onDestroyView");
	}

	// public void onBackPressed() {
	//
	// if (!expandTabView.onPressBack()) {
	// }
	// }

	class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.house_item, null);
			}
			return convertView;
		}
	}
}