package com.pintu;


import com.pintu.api.PTApi;
import com.pintu.api.PTImpl;
import com.pintu.tool.LazyImageLoader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PintuApp extends Application {

	public static Context mContext;
	public static PTApi mApi;
	public static LazyImageLoader mImageLoader;
	public static SharedPreferences mPref;
	
	//模拟登录用户
	public static String userID = "abcdefghijklmnop";
	
	
	public void onCreate(){
		super.onCreate();
		
		mContext = this.getApplicationContext();
		mApi = new PTImpl();
		mImageLoader = new LazyImageLoader();
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	public void onLowMemory(){
		super.onLowMemory();
		//Seemly, no need to imple...
	}

}
