package com.ouchat.activity;

import android.os.Bundle;

import com.ouchat.BaseActivity;
import com.ouchat.R;
import com.ouchat.adapter.UserGuiDeAdapter;
import com.ouchat.ui.view.ScrollViewPager;

public class UserGuiDeActivity extends BaseActivity {
	private ScrollViewPager mSvpPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userguide);
		initViews();
		initEvents();
	}

	@Override
	protected void initViews() {
		mSvpPager = (ScrollViewPager) findViewById(R.id.userguide_svp_pager);
		mSvpPager.setEnableTouchScroll(true);
		mSvpPager.setAdapter(new UserGuiDeAdapter(this));
	}

	@Override
	protected void initEvents() {

	}
}
