package com.ouchat.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ouchat.R;
import com.ouchat.activity.AboutActivity;
import com.ouchat.activity.FeedBackActivity;
import com.ouchat.activity.LoginActivity;
import com.ouchat.activity.SettingActivity;
import com.ouchat.ui.view.CustomDialog;
import com.ouchat.ui.view.TitleLayout;
import com.ouchat.util.FragmentCallBack;
import com.way.service.XXService;

/**
 * 我的界面
 * 
 * @ClassName: MineFragment
 * @Description: TODO
 * @author ou
 * @date 2014年12月3日 下午4:05:15
 * 
 */
public class MineFragment extends Fragment implements OnClickListener {
	private TextView mSetting;

	private FragmentCallBack mFragmentCallBack;
	Context mContext;

	public static MineFragment newInstance() {
		MineFragment newFragment = new MineFragment();
		// Bundle bundle = new Bundle();
		// bundle.putString("hello", s);
		// newFragment.setArguments(bundle);
		return newFragment;

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		try {
			mFragmentCallBack = (FragmentCallBack) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	View mainView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return mainView = inflater.inflate(R.layout.mine_fragment, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initTitle();
		mSetting = (TextView) view.findViewById(R.id.setting);
		mSetting.setOnClickListener(this);
	}

	/**
	 * 标题的布局
	 */
	TitleLayout titleLayout;

	// 标题
	private void initTitle() {
		titleLayout = (TitleLayout) mainView.findViewById(R.id.title);
		titleLayout
				.setTitle(mContext.getResources().getString(R.string.title5));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private Dialog mExitDialog;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set_feedback:
			startActivity(new Intent(getActivity(), FeedBackActivity.class));
			break;
		case R.id.set_about:
			startActivity(new Intent(getActivity(), AboutActivity.class));
			break;
		case R.id.btnCancel:
			if (mExitDialog != null && mExitDialog.isShowing())
				mExitDialog.dismiss();
			break;
		case R.id.btn_exit_comfirm:
			XXService service = mFragmentCallBack.getService();
			if (service != null) {
				service.logout();// 注销
				service.stopSelf();// 停止服务
			}
			if (mExitDialog.isShowing()) {
				mExitDialog.cancel();
			}
			getActivity().finish();
			break;
		case R.id.accountSetting:
			logoutDialog();
			break;
		case R.id.setting:
			startActivity(new Intent(mContext, SettingActivity.class));
			break;
		default:
			break;
		}
	}

	public void logoutDialog() {
		new CustomDialog.Builder(getActivity())
				.setTitle(getActivity().getString(R.string.open_switch_account))
				.setMessage(
						getActivity().getString(
								R.string.open_switch_account_msg))
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								XXService service = mFragmentCallBack
										.getService();
								if (service != null) {
									service.logout();// 注销
								}
								dialog.dismiss();
								startActivity(new Intent(getActivity(),
										LoginActivity.class));
								getActivity().finish();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create().show();
	}
}
