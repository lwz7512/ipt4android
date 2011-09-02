package com.pintu.activity;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;

import android.content.Intent;

public class CommunityTrends extends HeadSwitchActivity {

	@Override
	public int[] initNavIcons() {
		int[] icons = { R.drawable.xuetang, R.drawable.event, R.drawable.daren,
				R.drawable.industry };
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = { R.string.xuetang, R.string.event, R.string.daren,
				R.string.industry };
		return txts;
	}

	@Override
	public Intent switchByIndex(int index) {
		Intent result = null;
		switch (index) {
		case 0:
			result = new Intent(this, CmntClassRoom.class);
			break;
		case 1:
			result = new Intent(this, CmntEvent.class);
			break;
		case 2:
			result = new Intent(this, CmntDaren.class);
			break;
		case 3:
			result = new Intent(this, CmntNews.class);
			break;
			// ...

		}
		return result;
	}

}
