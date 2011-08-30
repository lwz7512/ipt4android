package com.pintu.activity;


import com.pintu.R;

import android.content.Intent;
import android.os.Bundle;

public class HotPicStory extends HeadSwitchActivity {

	@Override
	public int[] initNavIcons() {
		int[] icons = {
				android.R.drawable.ic_menu_today,
				android.R.drawable.ic_menu_recent_history};		
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
			break;
		case 1:
			result = new Intent(this,HistoryClassicStory.class);
			break;
		//...
			
		}
		return result;

	}
	

}
