package com.pintu.activity;

import android.content.Intent;
import android.os.Bundle;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;

/**
 * 主模块“俺滴”对应的活动容器
 * 用于对5个子活动的切换，并保存最后一次切换的索引
 * @author lwz
 *
 */
public class AndiAssets extends HeadSwitchActivity {

	private int currentTabIndex = 0;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//子活动顶部都有个刷新按钮，可以用来刷新数据，默认是显示缓存数据的
		this.showRefreshBtn();
	}
	
	protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //保存退出时查看的标签页索引，以便下次进入时打开
        outState.putInt(TABINDEX, currentTabIndex);
	}
	
	
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
			currentTabIndex = 0;
			break;
		case 1:
			result = new Intent(this, AndiPics.class);
			currentTabIndex = 1;
			break;
		case 2:
			result = new Intent(this, AndiPintu.class);
			currentTabIndex = 2;
			break;
		case 3:
			result = new Intent(this, AndiFavorite.class);
			currentTabIndex = 3;
			break;
		case 4:
			result = new Intent(this, AndiMessage.class);
			currentTabIndex = 4;
			break;
		// ...

		}
		return result;
	}

}
