package com.pintu.upgrade;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.pintu.R;

/**
 * 应用升级辅助类，主要是由MsgService来调用，两者实现交互完成升级下载安装
 * 2011/12/25
 * 
 * @author lwz
 *
 */
public class UpdateManager {

	private static final String TAG = "UpdateManager";

	private Context mContext;
	private Handler mHandler;

	//检查新版本
	public static final int UPGRADE_CHECK = 3;
	//弹出对话框
	public static final int POPUP_DIALOG = 4;
	//下载安装包
	public static final int UPDATE_CLIENT = 5;
	//下载完成
	public static final int DOWN_OVER = 6;

	

	// 下载包以及配置文件的服务器路径
	private String mDownloadURL;
	
	// 更新信息来自服务器xml配置文件
	//这个数据还要被HomeGallery接入
	public UpdateInfo info;
	

	public UpdateManager(Context context, Handler handler) {
		this.mContext = context;		
		this.mHandler = handler;
				
	}
	

	/**
	 * 外部接口，让主服务来调用
	 * @param configURL 服务器端的配置文件，保存了更新包的信息
	 */
	public void checkUpdateInfo(String configURL) {
		mDownloadURL = configURL;
		try {
			// 包装成url的对象
			URL url = new URL(mDownloadURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			InputStream is = conn.getInputStream();
			// 保存更新文件信息
			info = UpdateInfoParser.getUpdataInfo(is);

			String versionName = getVersionName();
			if (info.getVersion().equals(versionName)) {
				Log.i(TAG, "the same version, no need to upgrade!");
			} else if (Float.valueOf(info.getVersion()) > Float
					.valueOf(versionName)) {
				// 只有版本增加时才更新
				Log.i(TAG,
						"there has a new version, to upgrade:  "
								+ info.getVersion());
				mHandler.sendEmptyMessage(POPUP_DIALOG);
			}
		} catch (Exception e) {
			// 配置文件不存在，或者网络不给力
			Log.e(TAG, "Check update config file ERROR: "+mDownloadURL);
			e.printStackTrace();
		}
	}



	/*
	 * 
	 * 弹出对话框通知用户更新程序
	 * 
	 * 弹出对话框的步骤： 1.创建alertDialog的builder. 2.要给builder设置属性, 对话框的内容,样式,按钮
	 * 3.通过builder 创建一个对话框 4.对话框show()出来
	 */
	public void showUpdataDialog() {
		
		AlertDialog.Builder builer = new Builder(mContext);
		builer.setTitle(mContext.getText(R.string.newversion)+" "+info.getVersion());
		builer.setMessage(info.getDescription());
		// 当点确定按钮时从服务器上下载 新的apk 然后安装
		builer.setPositiveButton(mContext.getText(R.string.yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.i(TAG, "download update apk...");
						//启动下载任务
						mHandler.sendEmptyMessage(UPDATE_CLIENT);						
						//关闭对话框
						dialog.dismiss();
					}
				});
		// 当点取消按钮时进行登录
		builer.setNegativeButton(mContext.getText(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//关闭对话框
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}



	/*
	 * 获取当前程序的版本号
	 */
	private String getVersionName() {
		// 获取packagemanager的实例
		PackageManager packageManager = mContext.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(mContext.getPackageName(),
					0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "NameNotFoundException for getPackageInfo: "+mContext.getPackageName());
		}
		if (packInfo != null) {
			return packInfo.versionName;
		} else {
			return "1.0";
		}
	}

}
