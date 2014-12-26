package com.ouchat.ui.view;

import com.ouchat.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleLayout extends RelativeLayout {
	Context mContext;
	private TextView mTitleView;
	private ImageView mTitleRight, mTitleLeft;
	RelativeLayout titleLayout;

	public TitleLayout(Context context) {
		super(context);
		mContext = context;
		initTitleView();
	}

	public TitleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initTitleView();
	}

	// 标题
	private void initTitleView() {
		titleLayout=(RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.title_layout, null);
		mTitleView = (TextView) titleLayout.findViewById(R.id.ivTitleName);
		mTitleRight = (ImageView) titleLayout.findViewById(R.id.show_right_fragment_btn);
		mTitleLeft = (ImageView) titleLayout.findViewById(R.id.show_left_fragment_btn);
		this.addView(titleLayout);
	}

	public void setRightClickLinstener(OnClickListener listener) {
		mTitleRight.setOnClickListener(listener);
	}

	public void setLeftClickLinstener(OnClickListener listener) {
		mTitleLeft.setOnClickListener(listener);
	}

	public void setRightImageResource(int id) {
		mTitleRight.setVisibility(View.VISIBLE);
		mTitleRight.setImageResource(id);
	}

	public void setLeftImageResource(int id) {
		mTitleLeft.setVisibility(View.VISIBLE);
		mTitleLeft.setImageResource(id);
	}

	public void setTitle(String title) {
		mTitleView.setText(title);
	}
}
