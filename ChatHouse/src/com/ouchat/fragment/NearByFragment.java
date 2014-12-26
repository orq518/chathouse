/**
 * 
 */
package com.ouchat.fragment;

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
import android.widget.TextView;

import com.ouchat.R;
import com.ouchat.activity.NearByActivity;
import com.ouchat.ui.view.TitleLayout;
import com.ouchat.util.FragmentCallBack;

/**
 * @ClassName: NearByFragment
 * @Description: TODO 附近
 * @author ou
 * @date 2014年11月21日 下午1:22:22
 * 
 */
public class NearByFragment extends Fragment implements OnClickListener {
	private static final String TAG = "TestFragment";
	private String hello;// = "hello android";
	private String defaultHello = "default value";
	/**
	 * 附近卖家
	 */
	TextView tv_nearby_people;

	public static NearByFragment newInstance() {
		NearByFragment newFragment = new NearByFragment();
		// Bundle bundle = new Bundle();
		// bundle.putString("hello", s);
		// newFragment.setArguments(bundle);
		return newFragment;

	}

	Activity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	View mainView;
	/**
	 * 标题的布局
	 */
	TitleLayout titleLayout;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ouou", "NearByFragment-----onCreateView");
		Bundle args = getArguments();
		hello = args != null ? args.getString("hello") : defaultHello;
		mainView = inflater.inflate(R.layout.nearby_fragment, container, false);
		initTitle();
		initViews();
		return mainView;

	}

	

	// 标题
	private void initTitle() {
		titleLayout = (TitleLayout) mainView.findViewById(R.id.title);
		titleLayout
				.setTitle(activity.getResources().getString(R.string.title2));
	}

	private void initViews() {
		tv_nearby_people = (TextView) mainView.findViewById(R.id.nearby_people);
		tv_nearby_people.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.nearby_people:
			Intent intent = new Intent(activity,NearByActivity.class);
			activity.startActivity(intent);
			break;
		}
	}
}