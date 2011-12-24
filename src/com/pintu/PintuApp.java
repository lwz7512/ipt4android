package com.pintu;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.pintu.api.PTApi;
import com.pintu.api.PTImpl;
import com.pintu.db.CacheDao;
import com.pintu.db.CacheImpl;
import com.pintu.tool.LazyImageLoader;
import com.pintu.util.Preferences;

public class PintuApp extends Application {

	private static String TAG = "PintuApp";

	// 应用上下文
	public static Context mContext;
	// 远程访问接口
	public static PTApi mApi;
	// 本地数据存储接口
	public static CacheDao dbApi;
	// 本地设置存储
	public static SharedPreferences mPref;
	// 图片加载器
	public static LazyImageLoader mImageLoader;

	public static NotificationManager mNotificationManager;


	public void onCreate() {
		super.onCreate();

		//系统级的工具准备
		mContext = this.getApplicationContext();
		mPref = this.getSharedPreferences(TAG, 0);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//应用级工具准备
		mImageLoader = new LazyImageLoader(this);
		
		//业务级工具准备
		mApi = new PTImpl(getUser());
		dbApi = new CacheImpl(this);
		
	}


	// 登录成功记录用户
	public static void rememberUser(String userId) {
		mApi.updateUser(userId);
		mPref.edit().putString(Preferences.LOGON_USERID, userId).commit();
	}
	
	public static boolean isLoggedin(){
		return getUser().equals("")?false:true;
	}

	// 获得本地登录用户
	public static String getUser() {
		return mPref.getString(Preferences.LOGON_USERID, "");
	}


	// 获得客服用户
	public static String getKefu() {
		return Preferences.KEFU_USERID;
	}

	public static void cancelNotification() {
		mNotificationManager.cancel(R.string.messages);
	}

}
