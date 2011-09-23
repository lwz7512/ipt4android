package com.pintu.activity;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.util.Preferences;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 图片详情中，点击作者信息的“>”图标打开的活动容器
 * 当前活动组内的子活动，均为自取数据，按时间间隔自查
 * 时间间隔设为10秒
 * 该主活动不提供刷新按钮
 * @author lwz
 * 
 */
public class TadiAssets extends HeadSwitchActivity {
	private static final String TAG = "TadiAssets";

	// 当前用户
	private String currentUser;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			// 如果没传用户则关闭
			finish();
			return;
		}
		// 保存下来，当后面打开子活动时，传递进去
		currentUser = extras.getString("userId");

		// 默认都是打开第一个标签
		this.switchActivity(0);
	}

	@Override
	public int[] initNavIcons() {
		int[] icons = { R.drawable.tadi, R.drawable.picutures, R.drawable.pintu };
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = { R.string.baseinfo, R.string.picutues, R.string.pintu, };
		return txts;
	}

	@Override
	public Intent switchByIndex(int index) {
		Intent result = null;
		switch (index) {
		case 0:
			result = new Intent(this, TadiProfile.class);
			break;
		case 1:
			result = new Intent(this, TadiPics.class);
			break;
		case 2:
			result = new Intent(this, TadiPintu.class);
			break;
		}
		// 传递给子活动用户
		result.putExtra("userId", currentUser);

		return result;
	}

}
