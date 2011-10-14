package com.pintu.adapter;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.util.IptHelper;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author GV
 * 
 */
public class HeadSwitchAdapter extends BaseAdapter {

	private Context mContext;
	private TextView[] imgItems;
	private int selResId;
	//计算屏幕大小
	private DisplayMetrics  dm = new DisplayMetrics(); 


	public HeadSwitchAdapter(Context c, int[] picIds, int[] txts, int selResId) {
		mContext = c;
		this.selResId = selResId;
		//获得屏幕尺寸
		((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm); 
		int cellWidth = dm.widthPixels/picIds.length;
		imgItems = new TextView[picIds.length];
		for (int i = 0; i < picIds.length; i++) {
			imgItems[i] = new TextView(mContext);
			//这个高度必须写成与背景图相同的高度，否则选中时两种状态有偏差
			int footerHeightPixels = IptHelper.dip2px(PintuApp.mContext, 50);
			GridView.LayoutParams glp = new GridView.LayoutParams(cellWidth, footerHeightPixels);
			imgItems[i].setLayoutParams(glp);
			imgItems[i].setCompoundDrawablePadding(2);
			imgItems[i].setText(txts[i]);
			//使用纯黑色字体
			imgItems[i].setTextColor(0xFF000000);
			imgItems[i].setGravity(Gravity.CENTER);
			
			if (picIds.length > 2) {
				// 如果大于2个菜单就将图标置于顶部
				imgItems[i].setCompoundDrawablesWithIntrinsicBounds(0,
						picIds[i], 0, 0);
				imgItems[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			} else {
				// 如果只有2个菜单就将图标置于左边
				imgItems[i].setCompoundDrawablesWithIntrinsicBounds(picIds[i],
						0, 0, 0);
				imgItems[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			}
		}
	}

	public int getCount() {
		return imgItems.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TextView imageView;
		if (convertView == null) {
			imageView = imgItems[position];
		} else {
			imageView = (TextView) convertView;
		}
		return imageView;
	}

	/**
	 * 设置选中的效果
	 */
	public void SetFocus(int index) {
		for (int i = 0; i < imgItems.length; i++) {
			if (i != index) {
				// 恢复未选中的样式
				imgItems[i].setBackgroundResource(0);
				if(imgItems.length>2){
					//重新设置图标离顶部距离
					imgItems[i].setPadding(0, 4, 0, 0);					
				}else{
					//重新设置图标与左边的距离
					imgItems[i].setPadding(20, 0, 0, 0);	
				}
				//恢复默认的纯黑色字体
				imgItems[i].setTextColor(0xFF000000);
			}
		}
		//设置选中的样式
		imgItems[index].setBackgroundResource(selResId);
		//使用纯白色字体ARGB
		imgItems[index].setTextColor(0xFFFFFFFF);
		if(imgItems.length>2){
			//重新设置图标离顶部距离
			imgItems[index].setPadding(0, 4, 0, 0);			
		}else{
			//重新设置图标与左边的距离
			imgItems[index].setPadding(20, 0, 0, 0);	
		}
	}

}