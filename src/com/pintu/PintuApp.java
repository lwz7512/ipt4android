package com.pintu;


import com.pintu.api.PTApi;
import com.pintu.api.PTImpl;
import com.pintu.tool.LazyImageLoader;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

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
		//保存一个上次登录时间，哪里用呢？
		rememberLastLogin();
	}
	
	private void rememberLastLogin(){
		long lastLogin = mPref.getLong(Preferences.LAST_LOGIN_TIME, 0);
		if(lastLogin==0){
			long firstLogin = DateTimeHelper.getNowTime();
			mPref.edit().putLong(Preferences.LAST_LOGIN_TIME, firstLogin);
		}
	}
	
	public void onLowMemory(){
		super.onLowMemory();
		//Seemly, no need to imple...
	}

}
