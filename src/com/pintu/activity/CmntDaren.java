package com.pintu.activity;

import android.content.Intent;
import android.os.Bundle;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.util.Preferences;

public class CmntDaren extends HeadSwitchActivity  {

	private int currentTabIndex = 0;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
			
		//打开保存索引的标签
		int savedIndex = this.getLastVisitIndex(Preferences.CMNT_INDEX);
		//根据保存的标签索引来对应打开
		this.switchActivity(savedIndex);

	}
	
	protected void onDestroy(){
        super.onDestroy();
        
        //保存退出时查看的标签页索引，以便下次进入时打开
        this.rememberLastVisitIndex(Preferences.CMNT_INDEX, currentTabIndex);
	}
	
	@Override
	public int[] initNavIcons() {
		int[] icons = { R.drawable.picdaren, R.drawable.cmtdaren};
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = { R.string.picdaren, R.string.cmtdaren};
		return txts;
	}

	@Override
	public Intent switchByIndex(int index) {
		Intent result = null;
		switch (index) {
		case 0:
			result = new Intent(this, PicDaren.class);
			currentTabIndex = 0;
			break;
		case 1:
			result = new Intent(this, CommentDaren.class);
			currentTabIndex = 1;
			break;		

		}
		return result;
	}



}
