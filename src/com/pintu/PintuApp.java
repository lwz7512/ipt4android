package com.pintu;


import com.pintu.api.PTApi;
import com.pintu.api.PTImpl;
import com.pintu.tool.LazyImageLoader;

import android.app.Application;
import android.content.Context;

public class PintuApp extends Application {

	public static Context mContext;
	public static PTApi mApi;
	public static LazyImageLoader mImageLoader;
	
	
	public void onCreate(){
		super.onCreate();
		
		mContext = this.getApplicationContext();
		mApi = new PTImpl();
		mImageLoader = new LazyImageLoader();
		
	}
	
	public void onLowMemory(){
		super.onLowMemory();
		//Seemly, no need to imple...
	}

}
