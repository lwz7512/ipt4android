package com.pintu.activity;


import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.util.Preferences;

import android.content.Intent;
import android.os.Bundle;

public class HotPicClassic extends HeadSwitchActivity {

	//当前标签页的索引
	private int currentTabIndex = 0;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//子活动顶部都有个刷新按钮，可以用来刷新数据，默认是显示缓存数据的
		this.showRefreshBtn();
		//打开保存索引的标签
		int savedIndex = this.getLastVisitIndex(Preferences.HOT_INDEX);
		//根据保存的标签索引来对应打开
		this.switchActivity(savedIndex);

	}
	
	protected void onDestroy(){
        super.onDestroy();
        
        //保存退出时查看的标签页索引，以便下次进入时打开
        this.rememberLastVisitIndex(Preferences.HOT_INDEX, currentTabIndex);
	}

	
	@Override
	public int[] initNavIcons() {
		int[] icons = {
				R.drawable.todayhot,
				R.drawable.hisclassic};		
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = {
				R.string.todayhotpic,
				R.string.historyclassic
		};
		return txts;
	}
	
	@Override
	public Intent switchByIndex(int index) {
		Intent result = null;
		switch(index){
		case 0:
			result = new Intent(this,TodayHotPic.class);
			currentTabIndex = 0;
			break;
		case 1:
			result = new Intent(this,ClassicWorks.class);
			currentTabIndex = 1;
			break;
		//...
			
		}
		return result;

	}
	

}
