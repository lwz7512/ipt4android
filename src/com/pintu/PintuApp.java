package com.pintu;


import com.pintu.api.PTApi;
import com.pintu.api.PTImpl;
import com.pintu.db.CacheDao;
import com.pintu.db.CacheImpl;
import com.pintu.tool.LazyImageLoader;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PintuApp extends Application {

	//应用上下文
	public static Context mContext;
	//远程访问接口
	public static PTApi mApi;
	//本地数据存储接口
	public static CacheDao dbApi;
	//本地设置存储
	public static SharedPreferences mPref;
	//图片加载器
	public static LazyImageLoader mImageLoader;
	
	//模拟登录用户
	public static String userID = "abcdefghijklmnop";
	
	
	
	public void onCreate(){
		super.onCreate();
		
		mContext = this.getApplicationContext();
		mApi = new PTImpl();	
		dbApi = new CacheImpl(this);
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mImageLoader  = new LazyImageLoader(this);
		
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
