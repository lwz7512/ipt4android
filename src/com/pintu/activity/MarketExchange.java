package com.pintu.activity;

import android.content.Intent;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;

public class MarketExchange extends HeadSwitchActivity {

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
			break;
		case 1:
			result = new Intent(this,MarketNote.class);
			break;
		//...
			
		}
		return result;
	}


}
