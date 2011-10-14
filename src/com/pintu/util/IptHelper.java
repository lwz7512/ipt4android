package com.pintu.util;

import android.content.Context;

public class IptHelper {

	public static String getShortUserName(String userName) {
		String showName = userName;
		if (userName.contains("@")) {
			int atPos = userName.indexOf("@");
			showName = userName.substring(0, atPos);
		}
		return showName;
	}

	public static int dip2px(Context context, float dipValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dipValue * scale + 0.5f);

	}

	public static int px2dip(Context context, float pxValue){

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int)(pxValue / scale + 0.5f);

	}
	
	
}
