package com.pintu.service;

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
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.util.FileHelper;

/**
 * 下载安装包apk文件，和下载原图文件
 * 采用这种方式不影响用户界面操作，体验较好
 * 
 * @author lwz
 * 2012/12/27
 *
 */
public class DnldService extends Service {

	private static final String TAG = "DnldApkService";

	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;

	private static final int DOWNLOAD_APK = 1;
	private static final int DOWNLOAD_IMG = 2;
	private static final int FINISH_ME = 3;
	

	private int currentServiceId;

	private String apkURL;
	private String apkFileName = "PintuMain-release.apk";
	
	private String imgURL;
	private String imgType = ".jpg";

	public void onCreate() {
		Log.v(TAG, "DnldApkService Created!");
		super.onCreate();
		
		// 建立该服务实际运行的线程
		HandlerThread thread = new HandlerThread(TAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Starting #" + startId + ": " + intent.getExtras());

		// 记下来好结束它
		currentServiceId = startId;

		// 从参数中，获取下载文件需要的信息
		Bundle extras = intent.getExtras();
		if (extras != null) {
			//下载升级安装包
			apkURL = extras.getString("apkurl");
			if(apkURL!=null){
				mServiceHandler.sendEmptyMessage(DOWNLOAD_APK);
				Log.i(TAG, "to download apk...");				
			}
			//下载原图片，可能有点大
			imgURL = extras.getString("imgurl");
			imgType = extras.getString("imgtype");
			if(imgURL!=null && imgType!=null){
				mServiceHandler.sendEmptyMessage(DOWNLOAD_IMG);
				Log.i(TAG, "to download img...");	
			}
			
		}

		// 不自动重启服务
		return START_NOT_STICKY;
	}

	/**
	 * 建立操作器来处理消息，即应用客户端传来的数据
	 * 
	 * @author lwz
	 * 
	 */
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		// 处理sendMessage(msg)添加到队列中的消息
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case DOWNLOAD_APK:
				downloadApk();
				break;
			case DOWNLOAD_IMG:
				downloadImg();				
				break;

			case FINISH_ME:
				// 停止运行服务，准备下次运行
				// 会被系统触发onDestroy()
				stopSelf(currentServiceId);

			}

		} // end of handleMessage

	};

	/**
	 * 外部接口，让主服务来调用
	 */
	private void downloadApk() {
		if (apkURL == null) return;

		URL url = null;
		HttpURLConnection conn = null;
		InputStream is;
		BufferedOutputStream out = null;
		byte[] buffer = new byte[1024];
		int len;

		try {
			url = new URL(apkURL);
			if (url != null)
				conn = (HttpURLConnection) url.openConnection();
			if (conn != null) {
				conn.setConnectTimeout(5000);
				is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				out = new BufferedOutputStream(this.openFileOutput(
						apkFileName, Context.MODE_WORLD_READABLE));
				while ((len = bis.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.close();
				bis.close();
				is.close();
				
				// 下载结束
				notifyUserToInstallUpdate();
				//停止服务
				mServiceHandler.sendEmptyMessage(FINISH_ME);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "apk url is malformed: " + apkURL);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "save file write error: " + this.getFilesDir() + "/"
					+ apkURL);
		} catch (IOException e) {
			Log.e(TAG, "URL Connection FAILED: " + apkURL);
		}

	}
	
	private void downloadImg(){
		if(imgURL==null) return;
		
		File dir = null;
		try {
			dir = FileHelper.getBasePath();
		} catch (IOException e) {
			Log.e(TAG, "SD Card not mounted!");
		}
        if(dir==null) {
    		Toast.makeText(this, "SD Card not available!", Toast.LENGTH_LONG);    		
    	}
		
		URL url = null;
		HttpURLConnection conn = null;
		InputStream is;		

		try {
			url = new URL(imgURL);
			if (url != null)
				conn = (HttpURLConnection) url.openConnection();
			if (conn != null) {
				conn.setConnectTimeout(5000);
				is = conn.getInputStream();
				
				String fileName = createImgFileName(imgType);
			    String filePath = writeToFile2SD(is, fileName);
			    
				String imgSaved = this.getText(R.string.img_saved).toString();
				String successHint = imgSaved+filePath;
				//弹出提示
				Toast.makeText(this, successHint, Toast.LENGTH_LONG).show();
				//停止服务
				mServiceHandler.sendEmptyMessage(FINISH_ME);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "img url is malformed: " + imgURL);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "save file write error: " + this.getFilesDir() + "/"
					+ imgURL);
		} catch (IOException e) {
			Log.e(TAG, "URL Connection FAILED: " + imgURL);
		}				
		
	}
	
	private String writeToFile2SD(InputStream is, String filename) {
        Log.d("LDS", "new write to file");
        BufferedInputStream in = null;
        FileOutputStream out = null;
        File file = null;        
        try {        	        
        	file = new File(FileHelper.getBasePath(), filename);
        	out = new FileOutputStream(file);
            in  = new BufferedInputStream(is);            
            byte[] buffer = new byte[1024];
            int l;
            while ((l = in.read(buffer)) != -1) {
                out.write(buffer, 0, l);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (in  != null) in.close();
                if (out != null) {
                    Log.d("LDS", "new write to file to -> " + filename);
                    out.flush();
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    	if(file!=null){
    		return file.getAbsolutePath();    		
    	}else{
    		return null;
    	}
    }
	
    private String createImgFileName(String picType){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return dateFormat.format(new Date()) + picType;
    }

	private void notifyUserToInstallUpdate() {
		Intent it = getIntallIntent();
		if (it != null) {
			// 点击通知执行的动作
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					it, 0);
			// 消息内容
			String content = getText(R.string.dnldover).toString();
			// 状态栏显示内容
			Notification notification = new Notification(R.drawable.dnloaded,
					content, System.currentTimeMillis());
			// 消息展开后显示内容，及触发动作
			notification.setLatestEventInfo(this,
					getText(R.string.notification), content, contentIntent);

			// 用户点一下就清除通知
			notification.flags = Notification.FLAG_AUTO_CANCEL
					| Notification.FLAG_ONLY_ALERT_ONCE
					| Notification.FLAG_SHOW_LIGHTS;

			notification.ledARGB = 0xFF84E4FA;
			notification.ledOnMS = 5000;
			notification.ledOffMS = 5000;

			// 声音模式
			notification.defaults = Notification.DEFAULT_SOUND;

			// we use a string id because it is a unique number.
			// we use it later to cancel the notification
			PintuApp.mNotificationManager.notify(R.string.dnldover,
					notification);
		} else {
			Log.w(TAG, "install instent is null!");
		}
	}

	/**
	 * 安装apk
	 * 
	 * @param url
	 */
	private Intent getIntallIntent() {
		String installerPath = this.getFilesDir() + "/" + apkURL;
		Log.i(TAG, "installer path: " + installerPath);
		File apkfile = new File(installerPath);
		if (!apkfile.exists()) {
			return null;
		}
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		it.setDataAndType(Uri.fromFile(apkfile),
				"application/vnd.android.package-archive");

		return it;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDestroy() {
		Log.d(TAG, "MsgService Destroyed!");
		mServiceLooper.quit();
	}

}
