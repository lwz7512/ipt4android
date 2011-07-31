package com.pintu.tool;

import com.pintu.PintuApp;
import com.pintu.R;

import android.graphics.Bitmap;


public interface ImageCache {
	
	public static Bitmap mDefaultBitmap = ImageManager.drawableToBitmap(PintuApp.mContext.getResources().getDrawable(R.drawable.user_default_photo));
	
	public Bitmap get(String url);
	public void put(String url, Bitmap bitmap);
	
}
