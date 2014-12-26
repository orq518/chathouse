package com.ouchat.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.PopupWindow.OnDismissListener;

import com.ouchat.BasePopupWindow.onSubmitClickListener;
import com.ouchat.R;
import com.ouchat.popupwindow.NearByPopupWindow;
import com.ouchat.ui.view.HeaderLayout;
import com.ouchat.ui.view.HeaderLayout.HeaderStyle;
import com.ouchat.ui.view.HeaderLayout.onMiddleImageButtonClickListener;
import com.ouchat.ui.view.HeaderSpinner;
import com.ouchat.ui.view.HeaderSpinner.onSpinnerClickListener;
import com.ouchat.ui.view.SwitcherButton.SwitcherButtonState;
import com.ouchat.ui.view.SwitcherButton.onSwitcherButtonClickListener;

public class NearByActivity extends TabItemActivity {

	private HeaderLayout mHeaderLayout;
	private HeaderSpinner mHeaderSpinner;
	private NearByPeopleFragment mPeopleFragment;
	private NearByGroupFragment mGroupFragment;

	private NearByPopupWindow mPopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		initPopupWindow();
		initViews();
		initEvents();
		init();
	}

	@Override
	protected void initViews() {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.nearby_header);
//		mHeaderLayout.initSearch(new OnSearchClickListener());
		mHeaderSpinner = mHeaderLayout.setTitleNearBy("附近",
				new OnSpinnerClickListener(), "附近群组",
				0,
				new OnMiddleImageButtonClickListener(), "个人", "群组",
				new OnSwitcherButtonClickListener());
		mHeaderLayout.init(HeaderStyle.TITLE_NEARBY_PEOPLE);
	}

	@Override
	protected void initEvents() {

	}

	@Override
	protected void init() {
		mPeopleFragment = new NearByPeopleFragment(mApplication, this, this);
		mGroupFragment = new NearByGroupFragment(mApplication, this, this);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.nearby_layout_content, mPeopleFragment).commit();
	}

	private void initPopupWindow() {
		mPopupWindow = new NearByPopupWindow(this);
		mPopupWindow.setOnSubmitClickListener(new onSubmitClickListener() {

			@Override
			public void onClick() {
				mPeopleFragment.onManualRefresh();
			}
		});
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				mHeaderSpinner.initSpinnerState(false);
			}
		});
	}

	public class OnSpinnerClickListener implements onSpinnerClickListener {

		@Override
		public void onClick(boolean isSelect) {
			if (isSelect) {
				mPopupWindow
						.showViewTopCenter(findViewById(R.id.nearby_layout_root));
			} else {
				mPopupWindow.dismiss();
			}
		}
	}

//	public class OnSearchClickListener implements onSearchListener {
//
//		@Override
//		public void onSearch(EditText et) {
//			String s = et.getText().toString().trim();
//			if (TextUtils.isEmpty(s)) {
//				showCustomToast("请输入搜索关键字");
//				et.requestFocus();
//			} else {
//				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
//						.hideSoftInputFromWindow(NearByActivity.this
//								.getCurrentFocus().getWindowToken(),
//								InputMethodManager.HIDE_NOT_ALWAYS);
//				putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
//
//					@Override
//					protected void onPreExecute() {
//						super.onPreExecute();
//						mHeaderLayout.changeSearchState(SearchState.SEARCH);
//					}
//
//					@Override
//					protected Boolean doInBackground(Void... params) {
//						try {
//							Thread.sleep(2000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						return false;
//					}
//
//					@Override
//					protected void onPostExecute(Boolean result) {
//						super.onPostExecute(result);
//						mHeaderLayout.changeSearchState(SearchState.INPUT);
//						showCustomToast("未找到搜索的群");
//					}
//				});
//			}
//		}
//
//	}

	public class OnMiddleImageButtonClickListener implements
			onMiddleImageButtonClickListener {

		@Override
		public void onClick() {
//			mHeaderLayout.showSearch();
		}
	}

	public class OnSwitcherButtonClickListener implements
			onSwitcherButtonClickListener {

		@Override
		public void onClick(SwitcherButtonState state) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.setCustomAnimations(R.anim.fragment_fadein,
					R.anim.fragment_fadeout);
			switch (state) {
			case LEFT:
				mHeaderLayout.init(HeaderStyle.TITLE_NEARBY_PEOPLE);
				ft.replace(R.id.nearby_layout_content, mPeopleFragment)
						.commit();
				break;

			case RIGHT:
				mHeaderLayout.init(HeaderStyle.TITLE_NEARBY_GROUP);
				ft.replace(R.id.nearby_layout_content, mGroupFragment).commit();
				break;
			}
		}

	}

	@Override
	public void onBackPressed() {
//		if (mHeaderLayout.searchIsShowing()) {
//			clearAsyncTask();
//			mHeaderLayout.dismissSearch();
//			mHeaderLayout.clearSearch();
//			mHeaderLayout.changeSearchState(SearchState.INPUT);
//		} else {
			finish();
//		}
	}
}
