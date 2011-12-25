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
	//下载安装包
	public static final int UPDATE_CLIENT = 4;
	//下载完成
	public static final int DOWN_OVER = 5;

	// 下载包安装路径
	private String savePath;
	private static final String saveFileName = "PintuMain-release.apk";

	// 下载包以及配置文件的服务器路径
	private String mDownloadURL;
	// 更新信息来自服务器xml配置文件
	private UpdateInfo info;
	

	public UpdateManager(Context context, Handler handler) {
		this.mContext = context;
		this.mHandler = handler;

		savePath = mContext.getFilesDir() + "/" + saveFileName;
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
				// 弹出提示
				showUpdataDialog();
			}
		} catch (Exception e) {
			// 待处理
			Log.i(TAG, "check update config error!");
			e.printStackTrace();
		}
	}

	/**
	 * 外部接口，让主服务来调用
	 */
	public void downloadApk() {
		URL url = null;
		HttpURLConnection conn = null;
		InputStream is;		
		BufferedOutputStream out = null;
		byte[] buffer = new byte[1024];
		int len;
		
		try {
			url = new URL(info.getApkurl());
			if(url!=null)
			conn = (HttpURLConnection) url.openConnection();
			if(conn!=null){
				conn.setConnectTimeout(5000);
				is = conn.getInputStream();	
				BufferedInputStream bis = new BufferedInputStream(is);				
				out = new BufferedOutputStream(
	                    mContext.openFileOutput(savePath, Context.MODE_PRIVATE));		
				while ((len = bis.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.close();
				bis.close();
				is.close();
				//下载结束
				mHandler.sendEmptyMessage(DOWN_OVER);
			}			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {			
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
	private void showUpdataDialog() {
		AlertDialog.Builder builer = new Builder(mContext);
		builer.setTitle(mContext.getText(R.string.newversion));
		builer.setMessage(info.getDescription());
		// 当点确定按钮时从服务器上下载 新的apk 然后安装
		builer.setPositiveButton(mContext.getText(R.string.yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.i(TAG, "download update apk...");
						//启动下载任务
						mHandler.sendEmptyMessage(UPDATE_CLIENT);
						//下载开始提示
						Toast.makeText(mContext, R.string.backgrounddnld, Toast.LENGTH_SHORT);
					}
				});
		// 当点取消按钮时进行登录
		builer.setNegativeButton(mContext.getText(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// do nothing
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}

	/**
	 * 安装apk
	 * 
	 * @param url
	 */
	public  Intent getIntallIntent() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return null;
		}
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(it);

		return it;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (packInfo != null) {
			return packInfo.versionName;
		} else {
			return "1.0";
		}
	}

}
