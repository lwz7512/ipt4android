package com.ybcx.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hlidskialf.android.hardware.ShakeListener;
import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.activity.base.FullScreenActivity;
import com.ybcx.adapter.GalleryImageAdapter;
import com.ybcx.data.TPicDesc;
import com.ybcx.service.DnldService;
import com.ybcx.service.MsgService;
import com.ybcx.task.GenericTask;
import com.ybcx.task.RetrieveGalleryTask;
import com.ybcx.task.TaskAdapter;
import com.ybcx.task.TaskListener;
import com.ybcx.task.TaskManager;
import com.ybcx.task.TaskParams;
import com.ybcx.task.TaskResult;
import com.ybcx.tool.SimpleImageLoader;
import com.ybcx.upgrade.UpdateManager;
import com.ybcx.util.DateTimeHelper;
import com.ybcx.util.Preferences;

public class HomeGallery extends FullScreenActivity {

	private static final String TAG = "HomeGallery";
	
	private TaskManager taskManager = new TaskManager();
	
	private ShakeListener shaker;
	private UpdateManager updater;
	
	// Header
	private ImageButton refresh;
	private ProgressBar progressbar;

	// Grid
	private GridView gallery;

	// Footer
	private TextView tv_post;
	private TextView tv_hotpic;
	private TextView tv_community;
	private TextView tv_tags;
	private TextView tv_mine;

	private GalleryImageAdapter gridAdptr;
	private GenericTask mRetrieveTask;

	
	// 默认2秒后才能刷新，小于这个间隔不给取
	private static final long REFRESH_THRESHOLD = 1 * 2 * 1000;
	
	//随机画廊数据获取间隔
	//FIXME, 添加摇晃查询随机限制，以解决地铁公交上查看首页频繁查询问题
	//2012/01/20
	private static final long RANDOM_THRESHOLD = 10 * 2 * 1000;
	//随机画廊数据查询次数限制
	private static final int RANDOM_QUERY_MAX = 3;
	//随机画廊查询计数器
	private int randomQueryCounter = 0;
	
	//当前活动状态
	private boolean isAwake = true;
	//即时晃动查询开关，没取到数据时就打开开关
	private boolean shakeImmediately = false;

	
	
	
	@Override
	// Activity life cycle method
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homegallery);
		// 获得组件引用
		getViews();
		// 添加事件监听
		addEventListeners();

		// 首先尝试读取数据库缓存
		retrieveGalleryFromDB();
		
		// 网络检查
		if (!PintuApp.isNetworkAvailable()) {
			updateProgress(R.string.network_ungeilivable);
			return;
		}
		// 检查自己的消息
		startRetrieveMsgs();
		// 启动更新检查
		startUpdateCheck();
	}

	private void startUpdateCheck() {
		// 只有本地版才会提供自动升级功能
		if (PintuApp.VERSION_STATE.equals(PintuApp.LOCAL_VERSION)) {
			// 更新检查器，与一个下载服务进行交互
			updater = new UpdateManager(this, updateHandler);
			updateHandler.sendEmptyMessage(UpdateManager.UPGRADE_CHECK);
		}
	}

	// 每次画廊处于活动状态时都尝试获取远程数据
	protected void onStart() {
		super.onStart();
		// 不自动查询远程了，手动刷新吧
		// retrieveRemoteGallery();
		isAwake = true;
	}
	
	protected void onStop(){
		super.onStop();
		isAwake = false;
	}

	private void startRetrieveMsgs() {

		Intent it = new Intent();
		// 只有本地版才会提供自动升级功能
		if (PintuApp.VERSION_STATE.equals(PintuApp.LOCAL_VERSION)) {
			it.putExtra("configFileURL", PintuApp.mApi.getConfigURL());
		}
		it.setClass(this, MsgService.class);
		startService(it);
	}

	private void retrieveGalleryFromDB() {
		List<TPicDesc> items = PintuApp.dbApi.getCachedThumbnails();
		Log.i(TAG, ">>> cached recode size: " + items.size());
		// 先读取缓存
		if (items.size() > 0) {
			gridAdptr.refresh(items);
		} else {
			// 如果缓存没有，就从远程查询
			// 这往往发生在用户第一次登陆
			retrieveRemoteGallery();
		}
	}

	@Override
	// Activity life cycle method
	protected void onDestroy() {
		Log.d(TAG, "onDestroy.");
		super.onDestroy();

		taskManager.cancelAll();

		// 记下本次结束访问的时间
		// 下次启动时，作为其他视图是否获取数据的依据
		rememberLastVisit();

		// 退出时取消消息通知
		// 2011/12/22
		PintuApp.cancelNotification();

		// 杀掉应用进程
		// 这个真好使，所有的线程和异步任务都干掉了！
		Process.killProcess(Process.myPid());
	}

	private void rememberLastVisit() {
		long existTime = DateTimeHelper.getNowTime();
		this.getPreferences().edit()
				.putLong(Preferences.LAST_VISIT_TIME, existTime).commit();
		Log.d(TAG, "lastVisit: " + DateTimeHelper.getRelativeDate(new Date()));
	}

	private void getViews() {

		refresh = (ImageButton) findViewById(R.id.refresh_btn);
		progressbar = (ProgressBar) findViewById(R.id.top_refresh_progressBar);
		gallery = (GridView) findViewById(R.id.ptgallery);
		// 初始化画廊数据
		gridAdptr = new GalleryImageAdapter(this);
		gallery.setAdapter(gridAdptr);

		tv_post = (TextView) findViewById(R.id.tv_post);
		tv_hotpic = (TextView) findViewById(R.id.tv_hotpic);
		tv_community = (TextView) findViewById(R.id.tv_community);
		tv_tags = (TextView) findViewById(R.id.tv_tags);
		tv_mine = (TextView) findViewById(R.id.tv_mine);

	}

	private void addEventListeners() {
		refresh.setOnClickListener(refreshListener);
		gallery.setOnItemClickListener(cellClickListener);

		tv_post.setOnClickListener(postImgListener);
		tv_hotpic.setOnClickListener(hotpicListener);
		tv_community.setOnClickListener(communityListener);
		tv_tags.setOnClickListener(tagsListener);
		tv_mine.setOnClickListener(mineListener);

		// 初始化重力感应监听
		addShakeListener();
	}

	private void addShakeListener() {
		try {
			shaker = new ShakeListener(this);
		} catch (UnsupportedOperationException e) {
			updateProgress(e.getMessage());
		}
		if (shaker != null) {
			shaker.setOnShakeListener(sklistener);
		}
	}

	private ShakeListener.OnShakeListener sklistener = new ShakeListener.OnShakeListener() {
		public void onShake() {
			//只有活动状态才监听
			if(!isAwake) return;
			
			// 网络检查
			if (!PintuApp.isNetworkAvailable()) {
				updateProgress(R.string.network_ungeilivable);
				return;
			}
			
			if(shakeImmediately){
				//开始查询...
				retrieveRandomGallery();
				return;
			}
			
			long lastRefreshTime = getPreferences().getLong(
					Preferences.LAST_GALLERY_REFRESH_TIME, 0);
			long nowTime = DateTimeHelper.getNowTime();
			long diff = nowTime - lastRefreshTime;
			//查询间隔检查	
			if (diff < RANDOM_THRESHOLD) {
				updateProgress(R.string.ten_sec_refresh);
				return;
			}
						
			if(randomQueryCounter<RANDOM_QUERY_MAX){
				//开始查询...
				retrieveRandomGallery();				
			}else{
				updateProgress(R.string.shake_arrive_max);
			}
			
		} //end of shake listen
	};

	private OnClickListener mineListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			// 启动俺滴界面
			intent.setClass(HomeGallery.this, AndiAssets.class);
			startActivity(intent);
		}
	};

	private OnClickListener tagsListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(HomeGallery.this, TagList.class);
			startActivity(intent);
		}
	};

	private OnClickListener communityListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			// 启动社区界面
			intent.setClass(HomeGallery.this, CmntDaren.class);
			startActivity(intent);
		}
	};

	private OnClickListener hotpicListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			// 启动热图界面
			intent.setClass(HomeGallery.this, HotPicClassic.class);
			startActivity(intent);
		}
	};

	private OnItemClickListener cellClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 将截图ID取出来，送到详情活动中查询
			TPicDesc cellSelected = gridAdptr.getItem(position);
			String tpId = cellSelected.tpId;
			Intent it = new Intent();
			it.setClass(HomeGallery.this, PictureDetails.class);
			it.putExtra("tpId", tpId);
			// 打开详情活动
			startActivity(it);
		}
	};

	private OnClickListener postImgListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();
			// 启动编辑界面
			intent.setClass(HomeGallery.this, PictureEdit.class);
			startActivity(intent);
		}
	};

	private OnClickListener refreshListener = new OnClickListener() {
		public void onClick(View v) {
			retrieveRemoteGallery();
		}
	};

	private void retrieveRemoteGallery() {
		// 网络检查
		if (!PintuApp.isNetworkAvailable()) {
			updateProgress(R.string.network_ungeilivable);
			return;
		}

		long lastRefreshTime = this.getPreferences().getLong(
				Preferences.LAST_GALLERY_REFRESH_TIME, 0);
		long nowTime = DateTimeHelper.getNowTime();
		long diff = nowTime - lastRefreshTime;
		boolean shouldRetrieve;
		if (diff > REFRESH_THRESHOLD) {
			shouldRetrieve = true;
		} else {
			shouldRetrieve = false;
			this.updateProgress(R.string.two_sec_refresh);
		}
		if (shouldRetrieve) {
			// 将开始时间减小点，以解决服务器时间变慢引起的画廊为空问题
			// 后来服务器已经修正时钟不同步的问题了，留着吧，这样能查多点
			// 2011/12/23
			int minusLength = 3 * 60 * 1000;
			lastRefreshTime -= minusLength;
			// 开始查询
			doRetrieve(lastRefreshTime, nowTime);
		}

	}

	private void doRetrieve(long startTime, long endTime) {
		Log.d(TAG, "Attempting retrieve gallery data...");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new RetrieveGalleryTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);

			TaskParams params = new TaskParams();
			params.put("startTime", startTime);
			params.put("endTime", endTime);
			mRetrieveTask.execute(params);

			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
		}
	}

	private void retrieveRandomGallery() {
		Log.d(TAG, "Attempting retrieve gallery data...");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new RetrieveGalleryTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);

			TaskParams params = new TaskParams();
			params.put("method", "getRandGallery");
			mRetrieveTask.execute(params);

			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
			
			randomQueryCounter++;
		}
	}

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		public void onPreExecute(GenericTask task) {
			progressbar.setVisibility(View.VISIBLE);
			refresh.setVisibility(View.GONE);
		}

		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				// 保存画廊更新时间
				Editor pref = getPreferences().edit();
				pref.putLong(Preferences.LAST_GALLERY_REFRESH_TIME,
						DateTimeHelper.getNowTime());
				// 必须得提交设置，否则不生效
				pref.commit();

			} else if (result == TaskResult.FAILED) {
				updateProgress(R.string.error_server_down);
			} else if (result == TaskResult.IO_ERROR) {
				updateProgress(R.string.network_ungeilivable);
			} else if (result == TaskResult.JSON_PARSE_ERROR) {
				updateProgress(R.string.json_parse_error);
			}
			progressbar.setVisibility(View.GONE);
			refresh.setVisibility(View.VISIBLE);
		}

		// 结果拿到了，填充视图并入库
		public void deliverRetrievedList(List<Object> results) {

			String msg;
			// 如果没有取到数据就不处理了
			if (results.size() == 0) {
				if (shaker != null) {// 可以使用传感器，摇晃随便看看
					msg = getText(R.string.shake_toget_random).toString();					
					//即时晃动开关开启
					shakeImmediately = true;					
				} else {// 没法随便看看，只能休息了
					msg = getText(R.string.have_a_rest).toString();
				}
				updateProgress(msg);
				return;
			}else{
				//即时晃动开关关闭
				shakeImmediately = false;
			}

			// 准备缓存数据
			List<TPicDesc> items = new ArrayList<TPicDesc>();
			for (Object o : results) {
				items.add((TPicDesc) o);
			}

			// 先入库，更合理
			int successNum = PintuApp.dbApi.insertThumbnails(items);
			Log.i(TAG, ">>> inserted db record size: " + successNum);

			// 刷库更新画廊，这样最简单
			retrieveGalleryFromDB();
		}

	}; // end of mRetrieveTaskListener

	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	private void updateProgress(int message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	// ------------------- update process ----------------------------------------------

	private Handler updateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UpdateManager.UPGRADE_CHECK:
				Log.i(TAG, "to check update...");
				runCheckUpdateTask();
				break;

			case UpdateManager.POPUP_DIALOG:
				Log.i(TAG, "to show dialog...");
				updater.showUpdataDialog();

				break;

			case UpdateManager.UPDATE_CLIENT:
				startDownloadApkService();

				break;

			}
		}
	};

	private void runCheckUpdateTask() {
		new Thread() {
			public void run() {
				updater.checkUpdateInfo(PintuApp.mApi.getConfigURL());
			}
		}.start();
	}

	private void startDownloadApkService() {
		Intent it = new Intent();
		it.putExtra("apkurl", updater.info.getApkurl());
		it.setClass(this, DnldService.class);
		startService(it);

		// 下载开始提示
		Toast.makeText(this, R.string.backgrounddnld, Toast.LENGTH_SHORT)
				.show();
	}

	// ------------------- option menu definition ----------------------------------------

	protected static final int OPTIONS_MENU_ID_LOGOUT = 1;
	protected static final int OPTIONS_MENU_ID_PREFERENCES = 2;
	protected static final int OPTIONS_MENU_ID_ABOUT = 3;
	protected static final int OPTIONS_MENU_ID_SEARCH = 4;
	protected static final int OPTIONS_MENU_ID_REPLIES = 5;
	protected static final int OPTIONS_MENU_ID_DM = 6;
	protected static final int OPTIONS_MENU_ID_TWEETS = 7;
	protected static final int OPTIONS_MENU_ID_TOGGLE_REPLIES = 8;
	protected static final int OPTIONS_MENU_ID_FOLLOW = 9;
	protected static final int OPTIONS_MENU_ID_UNFOLLOW = 10;
	protected static final int OPTIONS_MENU_ID_IMAGE_CAPTURE = 11;
	protected static final int OPTIONS_MENU_ID_PHOTO_LIBRARY = 12;
	protected static final int OPTIONS_MENU_ID_EXIT = 13;
	protected static final int OPTIONS_MENU_ID_HELP = 14;

	/**
	 * 如果增加了Option Menu常量的数量，则必须重载此方法， 以保证其他人使用常量时不产生重复
	 * 
	 * @return 最大的Option Menu常量
	 */
	protected int getLastOptionMenuId() {
		return OPTIONS_MENU_ID_EXIT;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item;
		item = menu.add(0, OPTIONS_MENU_ID_HELP, 0, R.string.omenu_help);
		item.setIcon(android.R.drawable.ic_menu_help);

		item = menu.add(0, OPTIONS_MENU_ID_LOGOUT, 0, R.string.omenu_signout);
		item.setIcon(android.R.drawable.ic_menu_revert);

		item = menu.add(0, OPTIONS_MENU_ID_ABOUT, 0, R.string.omenu_about);
		item.setIcon(android.R.drawable.ic_menu_info_details);

		item = menu.add(0, OPTIONS_MENU_ID_EXIT, 0, R.string.omenu_exit);
		item.setIcon(android.R.drawable.ic_menu_rotate);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case OPTIONS_MENU_ID_LOGOUT:
			logout();
			return true;
		case OPTIONS_MENU_ID_HELP:
			intent = new Intent().setClass(this, HowTos.class);
			startActivity(intent);
			return true;
		case OPTIONS_MENU_ID_ABOUT:
			intent = new Intent().setClass(this, AboutThis.class);
			startActivity(intent);
			return true;
		case OPTIONS_MENU_ID_EXIT:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeGallery.this);
		builder.setTitle(getText(R.string.note));
		builder.setMessage(getText(R.string.suretologout));
		builder.setPositiveButton(getText(R.string.yes), okListener);
		builder.setNegativeButton(getText(R.string.cancel), cancelListener);

		Dialog dialog = builder.create();
		dialog.show();
	}

	private final DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			backToInstall();
		}
	};

	private final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	private void backToInstall() {

		PintuApp.dbApi.clearData();

		// Clear SharedPreferences
		this.getPreferences().edit().clear().commit();

		// 提供用户手动情况所有缓存选项
		SimpleImageLoader.clearAll();

		// close ui
		finish();
	}

} // end of class
