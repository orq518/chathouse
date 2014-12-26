package com.ouchat.dialog;

import android.app.Dialog;
import android.content.Context;

import com.ouchat.R;

public class NearByFilterDialog extends Dialog{

	public NearByFilterDialog(Context context) {
		super(context, R.style.Popup_Animation_DownScale);
		setContentView(R.layout.include_dialog_nearby_filter);
	}
}
