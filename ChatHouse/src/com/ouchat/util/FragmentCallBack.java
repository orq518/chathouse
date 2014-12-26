package com.ouchat.util;

import com.ouchat.activity.MainActivity;
import com.way.service.XXService;

public interface FragmentCallBack {
	public XXService getService();

	public MainActivity getMainActivity();
}
