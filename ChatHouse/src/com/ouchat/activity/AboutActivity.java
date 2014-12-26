package com.ouchat.activity;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.ouchat.R;
import com.ouchat.ui.swipeback.SwipeBackActivity;
import com.ouchat.ui.view.ChangeLog;

public class AboutActivity extends SwipeBackActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView tv = (TextView) findViewById(R.id.app_information);
		Linkify.addLinks(tv, Linkify.ALL);
	}

	public void showChangeLog(View view) {
		new ChangeLog(this).getFullLogDialog().show();
	}
}
