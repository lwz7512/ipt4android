package com.ybcx;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.ybcx.api.PTApi;
import com.ybcx.api.PTImpl;
import com.ybcx.db.CacheDao;
import com.ybcx.db.CacheImpl;
import com.ybcx.tool.LazyImageLoader;
import com.ybcx.util.Preferences;

public class PintuApp extends Application {

	private static String TAG = "PintuApp";
	
	public static String LOCAL_VERSION = "Local version";
	public static String MARKET_VERSION = "Market version";
	
//	public static String VERSION_STATE = MARKET_VERSION;
	public static String VERSION_STATE = LOCAL_VERSION;

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
	
	/**
	 * 本地版，从服务器上自动更新，而市场版只能通过Android Market更新
	 * 所以如果是本地版，就启动版本检测，从服务器上拿版本配置文件信息
	 * 然后分析版本号，当前应用比较，是否该进行升级
	 * 
	 * @return
	 */
	public static boolean isLocalVersion(){
		if(VERSION_STATE.equals(LOCAL_VERSION)){
			return true;
		}else if(VERSION_STATE.equals(MARKET_VERSION)){
			return false;
		}
		return false;
	}

	public static void cancelNotification() {
		mNotificationManager.cancel(R.string.messages);
		mNotificationManager.cancel(R.string.dnldover);
	}
	
	public static boolean isNetworkAvailable() {
		boolean flag = false;
		try{
			//需要添加uses-permission, android.permission.ACCESS_NETWORK_STATE			
			ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity == null) {
				flag = false;
			} else {
				flag = connectivity.getActiveNetworkInfo().isAvailable();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}

}
