package com.ybcx.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ybcx.R;
import com.ybcx.activity.base.HeadSwitchActivity;
import com.ybcx.util.Preferences;

/**
 * 主模块“俺滴”对应的活动容器 用于对5个子活动的切换，并保存最后一次切换的索引
 * 
 * @author lwz
 * 
 */
public class AndiAssets extends HeadSwitchActivity {

	//当前标签页的索引
	private int currentTabIndex = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 子活动顶部都有个刷新按钮，可以用来刷新数据，默认是显示缓存数据的
		this.showRefreshBtn();
		// 打开上次退出时保存索引的标签
		int savedIndex = this.getLastVisitIndex(Preferences.MYASSETS_INDEX);
		
		//检查下是否是由状态栏的消息通知来启动
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			//不出意外就是3了，打开我的消息
			savedIndex = extras.getInt(Preferences.MYASSETS_INDEX);
		}
		// 根据保存的标签索引来对应打开
		this.switchActivity(savedIndex);

	}

	//按下返回按钮的动作
	protected void goWhere(){
		Intent it = new Intent();
		it.setClass(this, HomeGallery.class);
		startActivity(it);
	}
	
	
	protected void onDestroy() {
		super.onDestroy();

		// 保存退出时查看的标签页索引，以便下次进入时打开
		this.rememberLastVisitIndex(Preferences.MYASSETS_INDEX, currentTabIndex);
	}

	@Override
	public int[] initNavIcons() {
		int[] icons = { R.drawable.wealth, R.drawable.home_post_pic,
				R.drawable.favorites, R.drawable.messages };
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = { R.string.assets, R.string.picutues,R.string.favorite, R.string.messages };
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
			result = new Intent(this, AndiFavorite.class);
			currentTabIndex = 2;
			break;
		case 3:
			result = new Intent(this, AndiMessage.class);
			currentTabIndex = 3;
			break;		

		}
		return result;
	}

}
