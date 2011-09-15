package com.pintu;


import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pintu.api.PTApi;
import com.pintu.api.PTImpl;
import com.pintu.db.CacheDao;
import com.pintu.db.CacheImpl;
import com.pintu.tool.LazyImageLoader;

public class PintuApp extends Application {

	private static String TAG = "PintuApp";
	
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
	
	public static NotificationManager mNotificationManager;
	
	//模拟登录用户
	private static String userID = "a053beae20125b5b";
	//客服用户
	private static String kefuID = "b8931b314c24dca4";
	

	
	public void onCreate(){
		super.onCreate();
		
		mContext = this.getApplicationContext();
		mApi = new PTImpl(getUser());	
		dbApi = new CacheImpl(this);
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mImageLoader  = new LazyImageLoader(this);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
	}
	
	
	//获得本地登录用户
	public static String getUser() {
		return userID;
	}

	//获得客服用户
	public static String getKefu(){
		return kefuID;
	}
	
	
	public static void cancelNotification(){
		mNotificationManager.cancel(R.string.messages);
	}

}
