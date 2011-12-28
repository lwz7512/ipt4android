package com.pintu.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

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
import com.pintu.activity.AndiAssets;
import com.pintu.data.TMsg;
import com.pintu.http.HttpException;
import com.pintu.upgrade.UpdateManager;
import com.pintu.util.Preferences;

/**
 * 两项任务：查询个人消息、检查更新包并提示安装
 * 2011/12/25
 * 
 * @author lwz
 *
 */
public class MsgService extends Service {
	private static final String TAG = "MsgService";
	/**
	 * 在当前的Java内存模型下，线程可以把变量保存在本地内存（比如机器的寄存器）中，
	 * 而不是直接在主存中进行读写。这就可能造成一个线程在主存中修改了一个变量的值，
	 * 而另外一个线程还继续使用它在寄存器中的变量值的拷贝，造成数据的不一致。
	 * 要解决这个问题，只需要像在本程序中的这样，把该变量声明为volatile（不稳定的）即可，
	 * 这就指示JVM，这个变量是不稳定的，每次使用它都到主存中进行读取。 一般说来，多任务环境下各任务间共享的标志都应该加volatile修饰。
	 * Volatile修饰的成员变量在每次被线程访问时，都强迫从共享内存中重读该成员变量的值。
	 * 而且，当成员变量发生变化时，强迫线程将变化值回写到共享内存。 这样在任何时刻，两个不同的线程总是看到某个成员变量的同一个值。
	 */
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	
	private static final int FETCH_MSG = 1;
	private static final int FINISH_ME = 2;
	
	
	private int currentServiceId;		
		
	
	public void onCreate() {
		Log.v(TAG, "MsgService Created!");
		super.onCreate();

		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		// 建立该服务实际运行的线程
		HandlerThread thread = new HandlerThread(TAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		// 建立操作器来处理消息，即应用客户端传来的数据，handler需要一个队列来执行
		// 这个队列是独立的HandlerThread产生的，不会阻塞主线程；
		// 2010/05/21 liwenzhi
		mServiceHandler = new ServiceHandler(mServiceLooper);
				
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Starting #" + startId + ": " + intent.getExtras());

		//记下来好结束它
		currentServiceId = startId;						
		
		Message msg = mServiceHandler.obtainMessage();		
		msg.what = FETCH_MSG;		
		mServiceHandler.sendMessage(msg);
		Log.i(TAG, "to fetch my msg...");

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
			
			case FETCH_MSG:
				// 查询自己的消息
				retrieveMyMsgs();				
				break;					
				
			case FINISH_ME:
				// 停止运行服务，准备下次运行
				//会被系统触发onDestroy()
				stopSelf(currentServiceId);
				
			}
			
			
		} //end of handleMessage

	};
	


	private void retrieveMyMsgs() {
		String userId = PintuApp.getUser();
		JSONArray jsMsgs = null;
		List<TMsg> retrievedMsgs = null;
		try {
			Log.d(TAG, "Fetching user unreaded message...");
			jsMsgs = PintuApp.mApi.getNewMessages(userId);
			Log.d(TAG, "User message fetched: " + jsMsgs.length());
			if (jsMsgs != null) {
				retrievedMsgs = jsonMsgToObjs(jsMsgs);
			}
			if (retrievedMsgs != null) {
				// 先入库缓存
				int newMsgNum = PintuApp.dbApi.insertMyMsgs(retrievedMsgs);
				// 将来这里要判断消息数量，如果大于0才发通知
				// 发通知
				if (newMsgNum > 0)
					notifyUserForNewMsg(newMsgNum);
				//停止服务
				mServiceHandler.sendEmptyMessage(FINISH_ME);
			}

		} catch (HttpException e) {
			Log.e(TAG, "Message fetching error!");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(TAG, "Message data parsing error!");
			e.printStackTrace();
		}

	}

	private List<TMsg> jsonMsgToObjs(JSONArray jsMsgs) {
		List<TMsg> retrievedMsgs = new ArrayList<TMsg>();
		try {
			for (int i = 0; i < jsMsgs.length(); i++) {
				TMsg msg = TMsg.parseJsonToObj(jsMsgs.getJSONObject(i));
				retrievedMsgs.add(msg);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.w(TAG, ">>> json Array getJSONObject Exception!");
		}
		return retrievedMsgs;
	}
	

	
	private void notifyUserForNewMsg(int msgNum) {
		// 打开俺滴家当
		Intent it = new Intent(this, AndiAssets.class);
		// 因为是在俺滴中的消息中，所以要指明切换到消息子活动
		it.putExtra(Preferences.MYASSETS_INDEX, 3);
		// 点击通知执行的动作
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, it, 0);
		// 消息内容
		String content = getText(R.string.youhave) + String.valueOf(msgNum)
				+ getText(R.string.shellunit) + getText(R.string.messages);
		// 状态栏显示内容
		Notification notification = new Notification(R.drawable.msg, content,
				System.currentTimeMillis());
		// 消息展开后显示内容，及触发动作
		notification.setLatestEventInfo(this, getText(R.string.notification),
				content, contentIntent);

		//用户点一下就清除通知
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
		PintuApp.mNotificationManager.notify(R.string.messages, notification);
	}

	public void onDestroy() {
		Log.d(TAG, "MsgService Destroyed!");
		mServiceLooper.quit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
