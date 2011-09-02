package com.pintu.activity;

import android.content.Intent;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;

public class AndiAssets extends HeadSwitchActivity {

	@Override
	public int[] initNavIcons() {
		int[] icons = { R.drawable.assets, R.drawable.picutures,
				R.drawable.pintu, R.drawable.favorites, R.drawable.messages };
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = { R.string.assets, R.string.picutues, R.string.pintu,
				R.string.favorite, R.string.messages };
		return txts;
	}

	@Override
	public Intent switchByIndex(int index) {
		Intent result = null;
		switch (index) {
		case 0:
			result = new Intent(this, AndiWealth.class);
			break;
		case 1:
			result = new Intent(this, AndiPics.class);
			break;
		case 2:
			result = new Intent(this, AndiPintu.class);
			break;
		case 3:
			result = new Intent(this, AndiFavorite.class);
			break;
		case 4:
			result = new Intent(this, AndiMessage.class);
			break;
		// ...

		}
		return result;
	}

}
