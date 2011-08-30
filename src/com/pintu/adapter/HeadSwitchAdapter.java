package com.pintu.adapter;


import com.pintu.R;

import android.content.Context;
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
	
    public HeadSwitchAdapter(Context c,int[] picIds,int[] txts,int height,int selResId) { 
        mContext = c; 
        this.selResId=selResId;
        imgItems=new TextView[picIds.length];
        for(int i=0;i<picIds.length;i++){
        	imgItems[i] = new TextView(mContext); 
        	imgItems[i].setLayoutParams(
        			new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, height)); 
        	imgItems[i].setPadding(2, 2, 2, 2); 
        	imgItems[i].setText(txts[i]); 
        	imgItems[i].setTextColor(R.drawable.tweet_color);
        	imgItems[i].setGravity(Gravity.CENTER_VERTICAL);
        	if(picIds.length>2){
        		//如果大于2个菜单就将图标置于顶部
        		imgItems[i].setCompoundDrawablesWithIntrinsicBounds(0, picIds[i], 0, 0);        		
        		imgItems[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        	}else{
        		//如果只有2个菜单就将图标置于左边
        		imgItems[i].setCompoundDrawablesWithIntrinsicBounds(picIds[i], 0, 0, 0);        		
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
        	imageView=imgItems[position];
        } else { 
            imageView = (TextView) convertView; 
        } 
        return imageView; 
    } 
    
    /** 
     * 设置选中的效果 
     */  
    public void SetFocus(int index)  
    {  
        for(int i=0;i<imgItems.length;i++)  
        {  
            if(i!=index)  
            {  
            	imgItems[i].setBackgroundResource(0);//恢复未选中的样式
            }  
        }  
        imgItems[index].setBackgroundResource(selResId);//设置选中的样式
    }      
    
    
} 