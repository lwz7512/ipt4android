package com.ybcx.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ybcx.R;
import com.ybcx.activity.base.HeadSwitchActivity;
import com.ybcx.util.Preferences;

public class MarketExchange extends HeadSwitchActivity {

	private int currentTabIndex = 0;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//子活动顶部都有个刷新按钮，可以用来刷新数据，默认是显示缓存数据的
		this.showRefreshBtn();
		//打开保存索引的标签
		int savedIndex = this.getLastVisitIndex(Preferences.MARKET_INDEX);
		//根据保存的标签索引来对应打开
		this.switchActivity(savedIndex);

	}

	protected void onDestroy(){
        super.onDestroy();
        
        //保存退出时查看的标签页索引，以便下次进入时打开
        this.rememberLastVisitIndex(Preferences.MARKET_INDEX, currentTabIndex);
	}

	
	@Override
	public int[] initNavIcons() {
		int[] icons = {R.drawable.market_shopping64, R.drawable.market_note64};
		return icons;
	}

	@Override
	public int[] initNavTxts() {
		int[] txts = {R.string.marketshopping, R.string.marketnote};
		return txts;
	}
	
	@Override
	public Intent switchByIndex(int index) {
		Intent result = null;
		switch(index){
		case 0:
			result = new Intent(this,MarketShopping.class);
			currentTabIndex = 0;
			break;
		case 1:
			result = new Intent(this,MarketNote.class);
			currentTabIndex = 1;
			break;
		//...
			
		}
		return result;
	}


}
